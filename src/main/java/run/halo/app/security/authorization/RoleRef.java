package run.halo.app.security.authorization;

import lombok.Data;

/**
 * RoleRef contains information that points to the role being used.
 *
 * @author guqing
 * @since 2.0.0
 */
@Data
public class RoleRef {

    /**
     * Kind is the type of resource being referenced.
     */
    String kind;

    /**
     * Name is the name of resource being referenced.
     */
    String name;

    /**
     * APIGroup is the group for the resource being referenced.
     */
    String apiGroup;
}
