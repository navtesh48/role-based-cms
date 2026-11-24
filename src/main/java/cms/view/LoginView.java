package cms.view;

import cms.controller.ArticleController;
import cms.controller.AuthController;
import cms.model.User;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public final class LoginView extends JFrame {
    private final JTextField username = new JTextField(16);
    private final JPasswordField password = new JPasswordField(16);

    public LoginView(AuthController auth, ArticleController articles) {
        super("Content Management System: Sign in");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JButton login = new JButton("Sign in");
        JButton signUp = new JButton("Create account");

        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 8, 6, 8);
        c.gridx = 0; c.gridy = 0; p.add(new JLabel("Username"), c);
        c.gridx = 1; p.add(username, c);
        c.gridx = 0; c.gridy = 1; p.add(new JLabel("Password"), c);
        c.gridx = 1; p.add(password, c);
        c.gridx = 0; c.gridy = 2; p.add(signUp, c);
        c.gridx = 1; p.add(login, c);
        setContentPane(p);
        getRootPane().setDefaultButton(login);

        login.addActionListener(e -> {
            try {
                User u = auth.login(username.getText().trim(), new String(password.getPassword()));
                password.setText("");
                new MainView(auth, articles, u, this).setVisible(true);
                setVisible(false);
            } catch (RuntimeException ex) {
                Dialogs.error(this, ex);
            }
        });
        signUp.addActionListener(e -> {
            try {
                auth.signUp(username.getText().trim(), new String(password.getPassword()));
                JOptionPane.showMessageDialog(this, "Account created. You can sign in now.");
            } catch (RuntimeException ex) {
                Dialogs.error(this, ex);
            }
        });
        pack();
        setLocationRelativeTo(null);
    }
}
