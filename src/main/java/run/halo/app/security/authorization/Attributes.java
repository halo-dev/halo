package run.halo.app.security.authorization;

import org.springframework.security.core.userdetails.UserDetails;

/**
 * Attributes is used by an Authorizer to get information about a request
 * that is used to make an authorization decision.
 *
 * @author guqing
 * @since 2.0.0
 */
public interface Attributes {
    /**
     * @return the UserDetails object to authorize
     */
    UserDetails getUser();

    /**
     * @return the verb associated with API requests(this includes get, list,
     * watch, create, update, patch, delete, deletecollection, and proxy)
     * or the lower-cased HTTP verb associated with non-API requests(this
     * includes get, put, post, patch, and delete)
     */
    String getVerb();

    /**
     * @return when isReadOnly() == true, the request has no side effects, other than
     * caching, logging, and other incidentals.
     */
    boolean isReadOnly();

    /**
     * @return The kind of object, if a request is for a REST object.
     */
    String getResource();

    /**
     * @return the subresource being requested, if present.
     */
    String getSubresource();

    /**
     * @return the name of the object as parsed off the request.  This will not be
     * present for all request types, but will be present for: get, update, delete
     */
    String getName();

    /**
     * @return The group of the resource, if a request is for a REST object.
     */
    String getApiGroup();

    /**
     * @return the version of the group requested, if a request is for a REST object.
     */
    String getApiVersion();

    /**
     * @return true for requests to API resources, like /api/v1/nodes,
     * and false for non-resource endpoints like /api, /healthz
     */
    boolean isResourceRequest();

    /**
     * @return returns the path of the request
     */
    String getPath();

    String getSubName();
}
