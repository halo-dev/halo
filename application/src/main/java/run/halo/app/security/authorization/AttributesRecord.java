package run.halo.app.security.authorization;

import java.security.Principal;

/**
 * @author guqing
 * @since 2.0.0
 */
public class AttributesRecord implements Attributes {
    private final RequestInfo requestInfo;
    private final Principal principal;

    public AttributesRecord(Principal principal, RequestInfo requestInfo) {
        this.requestInfo = requestInfo;
        this.principal = principal;
    }

    @Override
    public Principal getPrincipal() {
        return this.principal;
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

    @Override
    public String getUserSpace() {
        return requestInfo.getUserspace();
    }
}
