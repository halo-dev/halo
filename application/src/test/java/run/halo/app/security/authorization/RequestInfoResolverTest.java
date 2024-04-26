package run.halo.app.security.authorization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.method;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

/**
 * Tests for {@link RequestInfoFactory}.
 *
 * @author guqing
 * @see RequestInfo
 * @since 2.0.0
 */
public class RequestInfoResolverTest {

    @Test
    void shouldResolveAsWatchRequestWhenRequestIsWebSocket() {
        var request = method(HttpMethod.GET, "/apis/fake.halo.run/v1alpha1/fakes")
            .header("Upgrade", "websocket")
            .header("Connection", "Upgrade")
            .build();
        RequestInfo requestInfo = RequestInfoFactory.INSTANCE.newRequestInfo(request);
        assertThat(requestInfo).isNotNull();
        assertThat(requestInfo.getVerb()).isEqualTo("watch");
    }

    @Test
    void shouldNotResolveAsWatchRequestWhenRequestIsNotWebSocket() {
        var request = method(HttpMethod.GET, "/apis/fake.halo.run/v1alpha1/fakes")
            .header("Upgrade", "websocket")
            .build();
        RequestInfo requestInfo = RequestInfoFactory.INSTANCE.newRequestInfo(request);
        assertThat(requestInfo).isNotNull();
        assertThat(requestInfo.getVerb()).isEqualTo("list");
    }

    @Test
    public void requestInfoTest() {
        for (SuccessCase successCase : getTestRequestInfos()) {
            final var request = method(HttpMethod.valueOf(successCase.method), successCase.url)
                .build();

            RequestInfo requestInfo = RequestInfoFactory.INSTANCE.newRequestInfo(request);

            assertNotNull(requestInfo, successCase::toString);
            assertEquals(successCase.expectedVerb, requestInfo.getVerb(), successCase::toString);
            assertThat(requestInfo.getApiPrefix()).isEqualTo(successCase.expectedAPIPrefix);
            assertThat(requestInfo.getApiGroup()).isEqualTo(successCase.expectedAPIGroup);
            assertThat(requestInfo.getApiVersion()).isEqualTo(successCase.expectedAPIVersion);
            assertThat(requestInfo.getResource()).isEqualTo(successCase.expectedResource);
            assertThat(requestInfo.getSubresource()).isEqualTo(successCase.expectedSubresource);
            assertThat(requestInfo.getName()).isEqualTo(successCase.expectedName);
            assertThat(requestInfo.getParts()).isEqualTo(successCase.expectedParts);
        }
    }

    @Test
    public void nonApiRequestInfoTest() {
        Map<String, NonApiCase> map = new HashMap<>();
        map.put("simple groupless", new NonApiCase("/api/version/resource", true));
        map.put("simple group",
            new NonApiCase("/apis/group/version/resource/name/subresource", true));
        map.put("more steps", new NonApiCase("/api/version/resource/name/subresource", true));
        map.put("group list", new NonApiCase("/apis/batch/v1/job", true));
        map.put("group get", new NonApiCase("/apis/batch/v1/job/foo", true));
        map.put("group subresource", new NonApiCase("/apis/batch/v1/job/foo/scale", true));

        // bad case
        map.put("bad root", new NonApiCase("/not-api/version/resource", false));
        map.put("group without enough steps", new NonApiCase("/apis/extensions/v1beta1", false));
        map.put("group without enough steps 2", new NonApiCase("/apis/extensions/v1beta1/", false));
        map.put("not enough steps", new NonApiCase("/api/version", false));
        map.put("one step", new NonApiCase("/api", false));
        map.put("zero step", new NonApiCase("/", false));
        map.put("empty", new NonApiCase("", false));

        map.forEach((k, v) -> {
            var request = method(HttpMethod.GET, v.url).build();
            RequestInfo requestInfo = RequestInfoFactory.INSTANCE.newRequestInfo(request);
            if (requestInfo.isResourceRequest() != v.expected) {
                throw new RuntimeException(
                    String.format("%s: expected %s, actual %s", k, v.expected,
                        requestInfo.isResourceRequest()));
            }
        });
    }

