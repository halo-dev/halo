package run.halo.app.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import run.halo.app.cache.AbstractStringCacheStore;
import run.halo.app.controller.content.auth.CategoryAuthentication;
import run.halo.app.exception.NotFoundException;
import run.halo.app.model.entity.Category;
import run.halo.app.service.CategoryService;

/**
 * @author guqing
 * @date 2022-04-01
 */
@SpringBootTest
@RecordApplicationEvents
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class CategoryIntegrationTest {

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private CategoryAuthentication categoryAuthentication;
    @Autowired
    private AbstractStringCacheStore abstractStringCacheStore;

    @BeforeEach
    public void setUp() {
        Category category1 = new Category();
        category1.setId(1);
        category1.setName("category-1");
        category1.setSlug("category-1");
        category1.setPassword("123");
        category1.setParentId(0);

        Category category2 = new Category();
        category2.setId(2);
        category2.setName("category-2");
        category2.setSlug("category-2");
        category2.setParentId(1);

        categoryService.create(category1);
        categoryService.create(category2);
    }

    @Test
    public void authenticationAfterRemove() {
        Category category = categoryService.getById(1);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestedSessionId("mock_session_id");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        boolean authenticated = categoryAuthentication.isAuthenticated(category.getId());
        assertThat(authenticated).isFalse();

        // set authenticated flag is true for No.2 category
        categoryAuthentication.setAuthenticated(2, true);
        assertThat(categoryAuthentication.isAuthenticated(2)).isTrue();

        // set authenticated flag is true for No.1 category
        categoryAuthentication.setAuthenticated(category.getId(), true);
        assertThat(categoryAuthentication.isAuthenticated(category.getId())).isTrue();

        // remove category and category-post relationships
        categoryService.removeCategoryAndPostCategoryBy(category.getId());
        // authenticated cache flag is cleared by authentication manager
        String authenticationCacheKey =
            categoryAuthentication.buildCacheKey(request.getRequestedSessionId(),
                categoryAuthentication.getPrincipal().toString(), String.valueOf(category.getId()));
        assertThat(abstractStringCacheStore.get(authenticationCacheKey)).isEmpty();

        // isAuthenticated method will be throw NotFoundException because of category has been
        // removed.
        assertThatThrownBy(() -> categoryAuthentication.isAuthenticated(category.getId()))
            .isInstanceOf(NotFoundException.class);

        // bug other cache flags will not be affected
        assertThat(categoryAuthentication.isAuthenticated(2)).isTrue();
    }
}
