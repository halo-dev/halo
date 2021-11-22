package run.halo.app.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import run.halo.app.cache.InMemoryCacheStore;
import run.halo.app.utils.JsonUtils;

/**
 * @author guqing
 * @date 2021-11-19
 */
public class AuthorizationServiceImplTest {

    private AuthorizationServiceImpl authorizationService;
    private InMemoryCacheStore inMemoryCacheStore;

    @BeforeEach
    public void setUp() {
        inMemoryCacheStore = new InMemoryCacheStore();
        authorizationService = new AuthorizationServiceImpl(inMemoryCacheStore);
    }

    @Test
    public void deletePostAuthorizationTest() {
        inMemoryCacheStore.clear();
        RequestContextHolder.setRequestAttributes(mockRequestAttributes("1"));

        authorizationService.postAuthorization(1);
        authorizationService.postAuthorization(2);

        Set<String> permissions = authorizationService.getAccessPermissionStore();
        assertEquals("[POST:1, POST:2]", permissions.toString());

        authorizationService.deletePostAuthorization(1);
        Set<String> permissionsAfterDelete = authorizationService.getAccessPermissionStore();
        assertEquals("[POST:2]", permissionsAfterDelete.toString());

        RequestContextHolder.resetRequestAttributes();
        inMemoryCacheStore.clear();
    }

    @Test
    public void complexityOfDeletePostAuthorizationTest() {
        inMemoryCacheStore.clear();
        // simulate session of user 1
        RequestContextHolder.setRequestAttributes(mockRequestAttributes("1"));
        // user 1 accessed two encrypted posts
        authorizationService.postAuthorization(1);
        authorizationService.postAuthorization(2);

        // simulate session of user 2
        RequestContextHolder.setRequestAttributes(mockRequestAttributes("2"));

        // user 2 accessed two encrypted posts
        authorizationService.postAuthorization(2);
        authorizationService.postAuthorization(3);

        assertEquals(objectToJson(inMemoryCacheStore.toMap()),
            "{\"ACCESS_PERMISSION: 2\":\"[\\\"POST:3\\\",\\\"POST:2\\\"]\","
                + "\"ACCESS_PERMISSION: 1\":\"[\\\"POST:1\\\",\\\"POST:2\\\"]\"}");

        // simulate the admin user to change the post password
        authorizationService.deletePostAuthorization(2);

        assertEquals(objectToJson(inMemoryCacheStore.toMap()),
            "{\"ACCESS_PERMISSION: 2\":\"[\\\"POST:3\\\"]\","
                + "\"ACCESS_PERMISSION: 1\":\"[\\\"POST:1\\\"]\"}");

        RequestContextHolder.resetRequestAttributes();
        inMemoryCacheStore.clear();
    }

    @Test
    public void deleteCategoryAuthorizationTest() {
        inMemoryCacheStore.clear();
        // simulate session of user 1
        RequestContextHolder.setRequestAttributes(mockRequestAttributes("1"));
        // user 1 accessed two encrypted posts
        authorizationService.categoryAuthorization(1);
        authorizationService.categoryAuthorization(2);

        // simulate session of user 2
        RequestContextHolder.setRequestAttributes(mockRequestAttributes("2"));
        // user 2 accessed two encrypted categories
        authorizationService.categoryAuthorization(1);
        authorizationService.categoryAuthorization(3);

        assertEquals(objectToJson(inMemoryCacheStore.toMap()),
            "{\"ACCESS_PERMISSION: 2\":\"[\\\"CATEGORY:1\\\",\\\"CATEGORY:3\\\"]\","
                + "\"ACCESS_PERMISSION: 1\":\"[\\\"CATEGORY:1\\\",\\\"CATEGORY:2\\\"]\"}");

        // simulate the admin user to change the category password of No.1
        authorizationService.deleteCategoryAuthorization(1);

        assertEquals(objectToJson(inMemoryCacheStore.toMap()),
            "{\"ACCESS_PERMISSION: 2\":\"[\\\"CATEGORY:3\\\"]\","
                + "\"ACCESS_PERMISSION: 1\":\"[\\\"CATEGORY:2\\\"]\"}");

        RequestContextHolder.resetRequestAttributes();
        inMemoryCacheStore.clear();
    }

    private ServletRequestAttributes mockRequestAttributes(String sessionId) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockServletContext context = new MockServletContext();
        MockHttpSession session = new MockHttpSession(context, sessionId);
        request.setSession(session);
        return new ServletRequestAttributes(request);
    }

    private String objectToJson(Object o) {
        try {
            return JsonUtils.objectToJson(o);
        } catch (JsonProcessingException e) {
            // ignore this
        }
        return StringUtils.EMPTY;
    }
}