    @Test
    void pluginsScopedAndPluginManage() {
        List<CustomSuccessCase> testCases =
            List.of(
                new CustomSuccessCase("DELETE", "/apis/api.plugin.halo.run/v1/plugins/other/posts",
                    "delete", "apis", "api.plugin.halo.run", "v1", "", "plugins", "posts", "", "",
                    new String[] {"plugins", "other", "posts"}),

                // api group identification
                new CustomSuccessCase("POST",
                    "/apis/api.plugin.halo.run/v1/plugins/other/posts/foo",
                    "create", "apis",
                    "api.plugin.halo.run", "v1", "", "plugins", "posts", "other", "foo",
                    new String[] {"plugins", "other", "posts", "foo"}),

                // api version identification
                new CustomSuccessCase("POST",
                    "/apis/api.plugin.halo.run/v1beta3/plugins/other/posts/bar", "create",
                    "apis", "api.plugin.halo.run", "v1beta3", "", "plugins", "posts", "other",
                    "bar",
                    new String[] {"plugins", "other", "posts", "bar"}));

        // 以 /apis 开头的 plugins 资源为 core 中管理插件使用的资源
        for (CustomSuccessCase successCase : testCases) {
            var request =
                method(HttpMethod.valueOf(successCase.method),
                    successCase.url).build();
            RequestInfo requestInfo = RequestInfoFactory.INSTANCE.newRequestInfo(request);
            assertThat(requestInfo).isNotNull();
            assertRequestInfoCase(successCase, requestInfo);
        }

        List<CustomSuccessCase> pluginScopedCases =
            List.of(
                new CustomSuccessCase("DELETE", "/apis/api.plugin.halo.run/v1/plugins/other/posts",
                    "delete", "apis", "api.plugin.halo.run", "v1", "", "plugins", "posts",
                    "other", "", new String[] {"plugins", "other", "posts"}),

                // api group identification
                new CustomSuccessCase("POST",
                    "/apis/api.plugin.halo.run/v1/plugins/other/posts/some-name", "create", "apis",
                    "api.plugin.halo.run", "v1", "other", "plugins", "posts", "other", "some-name",
                    new String[] {"plugins", "other", "posts", "some-name"}));

        for (CustomSuccessCase pluginScopedCase : pluginScopedCases) {
            var request =
                method(HttpMethod.valueOf(pluginScopedCase.method),
                    pluginScopedCase.url).build();
            RequestInfo requestInfo = RequestInfoFactory.INSTANCE.newRequestInfo(request);
            assertThat(requestInfo).isNotNull();
            assertRequestInfoCase(pluginScopedCase, requestInfo);
        }
    }

    private void assertRequestInfoCase(CustomSuccessCase pluginScopedCase,
        RequestInfo requestInfo) {
        assertThat(requestInfo.getVerb()).isEqualTo(pluginScopedCase.expectedVerb);
        assertThat(requestInfo.getParts()).isEqualTo(pluginScopedCase.expectedParts);
        assertThat(requestInfo.getApiGroup()).isEqualTo(pluginScopedCase.expectedAPIGroup);
        assertThat(requestInfo.getResource()).isEqualTo(pluginScopedCase.expectedResource);
        assertThat(requestInfo.getSubresource())
            .isEqualTo(pluginScopedCase.expectedSubresource());
        assertThat(requestInfo.getSubName())
            .isEqualTo(pluginScopedCase.expectedSubName());
    }

