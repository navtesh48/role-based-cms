package cms.model;

import java.util.EnumSet;
import java.util.Set;

/** Roles and the permissions each one grants. Access checks always go through {@link #can}. */
public enum Role {
    ADMIN(EnumSet.allOf(Permission.class)),
    USER(EnumSet.of(Permission.CREATE_ARTICLE, Permission.EDIT_OWN, Permission.DELETE_OWN));

    private final Set<Permission> permissions;

    Role(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public boolean can(Permission p) {
        return permissions.contains(p);
    }
}
