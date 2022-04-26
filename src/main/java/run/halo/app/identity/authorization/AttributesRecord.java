package run.halo.app.identity.authorization;

import org.springframework.security.core.userdetails.UserDetails;

/**
 * AttributesRecord is used by an Authorizer to get information about a request
 * that is used to make an authorization decision.
 *
 * @author guqing
 * @since 2.0.0
 */
public class AttributesRecord {
    /**
     * @return the UserDetails object to authorize
     */
    public UserDetails getUser() {
        return null;
    }

    /**
     * @return the verb associated with API requests(this includes get, list,
     * watch, create, update, patch, delete, deletecollection, and proxy)
     * or the lower-cased HTTP verb associated with non-API requests(this
     * includes get, put, post, patch, and delete)
     */
    public String getVerb() {
        return null;
    }

    /**
     * @return when isReadOnly() == true, the request has no side effects, other than
     * caching, logging, and other incidentals.
     */
    public boolean isReadOnly() {
        return false;
    }
}
