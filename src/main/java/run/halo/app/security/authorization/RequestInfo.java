package run.halo.app.security.authorization;

import java.util.Objects;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;

/**
 * RequestInfo holds information parsed from the {@link ServerHttpRequest}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Getter
@ToString
public class RequestInfo {
    boolean isResourceRequest;
    final String path;
    String namespace;
    String verb;
    String apiPrefix;
    String apiGroup;
    String apiVersion;
    String resource;

    String name;

    String subresource;

    String subName;

    String[] parts;

    public RequestInfo(boolean isResourceRequest, String path, String verb) {
        this(isResourceRequest, path, null, verb, null, null, null, null, null, null, null, null);
    }

    public RequestInfo(boolean isResourceRequest, String path, String namespace, String verb,
        String apiPrefix,
        String apiGroup,
        String apiVersion, String resource, String name, String subresource, String subName,
        String[] parts) {
        this.isResourceRequest = isResourceRequest;
        this.path = StringUtils.defaultString(path, "");
        this.namespace = StringUtils.defaultString(namespace, "");
        this.verb = StringUtils.defaultString(verb, "");
        this.apiPrefix = StringUtils.defaultString(apiPrefix, "");
        this.apiGroup = StringUtils.defaultString(apiGroup, "");
        this.apiVersion = StringUtils.defaultString(apiVersion, "");
        this.resource = StringUtils.defaultString(resource, "");
        this.subresource = StringUtils.defaultString(subresource, "");
        this.subName = StringUtils.defaultString(subName, "");
        this.name = StringUtils.defaultString(name, "");
        this.parts = Objects.requireNonNullElseGet(parts, () -> new String[] {});
    }
}
