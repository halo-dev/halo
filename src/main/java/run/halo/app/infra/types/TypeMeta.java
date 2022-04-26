package run.halo.app.infra.types;

import lombok.Data;

/**
 * TypeMeta describes an individual object in an API response or request
 * with strings representing the type of the object and its API schema version.
 * Structures that are versioned or persisted should inline TypeMeta.
 *
 * @author guqing
 * @since 2.0.0
 */
@Data
public class TypeMeta {

    /**
     * Kind is a string value representing the REST resource this object represents.
     * Servers may infer this from the endpoint the client submits requests to.
     * Cannot be updated.
     * In CamelCase.
     * More info:
     * <a href="https://git.k8s.io/community/contributors/devel/sig-architecture/api-conventions.md#types-kinds>types-kinds</a>
     * +optional
     */
    String kind;

    /**
     * APIVersion defines the versioned schema of this representation of an object.
     * Servers should convert recognized schemas to the latest internal value, and
     * may reject unrecognized values.
     * More info:
     * <a href="https://git.k8s.io/community/contributors/devel/sig-architecture/api-conventions.md#resources">resources</a>
     * +optional
     */
    String apiVersion;
}
