package run.halo.app.security.authorization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.method;
import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.userdetails.User;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.Role.PolicyRule;
import run.halo.app.core.extension.service.RoleService;
import run.halo.app.extension.Metadata;

@ExtendWith(MockitoExtension.class)
class DefaultRuleResolverTest {

    @Mock
    RoleService roleService;

    @InjectMocks
    DefaultRuleResolver ruleResolver;

    @Test
    void visitRules() {
        when(roleService.listDependenciesFlux(Set.of("authenticated", "anonymous", "ruleReadPost")))
            .thenReturn(Flux.just(mockRole()));
        var fakeUser = new User("admin", "123456", createAuthorityList("ruleReadPost"));
        var cases = getRequestResolveCases();
        cases.forEach(requestResolveCase -> {
            var httpMethod = HttpMethod.valueOf(requestResolveCase.method);
            var request = method(httpMethod, requestResolveCase.url).build();
            var requestInfo = RequestInfoFactory.INSTANCE.newRequestInfo(request);
            StepVerifier.create(ruleResolver.visitRules(fakeUser, requestInfo))
                .assertNext(
                    visitor -> assertEquals(requestResolveCase.expected, visitor.isAllowed()))
                .verifyComplete();
        });

        verify(roleService, times(cases.size())).listDependenciesFlux(
            Set.of("authenticated", "anonymous", "ruleReadPost"));
    }

    Role mockRole() {
        var role = new Role();
        var rules = List.of(
            new PolicyRule.Builder().apiGroups("").resources("posts").verbs("list", "get").build(),
            new PolicyRule.Builder().apiGroups("").resources("categories").verbs("*").build(),
            new PolicyRule.Builder().apiGroups("api.plugin.halo.run")
                .resources("plugins/users")
                .resourceNames("foo/bar").verbs("*").build(),
            new PolicyRule.Builder().apiGroups("api.plugin.halo.run")
                .resources("plugins/users")
                .resourceNames("foo").verbs("*").build(),
            new PolicyRule.Builder().nonResourceURLs("/healthy").verbs("get", "post", "head")
                .build());
        role.setRules(rules);
        var metadata = new Metadata();
        metadata.setName("ruleReadPost");
        role.setMetadata(metadata);
        return role;
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

            // plugin custom resource url
            new RequestResolveCase("/apis/api.plugin.halo.run/v1alpha1/plugins/foo/users", "GET",
                true),
            new RequestResolveCase("/apis/api.plugin.halo.run/v1alpha1/plugins/foo/users/bar",
                "GET", true),
            new RequestResolveCase("/apis/api.plugin.halo.run/v1alpha1/plugins/foo/posts/bar",
                "GET", false),

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

    record RequestResolveCase(String url, String method, boolean expected) {
    }
}