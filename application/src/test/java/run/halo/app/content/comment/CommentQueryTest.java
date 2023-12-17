package run.halo.app.content.comment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.NonNull;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.extension.Metadata;

/**
 * Tests for {@link CommentQuery}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class CommentQueryTest {

    @Test
    void getKeyword() {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("keyword", "test");
        CommentQuery commentQuery = getCommentQuery(queryParams);
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
        CommentQuery commentQuery = getCommentQuery(queryParams);
        assertThat(commentQuery.getApproved()).isTrue();
        queryParams.clear();

        queryParams.add("approved", "");
        assertThat(commentQuery.getApproved()).isNull();
        queryParams.clear();

        queryParams.add("approved", "1");
        assertThat(commentQuery.getApproved()).isFalse();
        queryParams.clear();
    }

    @NonNull
    private CommentQuery getCommentQuery(MultiValueMap<String, String> queryParams) {
        ServerWebExchange exchange = mock(ServerWebExchange.class);
        MockServerRequest request = MockServerRequest.builder()
            .queryParams(queryParams)
            .exchange(exchange)
            .build();
        ServerHttpRequest httpRequest = mock(ServerHttpRequest.class);
        lenient().when(exchange.getRequest()).thenReturn(httpRequest);
        lenient().when(httpRequest.getQueryParams()).thenReturn(queryParams);
        return new CommentQuery(request);
    }

    @Test
    void getHidden() {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("hidden", "true");
        CommentQuery commentQuery = getCommentQuery(queryParams);
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
        CommentQuery commentQuery = getCommentQuery(queryParams);
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
        CommentQuery commentQuery = getCommentQuery(queryParams);
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
        CommentQuery commentQuery = getCommentQuery(queryParams);
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
        CommentQuery commentQuery = getCommentQuery(queryParams);
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
        CommentQuery commentQuery = getCommentQuery(queryParams);
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
        CommentQuery commentQuery = getCommentQuery(queryParams);
        assertThat(commentQuery.getSubjectName()).isEqualTo("test-subject-name");
        queryParams.clear();

        queryParams.add("subjectName", "");
        assertThat(commentQuery.getSubjectName()).isNull();
        queryParams.clear();

        queryParams.add("subjectName", null);
        assertThat(commentQuery.getSubjectName()).isNull();
        queryParams.clear();
    }

    @Nested
    class CommentSortTest {
        @Test
        void sortByCreateTimeAsc() {
            MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
            queryParams.set("sort", "creationTimestamp,asc");
            CommentQuery commentQuery = getCommentQuery(queryParams);
            Comparator<Comment> createTimeSorter = commentQuery.toComparator();
            List<String> commentNames = comments().stream()
                .sorted(createTimeSorter)
                .map(comment -> comment.getMetadata().getName())
                .toList();
            assertThat(commentNames).isEqualTo(List.of("B", "C", "A"));
        }

        @Test
        void sortByCreateTimeDesc() {
            MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
            queryParams.add("sort", "creationTimestamp,desc");
            Comparator<Comment> createTimeSorter = getCommentQuery(queryParams).toComparator();
            List<String> commentNames = comments().stream()
                .sorted(createTimeSorter)
                .map(comment -> comment.getMetadata().getName())
                .toList();
            assertThat(commentNames).isEqualTo(List.of("A", "B", "C"));
        }

        @Test
        void sortByReplyCountAsc() {
            MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
            queryParams.add("sort", "replyCount,asc");
            Comparator<Comment> comparator = getCommentQuery(queryParams).toComparator();
            List<String> commentNames = comments().stream()
                .sorted(comparator)
                .map(comment -> comment.getMetadata().getName())
                .toList();
            assertThat(commentNames).isEqualTo(List.of("A", "B", "C"));
        }

        @Test
        void sortByReplyCountDesc() {
            MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
            queryParams.add("sort", "replyCount,desc");
            Comparator<Comment> comparator = getCommentQuery(queryParams).toComparator();
            List<String> commentNames = comments().stream()
                .sorted(comparator)
                .map(comment -> comment.getMetadata().getName())
                .toList();
            assertThat(commentNames).isEqualTo(List.of("C", "B", "A"));
        }

        @Test
        void sortByLastReplyTimeAsc() {
            MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
            queryParams.add("sort", "lastReplyTime,asc");
            Comparator<Comment> comparator = getCommentQuery(queryParams).toComparator();
            List<String> commentNames = comments().stream()
                .sorted(comparator)
                .map(comment -> comment.getMetadata().getName())
                .toList();
            assertThat(commentNames).isEqualTo(List.of("C", "A", "B"));
        }

        @Test
        void sortByLastReplyTimeDesc() {
            MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
            queryParams.add("sort", "lastReplyTime,desc");
            Comparator<Comment> comparator = getCommentQuery(queryParams).toComparator();
            List<String> commentNames = comments().stream()
                .sorted(comparator)
                .map(comment -> comment.getMetadata().getName())
                .toList();
            assertThat(commentNames).isEqualTo(List.of("B", "A", "C"));
        }

        @Test
        void sortByDefaultDesc() {
            MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
            Comparator<Comment> comparator = getCommentQuery(queryParams).toComparator();
            List<String> commentNames = comments().stream()
                .sorted(comparator)
                .map(comment -> comment.getMetadata().getName())
                .toList();
            assertThat(commentNames).isEqualTo(List.of("B", "A", "C"));

            List<String> commentList = commentsIncludeNoReply().stream()
                .sorted(comparator)
                .map(comment -> comment.getMetadata().getName())
                .toList();
            assertThat(commentList).isEqualTo(List.of("D", "E", "B", "A", "C"));
        }

        List<Comment> comments() {
            final Instant now = Instant.now();
            Comment commentA = new Comment();
            commentA.setMetadata(new Metadata());
            commentA.getMetadata().setName("A");
            // create time
            commentA.getMetadata().setCreationTimestamp(now.plusSeconds(10));
            commentA.setSpec(new Comment.CommentSpec());
            commentA.getSpec().setCreationTime(commentA.getMetadata().getCreationTimestamp());

            commentA.setStatus(new Comment.CommentStatus());
            // last reply time
            commentA.getStatus().setLastReplyTime(now.plusSeconds(5));
            // reply count
            commentA.getStatus().setReplyCount(3);

            Comment commentB = new Comment();
            commentB.setMetadata(new Metadata());
            commentB.getMetadata().setName("B");
            commentB.getMetadata().setCreationTimestamp(now.plusSeconds(5));
            commentB.setSpec(new Comment.CommentSpec());
            commentB.setStatus(new Comment.CommentStatus());
            commentB.getStatus().setLastReplyTime(now.plusSeconds(15));
            commentB.getStatus().setReplyCount(8);
            commentB.getSpec().setCreationTime(commentB.getMetadata().getCreationTimestamp());

            Comment commentC = new Comment();
            commentC.setMetadata(new Metadata());
            commentC.getMetadata().setName("C");

            commentC.getMetadata().setCreationTimestamp(now.plusSeconds(5));

            commentC.setSpec(new Comment.CommentSpec());
            commentC.setStatus(new Comment.CommentStatus());
            commentC.getStatus().setLastReplyTime(now.plusSeconds(3));
            commentC.getStatus().setReplyCount(10);
            commentC.getSpec().setCreationTime(commentC.getMetadata().getCreationTimestamp());

            return List.of(commentA, commentB, commentC);
        }

        List<Comment> commentsIncludeNoReply() {

            final Instant now = Instant.now();
            Comment commentD = new Comment();
            commentD.setMetadata(new Metadata());
            commentD.getMetadata().setName("D");

            commentD.getMetadata().setCreationTimestamp(now.plusSeconds(50));

            commentD.setSpec(new Comment.CommentSpec());
            commentD.getSpec().setCreationTime(commentD.getMetadata().getCreationTimestamp());
            commentD.setStatus(new Comment.CommentStatus());

            Comment commentE = new Comment();
            commentE.setMetadata(new Metadata());
            commentE.getMetadata().setName("E");

            commentE.getMetadata().setCreationTimestamp(now.plusSeconds(20));
            commentE.setSpec(new Comment.CommentSpec());
            commentE.getSpec().setCreationTime(commentE.getMetadata().getCreationTimestamp());
            commentE.setStatus(new Comment.CommentStatus());

            List<Comment> comments = new ArrayList<>(comments());
            comments.add(commentD);
            comments.add(commentE);

            return comments;
        }
    }
}