package run.halo.app.controller.content;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import run.halo.app.cache.InMemoryCacheStore;
import run.halo.app.controller.content.auth.CategoryAuthentication;
import run.halo.app.model.entity.Category;
import run.halo.app.model.enums.EncryptTypeEnum;
import run.halo.app.service.CategoryService;

/**
 * @author guqing
 * @date 2022-02-25
 */
@ExtendWith(SpringExtension.class)
public class CategoryAuthenticationTest {

    private CategoryAuthentication categoryAuthentication;

    @MockBean
    private CategoryService categoryService;

    private final InMemoryCacheStore inMemoryCacheStore = new InMemoryCacheStore();

    @BeforeEach
    public void setUp() {
        categoryAuthentication = new CategoryAuthentication(categoryService, inMemoryCacheStore);

        Category category = new Category();
        category.setId(1);
        category.setSlug("slug-1");
        category.setName("name-1");
        category.setDescription("description-1");
        category.setPassword("123");
        when(categoryService.getById(1)).thenReturn(category);
    }

    @Test
    public void isAuthenticated() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestedSessionId("mock_session_id");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        boolean authenticated = categoryAuthentication.isAuthenticated(1);
        assertThat(authenticated).isFalse();

        categoryAuthentication.setAuthenticated(1, true);
        assertThat(categoryAuthentication.isAuthenticated(1)).isTrue();

        categoryAuthentication.clearByResourceId(1);
        assertThat(categoryAuthentication.isAuthenticated(1)).isFalse();
    }

    @Test
    public void buildCacheKeyTest() {
        String cacheKey = categoryAuthentication.buildCacheKey("mock_session_id",
            EncryptTypeEnum.CATEGORY.getName(), "1");
        assertThat(cacheKey).isEqualTo("CONTENT_AUTHENTICATED:category:1:mock_session_id");
    }

    @Test
    public void getSessionIdTest() {
        String sessionId = categoryAuthentication.getSessionId();
        assertThat(sessionId).isEqualTo("");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestedSessionId("mock_session_id");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        assertThat(categoryAuthentication.getSessionId()).isEqualTo("mock_session_id");
    }
}
