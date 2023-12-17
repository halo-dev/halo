package run.halo.app.core.extension;

import static run.halo.app.core.extension.RoleBinding.GROUP;
import static run.halo.app.core.extension.RoleBinding.KIND;
import static run.halo.app.core.extension.RoleBinding.VERSION;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.ExtensionOperator;
import run.halo.app.extension.GVK;
import run.halo.app.extension.Metadata;

/**
 * RoleBinding references a role, but does not contain it.
 * It can reference a Role in the global.
 * It adds who information via Subjects.
 *
 * @author guqing
 * @since 2.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@GVK(group = GROUP,
    version = VERSION,
    kind = KIND,
    plural = "rolebindings",
    singular = "rolebinding")
public class RoleBinding extends AbstractExtension {

    public static final String GROUP = "";

    public static final String VERSION = "v1alpha1";

    public static final String KIND = "RoleBinding";

    /**
     * Subjects holds references to the objects the role applies to.
     */
    List<Subject> subjects;

    /**
     * RoleRef can reference a Role in the current namespace or a ClusterRole in the global
     * namespace.
     * If the RoleRef cannot be resolved, the Authorizer must return an error.
     */
    RoleRef roleRef;

    /**
     * RoleRef contains information that points to the role being used.
     *
     * @author guqing
     * @since 2.0.0
     */
    @Data
    public static class RoleRef {

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

    /**
     * @author guqing
     * @since 2.0.0
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Subject {
        /**
         * Kind of object being referenced. Values defined by this API group are "User", "Group",
         * and "ServiceAccount".
         * If the Authorizer does not recognize the kind value, the Authorizer should report
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
         * Defaults to "rbac.authorization.halo.run" for User and Group subjects.
         */
        String apiGroup;

        public static Predicate<Subject> isUser(String username) {
            return subject -> User.KIND.equals(subject.getKind())
                && User.GROUP.equals(subject.getApiGroup())
                && username.equals(subject.getName());
        }

        public static Predicate<Subject> containsUser(Set<String> usernames) {
            return subject -> User.KIND.equals(subject.getKind())
                && User.GROUP.equals(subject.apiGroup)
                && usernames.contains(subject.getName());
        }
    }

    public static RoleBinding create(String username, String roleName) {
        var metadata = new Metadata();
        metadata.setName(String.join("-", username, roleName, "binding"));

        var roleRef = new RoleRef();
        roleRef.setKind(Role.KIND);
        roleRef.setName(roleName);
        roleRef.setApiGroup(Role.GROUP);

        var subject = new Subject();
        subject.setKind(User.KIND);
        subject.setName(username);
        subject.setApiGroup(User.GROUP);

        var binding = new RoleBinding();
        binding.setMetadata(metadata);
        binding.setRoleRef(roleRef);

        // keep the subjects mutable
        var subjects = new LinkedList<Subject>();
        subjects.add(subject);

        binding.setSubjects(subjects);
        return binding;
    }

    public static Predicate<RoleBinding> containsUser(String username) {
        return ExtensionOperator.<RoleBinding>isNotDeleted().and(
            binding -> binding.getSubjects().stream()
                .anyMatch(Subject.isUser(username)));
    }

    public static Predicate<RoleBinding> containsUser(Set<String> usernames) {
        return ExtensionOperator.<RoleBinding>isNotDeleted()
            .and(binding -> binding.getSubjects().stream()
                .anyMatch(Subject.containsUser(usernames)));
    }
}
