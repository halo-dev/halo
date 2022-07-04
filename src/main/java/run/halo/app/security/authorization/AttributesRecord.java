package run.halo.app.security.authorization;

import org.springframework.security.core.userdetails.UserDetails;

/**
 * @author guqing
 * @since 2.0.0
 */
public class AttributesRecord implements Attributes {
    private final RequestInfo requestInfo;
    private final UserDetails user;

    public AttributesRecord(UserDetails user, RequestInfo requestInfo) {
        this.requestInfo = requestInfo;
        this.user = user;
    }

    @Override
    public UserDetails getUser() {
        return this.user;
    }

    @Override
    public String getVerb() {
        return requestInfo.getVerb();
    }

    @Override
    public boolean isReadOnly() {
        String verb = requestInfo.getVerb();
        return "get".equals(verb)
            || "list".equals(verb)
            || "watch".equals(verb);
    }

    @Override
    public String getResource() {
        return requestInfo.getResource();
    }

    @Override
    public String getSubresource() {
        return requestInfo.getSubresource();
    }

    @Override
    public String getName() {
        return requestInfo.getName();
    }

    @Override
    public String getApiGroup() {
        return requestInfo.getApiGroup();
    }

    @Override
    public String getApiVersion() {
        return requestInfo.getApiVersion();
    }

    @Override
    public boolean isResourceRequest() {
        return requestInfo.isResourceRequest();
    }

    @Override
    public String getPath() {
        return requestInfo.getPath();
    }

    @Override
    public String getSubName() {
        return requestInfo.getSubName();
    }
}
