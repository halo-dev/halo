package run.halo.app.security.authorization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.method;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import run.halo.app.extension.Metadata;

/**
 * Tests for {@link RequestInfoFactory}.
 *
 * @author guqing
 * @see RbacRequestEvaluation
 * @see RequestInfo
 * @see DefaultRuleResolver
 * @since 2.0.0
 */
public class RequestInfoResolverTest {

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
    }

    @Test
    public void defaultRuleResolverTest() {
        DefaultRuleResolver ruleResolver = new DefaultRuleResolver(name -> {
            // role getter
            Role role = new Role();
            List<PolicyRule> rules = List.of(
                new PolicyRule.Builder().apiGroups("").resources("posts").verbs("list", "get")
                    .build(),
                new PolicyRule.Builder().apiGroups("").resources("categories").verbs("*").build(),
                new PolicyRule.Builder().nonResourceURLs("/healthy").verbs("get", "post", "head")
                    .build());
            role.setRules(rules);
            Metadata metadata = new Metadata();
            metadata.setName("ruleReadPost");
            role.setMetadata(metadata);
            return role;
        });
        // list bound role names
        ruleResolver.setRoleBindingLister(
            (Collection<? extends GrantedAuthority> authorities) -> Set.of("ruleReadPost"));

        User user = new User("admin", "123456", AuthorityUtils.createAuthorityList("ruleReadPost"));

        // resolve user rules
        List<PolicyRule> rules = ruleResolver.rulesFor(user);
        assertThat(rules).isNotNull();

        RbacRequestEvaluation rbacRequestEvaluation = new RbacRequestEvaluation();
        for (RequestResolveCase requestResolveCase : getRequestResolveCases()) {
            var request =
                method(HttpMethod.valueOf(requestResolveCase.method),
                    requestResolveCase.url).build();
            RequestInfo requestInfo = RequestInfoFactory.INSTANCE.newRequestInfo(request);

            AttributesRecord attributes = new AttributesRecord(user, requestInfo);
            boolean allowed = rbacRequestEvaluation.rulesAllow(attributes, rules);
            assertThat(allowed).isEqualTo(requestResolveCase.expected);
        }
    }

    public record NonApiCase(String url, boolean expected) {
    }

    public record ErrorCases(String desc, String url) {
    }

    List<RequestResolveCase> getRequestResolveCases() {
        return List.of(new RequestResolveCase("/api/v1/tags", "GET", false),
            new RequestResolveCase("/api/v1/tags/tagName", "GET", false),

            new RequestResolveCase("/api/v1/categories/aName", "GET", true),
            new RequestResolveCase("/api/v1//categories", "POST", true),
            new RequestResolveCase("/api/v1/categories", "DELETE", true),
            new RequestResolveCase("/api/v1/posts", "GET", true),
            new RequestResolveCase("/api/v1/posts/aName", "GET", true),

            new RequestResolveCase("/api/v1/posts", "DELETE", false),
            new RequestResolveCase("/api/v1/posts/aName", "UPDATE", false),

            // group resource url
            new RequestResolveCase("/apis/group/v1/posts", "GET", false),

            // non resource url
            new RequestResolveCase("/healthy", "GET", true),
            new RequestResolveCase("/healthy", "POST", true),
            new RequestResolveCase("/healthy", "HEAD", true),
            new RequestResolveCase("//healthy", "GET", false),
            new RequestResolveCase("/healthy/name", "GET", false),
            new RequestResolveCase("/healthy1", "GET", false),

            new RequestResolveCase("//healthy//name", "GET", false),
            new RequestResolveCase("/", "GET", false));
    }

    public record RequestResolveCase(String url, String method, boolean expected) {
    }

    public record SuccessCase(String method, String url, String expectedVerb,
                              String expectedAPIPrefix, String expectedAPIGroup,
                              String expectedAPIVersion, String expectedNamespace,
                              String expectedResource, String expectedSubresource,
                              String expectedName, String[] expectedParts) {
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
            new SuccessCase("GET", "/api/v1/namespaces/other/posts?watch=1", "watch", "api", "",
                "v1", "other", "posts", "", "", new String[] {"posts"}),
            new SuccessCase("GET", "/api/v1/namespaces/other/posts?watch=0", "list", "api", "",
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
            new SuccessCase("DELETE", "/api/v1/nodes", "deletecollection", "api", "", "v1", "",
                "nodes", "", "", new String[] {"nodes"}),
            new SuccessCase("DELETE", "/api/v1/namespaces", "deletecollection", "api", "", "v1", "",
                "namespaces", "", "", new String[] {"namespaces"}),
            new SuccessCase("DELETE", "/api/v1/namespaces/other/posts", "deletecollection", "api",
                "", "v1", "other", "posts", "", "", new String[] {"posts"}),
            new SuccessCase("DELETE", "/apis/extensions/v1/namespaces/other/posts",
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