    @Test
    public void errorCaseTest() {
        List<ErrorCases> errorCases = List.of(new ErrorCases("no resource path", "/"),
            new ErrorCases("just apiversion", "/api/version/"),
            new ErrorCases("just prefix, group, version", "/apis/group/version/"),
            new ErrorCases("apiversion with no resource", "/api/version/"),
            new ErrorCases("bad prefix", "/badprefix/version/resource"),
            new ErrorCases("missing api group", "/apis/version/resource"));
        for (ErrorCases errorCase : errorCases) {
            var request =
                method(HttpMethod.GET, errorCase.url).build();
            RequestInfo apiRequestInfo = RequestInfoFactory.INSTANCE.newRequestInfo(request);
            if (apiRequestInfo.isResourceRequest()) {
                throw new RuntimeException(
                    String.format("%s: expected non-resource request", errorCase.desc));
            }
        }

        List<ErrorCases> postCases =
            List.of(new ErrorCases("api resource has name and no subresource but post",
                    "/api/version/themes/install"),
                new ErrorCases("apis resource has name and no subresource but post",
                    "/apis/api.halo.run/v1alpha1/themes/install"));
        for (ErrorCases errorCase : postCases) {
            var request =
                method(HttpMethod.POST, errorCase.url).build();
            RequestInfo apiRequestInfo = RequestInfoFactory.INSTANCE.newRequestInfo(request);
            if (apiRequestInfo.isResourceRequest()) {
                throw new RuntimeException(
                    String.format("%s: expected non-resource request", errorCase.desc));
            }
        }
    }

    public record NonApiCase(String url, boolean expected) {
    }

    public record ErrorCases(String desc, String url) {
    }


    public record SuccessCase(String method, String url, String expectedVerb,
                              String expectedAPIPrefix, String expectedAPIGroup,
                              String expectedAPIVersion, String expectedNamespace,
                              String expectedResource, String expectedSubresource,
                              String expectedName, String[] expectedParts) {
    }

    public record CustomSuccessCase(String method, String url, String expectedVerb,
                                    String expectedAPIPrefix, String expectedAPIGroup,
                                    String expectedAPIVersion, String expectedNamespace,
                                    String expectedResource, String expectedSubresource,
                                    String expectedName, String expectedSubName,
                                    String[] expectedParts) {
    }

