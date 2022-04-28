package run.halo.app.identity.authorization;

import lombok.Data;

/**
 * Subject contains a reference to the object or user identities a role binding applies to.  This
 * can either hold a direct API object reference,
 * or a value for non-objects such as user and group names.
 *
 * @author guqing
 * @see
 * <a href="https://github.com/kubernetes/kubernetes/blob/537941765fe1304dd096c1a2d4d4e70f10768218/pkg/apis/rbac/types.go#L67">types#Subject</a>
 * @since 2.0.0
 */
@Data
public class Subject {
    /**
     * Kind of object being referenced. Values defined by this API group are "User", "Group",
     * and "ServiceAccount".
     * If the Authorizer does not recognized the kind value, the Authorizer should report
     * an error.
     */
    String kind;

    /**
     * Name of the object being referenced.
     */
    String name;

    /**
     * APIGroup holds the API group of the referenced subject.
     * Defaults to "" for ServiceAccount subjects.
     * Defaults to "rbac.authorization.k8s.io" for User and Group subjects.
     */
    String apiGroup;
}
