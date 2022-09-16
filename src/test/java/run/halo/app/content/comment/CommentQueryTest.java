package run.halo.app.content.comment;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Tests for {@link CommentQuery}.
 *
 * @author guqing
 * @since 2.0.0
 */
class CommentQueryTest {

    @Test
    void getKeyword() {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("keyword", "test");
        CommentQuery commentQuery = new CommentQuery(queryParams);
        assertThat(commentQuery.getKeyword()).isEqualTo("test");
        queryParams.clear();

        queryParams.add("keyword", "");
        assertThat(commentQuery.getKeyword()).isNull();
        queryParams.clear();

        queryParams.add("keyword", null);
        assertThat(commentQuery.getKeyword()).isNull();
        queryParams.clear();
    }

    @Test
    void getApproved() {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("approved", "true");
        CommentQuery commentQuery = new CommentQuery(queryParams);
        assertThat(commentQuery.getApproved()).isTrue();
        queryParams.clear();

        queryParams.add("approved", "");
        assertThat(commentQuery.getApproved()).isNull();
        queryParams.clear();

        queryParams.add("approved", "1");
        assertThat(commentQuery.getApproved()).isFalse();
        queryParams.clear();
    }

    @Test
    void getHidden() {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("hidden", "true");
        CommentQuery commentQuery = new CommentQuery(queryParams);
        assertThat(commentQuery.getHidden()).isTrue();
        queryParams.clear();

        queryParams.add("hidden", "");
        assertThat(commentQuery.getHidden()).isNull();
        queryParams.clear();

        queryParams.add("hidden", "1");
        assertThat(commentQuery.getHidden()).isFalse();
        queryParams.clear();
    }

    @Test
    void getAllowNotification() {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("allowNotification", "true");
        CommentQuery commentQuery = new CommentQuery(queryParams);
        assertThat(commentQuery.getAllowNotification()).isTrue();
        queryParams.clear();

        queryParams.add("allowNotification", "");
        assertThat(commentQuery.getAllowNotification()).isNull();
        queryParams.clear();

        queryParams.add("allowNotification", "1");
        assertThat(commentQuery.getAllowNotification()).isFalse();
        queryParams.clear();
    }

    @Test
    void getTop() {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("top", "true");
        CommentQuery commentQuery = new CommentQuery(queryParams);
        assertThat(commentQuery.getTop()).isTrue();
        queryParams.clear();

        queryParams.add("top", "");
        assertThat(commentQuery.getTop()).isNull();
        queryParams.clear();

        queryParams.add("top", "1");
        assertThat(commentQuery.getTop()).isFalse();
        queryParams.clear();
    }

    @Test
    void getOwnerKind() {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("ownerKind", "test-owner-kind");
        CommentQuery commentQuery = new CommentQuery(queryParams);
        assertThat(commentQuery.getOwnerKind()).isEqualTo("test-owner-kind");
        queryParams.clear();

        queryParams.add("ownerKind", "");
        assertThat(commentQuery.getOwnerKind()).isNull();
        queryParams.clear();

        queryParams.add("ownerKind", null);
        assertThat(commentQuery.getOwnerKind()).isNull();
        queryParams.clear();
    }

    @Test
    void getOwnerName() {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("ownerName", "test-owner-name");
        CommentQuery commentQuery = new CommentQuery(queryParams);
        assertThat(commentQuery.getOwnerName()).isEqualTo("test-owner-name");
        queryParams.clear();

        queryParams.add("ownerName", "");
        assertThat(commentQuery.getOwnerName()).isNull();
        queryParams.clear();

        queryParams.add("ownerName", null);
        assertThat(commentQuery.getOwnerName()).isNull();
        queryParams.clear();
    }

    @Test
    void getSubjectKind() {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("subjectKind", "test-subject-kind");
        CommentQuery commentQuery = new CommentQuery(queryParams);
        assertThat(commentQuery.getSubjectKind()).isEqualTo("test-subject-kind");
        queryParams.clear();

        queryParams.add("subjectKind", "");
        assertThat(commentQuery.getSubjectKind()).isNull();
        queryParams.clear();

        queryParams.add("subjectKind", null);
        assertThat(commentQuery.getSubjectKind()).isNull();
        queryParams.clear();
    }

    @Test
    void getSubjectName() {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("subjectName", "test-subject-name");
        CommentQuery commentQuery = new CommentQuery(queryParams);
        assertThat(commentQuery.getSubjectName()).isEqualTo("test-subject-name");
        queryParams.clear();

        queryParams.add("subjectName", "");
        assertThat(commentQuery.getSubjectName()).isNull();
        queryParams.clear();

        queryParams.add("subjectName", null);
        assertThat(commentQuery.getSubjectName()).isNull();
        queryParams.clear();
    }

    @Test
    void getSort() {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("sort", CommentSorter.REPLY_COUNT.name());
        CommentQuery commentQuery = new CommentQuery(queryParams);
        assertThat(commentQuery.getSort()).isEqualTo(CommentSorter.REPLY_COUNT);
        queryParams.clear();

        queryParams.add("sort", "");
        assertThat(commentQuery.getSort()).isNull();
        queryParams.clear();

        queryParams.add("sort", "nothing");
        assertThat(commentQuery.getSort()).isNull();
        queryParams.clear();
    }

    @Test
    void getSortOrder() {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("sortOrder", "true");
        CommentQuery commentQuery = new CommentQuery(queryParams);
        assertThat(commentQuery.getSortOrder()).isTrue();
        queryParams.clear();

        queryParams.add("sortOrder", "");
        assertThat(commentQuery.getSortOrder()).isNull();
        queryParams.clear();

        queryParams.add("sortOrder", null);
        assertThat(commentQuery.getSortOrder()).isNull();
        queryParams.clear();
    }
}