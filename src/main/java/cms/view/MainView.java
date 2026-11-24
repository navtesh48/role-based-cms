package cms.view;

import cms.controller.ArticleController;
import cms.controller.AuthController;
import cms.model.Article;
import cms.model.Permission;
import cms.model.Role;
import cms.model.User;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/** Article list/editor for everyone; a Users tab appears only for roles with MANAGE_USERS. */
public final class MainView extends JFrame {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    private final AuthController auth;
    private final ArticleController articles;
    private final User user;
    private List<Article> current = List.of();

    private final DefaultTableModel articleModel = readOnlyModel("Title", "Author", "Updated");
    private final JTable articleTable = new JTable(articleModel);
    private final DefaultTableModel userModel = readOnlyModel("ID", "Username", "Role");
    private final JTable userTable = new JTable(userModel);

    public MainView(AuthController auth, ArticleController articles, User user, JFrame loginFrame) {
        super("Content Management System");
        this.auth = auth;
        this.articles = articles;
        this.user = user;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(820, 520);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Articles", articlesPanel());
        if (user.role().can(Permission.MANAGE_USERS)) tabs.addTab("Users", usersPanel());

        JButton logout = new JButton("Sign out");
        logout.addActionListener(e -> {
            auth.logout();
            dispose();
            loginFrame.setVisible(true);
        });
        JPanel top = new JPanel(new BorderLayout());
        top.add(new JLabel("  Signed in as " + user.username() + " (" + user.role() + ")"), BorderLayout.WEST);
        top.add(logout, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        refreshArticles();
        if (user.role().can(Permission.MANAGE_USERS)) refreshUsers();
    }

    private static DefaultTableModel readOnlyModel(String... cols) {
        return new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    private JPanel articlesPanel() {
        articleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JButton add = new JButton("New");
        JButton edit = new JButton("Edit");
        JButton del = new JButton("Delete");
        add.setEnabled(user.role().can(Permission.CREATE_ARTICLE));
        add.addActionListener(e -> editArticle(null));
        edit.addActionListener(e -> selected().ifPresent(this::editArticle));
        del.addActionListener(e -> selected().ifPresent(a -> {
            if (JOptionPane.showConfirmDialog(this, "Delete \"" + a.title() + "\"?", "Confirm",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                run(() -> articles.delete(a.id()));
            }
        }));
        JPanel buttons = new JPanel();
        buttons.add(add); buttons.add(edit); buttons.add(del);
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JScrollPane(articleTable), BorderLayout.CENTER);
        p.add(buttons, BorderLayout.SOUTH);
        return p;
    }

    private JPanel usersPanel() {
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JComboBox<Role> role = new JComboBox<>(Role.values());
        JButton apply = new JButton("Set role");
        apply.addActionListener(e -> {
            int row = userTable.getSelectedRow();
            if (row < 0) return;
            long id = (Long) userModel.getValueAt(row, 0);
            try {
                auth.setRole(id, (Role) role.getSelectedItem());
                if (id == user.id() && role.getSelectedItem() != Role.ADMIN) {
                    JOptionPane.showMessageDialog(this, "You changed your own role. Please sign in again.");
                    dispose();
                    System.exit(0);
                }
                refreshUsers();
            } catch (RuntimeException ex) {
                Dialogs.error(this, ex);
            }
        });
        JPanel bar = new JPanel();
        bar.add(new JLabel("New role:")); bar.add(role); bar.add(apply);
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JScrollPane(userTable), BorderLayout.CENTER);
        p.add(bar, BorderLayout.SOUTH);
        return p;
    }

    private java.util.Optional<Article> selected() {
        int row = articleTable.getSelectedRow();
        return row < 0 ? java.util.Optional.empty() : java.util.Optional.of(current.get(row));
    }

    private void editArticle(Article existing) {
        JTextField title = new JTextField(existing == null ? "" : existing.title(), 30);
        JTextArea body = new JTextArea(existing == null ? "" : existing.body(), 12, 30);
        body.setLineWrap(true);
        body.setWrapStyleWord(true);
        JPanel form = new JPanel(new BorderLayout(0, 6));
        form.add(title, BorderLayout.NORTH);
        form.add(new JScrollPane(body), BorderLayout.CENTER);
        int ok = JOptionPane.showConfirmDialog(this, form, existing == null ? "New article" : "Edit article",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;
        run(() -> {
            if (existing == null) articles.create(title.getText(), body.getText());
            else articles.update(existing.id(), title.getText(), body.getText());
        });
    }

    private void run(Runnable action) {
        try {
            action.run();
            refreshArticles();
        } catch (RuntimeException ex) {
            Dialogs.error(this, ex);
        }
    }

    private void refreshArticles() {
        try {
            current = articles.list();
        } catch (RuntimeException ex) {
            Dialogs.error(this, ex);
            return;
        }
        articleModel.setRowCount(0);
        for (Article a : current) {
            articleModel.addRow(new Object[]{a.title(), a.authorName(), FMT.format(a.updatedAt())});
        }
    }

    private void refreshUsers() {
        userModel.setRowCount(0);
        for (User u : auth.users()) userModel.addRow(new Object[]{u.id(), u.username(), u.role()});
    }
}
