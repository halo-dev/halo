package run.halo.app.security.authorization;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.reactive.ServerHttpRequest;

/**
 * Creates {@link RequestInfo} from {@link ServerHttpRequest}.
 *
 * @author guqing
 * @since 2.0.0
 */
public class RequestInfoFactory {
    public static final RequestInfoFactory INSTANCE =
        new RequestInfoFactory(Set.of("api", "apis"), Set.of("api"));

    /**
     * without leading and trailing slashes.
     */
    final Set<String> apiPrefixes;

    /**
     * without leading and trailing slashes.
     */
    final Set<String> grouplessApiPrefixes;

    /**
     * special verbs no subresources.
     */
    final Set<String> specialVerbs;

    public RequestInfoFactory(Set<String> apiPrefixes, Set<String> grouplessApiPrefixes) {
        this(apiPrefixes, grouplessApiPrefixes, Set.of("proxy", "watch"));
    }

    public RequestInfoFactory(Set<String> apiPrefixes, Set<String> grouplessApiPrefixes,
        Set<String> specialVerbs) {
        this.apiPrefixes = apiPrefixes;
        this.grouplessApiPrefixes = grouplessApiPrefixes;
        this.specialVerbs = specialVerbs;
    }

    /**
     * <p>newRequestInfo returns the information from the http request.  If error is not occurred,
     * RequestInfo holds the information as best it is known before the failure
     * It handles both resource and non-resource requests and fills in all the pertinent
     * information.</p>
     * <p>for each.</p>
     * Valid Inputs:
     * <p>Resource paths</p>
     * <pre>
     * /api/{version}/plugins
     * /api/{version}/plugins/{pluginName}
     * /api/{version}/plugins/{pluginName}/{resource}
     * /api/{version}/plugins/{pluginName}/{resource}/{resourceName}
     * /api/{version}/{resource}
     * /api/{version}/{resource}/{resourceName}
     * </pre>
     * <p>Special verbs without subresources:</p>
     * <pre>
     * /api/{version}/proxy/{resource}/{resourceName}
     * /api/{version}/proxy/namespaces/{namespace}/{resource}/{resourceName}
     * </pre>
     *
     * <p>Special verbs with subresources:</p>
     * <pre>
     * /api/{version}/watch/{resource}
     * /api/{version}/watch/namespaces/{namespace}/{resource}
     * </pre>
     *
     * <p>NonResource paths:</p>
     * <pre>
     * /apis/{api-group}/{version}
     * /apis/{api-group}
     * /apis
     * /api/{version}
     * /api
     * /healthz
     * </pre>
     *
     * @param request http request
     * @return request holds the information of both resource and non-resource requests
     */
    public RequestInfo newRequestInfo(ServerHttpRequest request) {
        // non-resource request default
        PathContainer path = request.getPath().pathWithinApplication();
        RequestInfo requestInfo =
            new RequestInfo(false, path.value(), request.getMethod().name().toLowerCase());

        String[] currentParts = splitPath(request.getPath().value());

        if (currentParts.length < 3) {
            // return a non-resource request
            return requestInfo;
        }

        if (!apiPrefixes.contains(currentParts[0])) {
            // return a non-resource request
            return requestInfo;
        }
        requestInfo.apiPrefix = currentParts[0];
        currentParts = Arrays.copyOfRange(currentParts, 1, currentParts.length);

        if (!grouplessApiPrefixes.contains(requestInfo.apiPrefix)) {
            // one part (APIPrefix) has already been consumed, so this is actually "do we have
            // four parts?"
            if (currentParts.length < 3) {
                // return a non-resource request
                return requestInfo;
            }

            requestInfo.apiGroup = StringUtils.defaultString(currentParts[0], "");
            currentParts = Arrays.copyOfRange(currentParts, 1, currentParts.length);
        }
        requestInfo.isResourceRequest = true;
        requestInfo.apiVersion = currentParts[0];
        currentParts = Arrays.copyOfRange(currentParts, 1, currentParts.length);
        // handle input of form /{specialVerb}/*
        Set<String> specialVerbs = Set.of("proxy", "watch");
        if (specialVerbs.contains(currentParts[0])) {
            if (currentParts.length < 2) {
                throw new IllegalArgumentException(
                    String.format("unable to determine kind and namespace from url, %s",
                        request.getPath()));
            }
            requestInfo.verb = currentParts[0];
            currentParts = Arrays.copyOfRange(currentParts, 1, currentParts.length);
        } else {
            requestInfo.verb = switch (request.getMethod().name().toUpperCase()) {
                case "POST" -> "create";
                case "GET", "HEAD" -> "get";
                case "PUT" -> "update";
                case "PATCH" -> "patch";
                case "DELETE" -> "delete";
                default -> "";
            };
        }
        // URL forms: /plugins/{plugin-name}/{kind}/*, where parts are adjusted to be relative
        // to kind
        if (Objects.equals(currentParts[0], "plugins")
            && StringUtils.isEmpty(requestInfo.getApiGroup())) {
            if (currentParts.length > 1) {
                requestInfo.pluginName = currentParts[1];

                // if there is another step after the plugin name and it is not a known
                // plugins subresource
                // move currentParts to include it as a resource in its own right
                if (currentParts.length > 2) {
                    currentParts = Arrays.copyOfRange(currentParts, 2, currentParts.length);
                }
            }
        } else {
            requestInfo.pluginName = "";
        }

        // parsing successful, so we now know the proper value for .Parts
        requestInfo.parts = currentParts;
        // special verbs no subresources
        // parts look like: resource/resourceName/subresource/other/stuff/we/don't/interpret
        if (requestInfo.parts.length >= 3 && !specialVerbs.contains(
            requestInfo.verb)) {
            requestInfo.subresource = requestInfo.parts[2];
        }

        if (requestInfo.parts.length >= 2) {
            requestInfo.name = requestInfo.parts[1];
        }

        if (requestInfo.parts.length >= 1) {
            requestInfo.resource = requestInfo.parts[0];
        }

        // if there's no name on the request and we thought it was a get before, then the actual
        // verb is a list or a watch
        if (requestInfo.name.length() == 0 && "get".equals(requestInfo.verb)) {
            var watch = request.getQueryParams().getFirst("watch");
            if (isWatch(watch)) {
                requestInfo.verb = "watch";
            } else {
                requestInfo.verb = "list";
            }
        }
        // if there's no name on the request and we thought it was a deleted before, then the
        // actual verb is deletecollection
        if (requestInfo.name.length() == 0
            && Objects.equals(requestInfo.verb, "delete")) {
            requestInfo.verb = "deletecollection";
        }
        return requestInfo;
    }

    boolean isWatch(String requestParam) {
        return "1".equals(requestParam) || "true".equals(requestParam);
    }

    private String[] splitPath(String path) {
        path = StringUtils.strip(path, "/");
        if (StringUtils.isEmpty(path)) {
            return new String[] {};
        }
        return StringUtils.split(path, "/");
    }
}
