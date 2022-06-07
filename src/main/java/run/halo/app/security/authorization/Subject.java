package run.halo.app.security.authorization;

import lombok.Data;

/**
 * @author guqing
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
