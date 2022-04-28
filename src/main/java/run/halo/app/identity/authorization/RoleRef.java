package run.halo.app.identity.authorization;

import lombok.Data;

/**
 * RoleRef contains information that points to the role being used
 *
 * @author guqing
 * @see
 * <a href="https://github.com/kubernetes/kubernetes/blob/537941765fe1304dd096c1a2d4d4e70f10768218/pkg/apis/rbac/types.go#L83">types#RoleRef</a>
 * @since 2.0.0
 */
@Data
public class RoleRef {

    /**
     * Kind is the type of resource being referenced
     */
    String kind;

    /**
     * Name is the name of resource being referenced
     */
    String name;

    /**
     * APIGroup is the group for the resource being referenced
     */
    String apiGroup;
}