    List<SuccessCase> getTestRequestInfos() {
        String namespaceAll = "*";
        return List.of(
            new SuccessCase("GET", "/api/v1/namespaces", "list", "api", "", "v1", "", "namespaces",
                "", "", new String[] {"namespaces"}),
            new SuccessCase("GET", "/api/v1/namespaces/other", "get", "api", "", "v1", "other",
                "namespaces", "", "other", new String[] {"namespaces", "other"}),

            new SuccessCase("GET", "/api/v1/namespaces/other/posts", "list", "api", "", "v1",
                "other", "posts", "", "", new String[] {"posts"}),
            new SuccessCase("GET", "/api/v1/namespaces/other/posts/foo", "get", "api", "", "v1",
                "other", "posts", "", "foo", new String[] {"posts", "foo"}),
            new SuccessCase("HEAD", "/api/v1/namespaces/other/posts/foo", "get", "api", "", "v1",
                "other", "posts", "", "foo", new String[] {"posts", "foo"}),
            new SuccessCase("GET", "/api/v1/posts", "list", "api", "", "v1", namespaceAll, "posts",
                "", "", new String[] {"posts"}),
            new SuccessCase("HEAD", "/api/v1/posts", "list", "api", "", "v1", namespaceAll, "posts",
                "", "", new String[] {"posts"}),
            new SuccessCase("GET", "/api/v1/namespaces/other/posts/foo", "get", "api", "", "v1",
                "other", "posts", "", "foo", new String[] {"posts", "foo"}),
            new SuccessCase("GET", "/api/v1/namespaces/other/posts", "list", "api", "", "v1",
                "other", "posts", "", "", new String[] {"posts"}),

            // special verbs
            new SuccessCase("GET", "/api/v1/proxy/namespaces/other/posts/foo", "proxy", "api", "",
                "v1", "other", "posts", "", "foo", new String[] {"posts", "foo"}),
            new SuccessCase("GET",
                "/api/v1/proxy/namespaces/other/posts/foo/subpath/not/a/subresource", "proxy",
                "api", "", "v1", "other", "posts", "", "foo",
                new String[] {"posts", "foo", "subpath", "not", "a", "subresource"}),
            new SuccessCase("GET", "/api/v1/watch/posts", "watch", "api", "", "v1", namespaceAll,
                "posts", "", "", new String[] {"posts"}),
            new SuccessCase("GET", "/api/v1/posts?watch=true", "watch", "api", "", "v1",
                namespaceAll, "posts", "", "", new String[] {"posts"}),
            new SuccessCase("GET", "/api/v1/posts?watch=false", "list", "api", "", "v1",
                namespaceAll, "posts", "", "", new String[] {"posts"}),
            new SuccessCase("GET", "/api/v1/watch/namespaces/other/posts", "watch", "api", "", "v1",
                "other", "posts", "", "", new String[] {"posts"}),
            new SuccessCase("GET", "/api/v1/namespaces/other/posts?watch=true", "watch", "api", "",
                "v1", "other", "posts", "", "", new String[] {"posts"}),
            new SuccessCase("GET", "/api/v1/namespaces/other/posts?watch=false", "list", "api", "",
                "v1", "other", "posts", "", "", new String[] {"posts"}),

            // subresource identification
            new SuccessCase("GET", "/api/v1/namespaces/other/posts/foo/status", "get", "api", "",
                "v1", "other", "posts", "status", "foo", new String[] {"posts", "foo", "status"}),
            new SuccessCase("GET", "/api/v1/namespaces/other/posts/foo/proxy/subpath", "get", "api",
                "", "v1", "other", "posts", "proxy", "foo",
                new String[] {"posts", "foo", "proxy", "subpath"}),
            new SuccessCase("PUT", "/api/v1/namespaces/other/finalize", "update", "api", "", "v1",
                "other", "namespaces", "finalize", "other",
                new String[] {"namespaces", "other", "finalize"}),
            new SuccessCase("PUT", "/api/v1/namespaces/other/status", "update", "api", "", "v1",
                "other", "namespaces", "status", "other",
                new String[] {"namespaces", "other", "status"}),

            // verb identification
            new SuccessCase("PATCH", "/api/v1/namespaces/other/posts/foo", "patch", "api", "", "v1",
                "other", "posts", "", "foo", new String[] {"posts", "foo"}),
            new SuccessCase("DELETE", "/api/v1/namespaces/other/posts/foo", "delete", "api", "",
                "v1", "other", "posts", "", "foo", new String[] {"posts", "foo"}),
            new SuccessCase("POST", "/api/v1/namespaces/other/posts", "create", "api", "", "v1",
                "other", "posts", "", "", new String[] {"posts"}),

            // deletecollection verb identification
            new SuccessCase("DELETE", "/api/v1/nodes?all=true", "deletecollection", "api", "", "v1",
                "",
                "nodes", "", "", new String[] {"nodes"}),
            new SuccessCase("DELETE", "/api/v1/namespaces?all=false", "delete", "api", "",
                "v1", "",
                "namespaces", "", "", new String[] {"namespaces"}),
            new SuccessCase("DELETE", "/api/v1/namespaces/other/posts?all=true", "deletecollection",
                "api",
                "", "v1", "other", "posts", "", "", new String[] {"posts"}),
            new SuccessCase("DELETE", "/apis/extensions/v1/namespaces/other/posts?all=true",
                "deletecollection", "apis", "extensions", "v1", "other", "posts", "", "",
                new String[] {"posts"}),

            // api group identification
            new SuccessCase("POST", "/apis/extensions/v1/namespaces/other/posts", "create", "apis",
                "extensions", "v1", "other", "posts", "", "", new String[] {"posts"}),

            // api version identification
            new SuccessCase("POST", "/apis/extensions/v1beta3/namespaces/other/posts", "create",
                "apis", "extensions", "v1beta3", "other", "posts", "", "", new String[] {"posts"}));
    }

}
