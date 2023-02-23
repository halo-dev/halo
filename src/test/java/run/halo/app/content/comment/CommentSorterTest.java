package run.halo.app.content.comment;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Test;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.extension.Metadata;

/**
 * Tests for {@link CommentSorter}.
 *
 * @author guqing
 * @since 2.0.0
 */
class CommentSorterTest {

    @Test
    void sortByCreateTimeAsc() {
        Comparator<Comment> createTimeSorter = CommentSorter.from(CommentSorter.CREATE_TIME);
        List<String> commentNames = comments().stream()
            .sorted(createTimeSorter)
            .map(comment -> comment.getMetadata().getName())
            .toList();
        assertThat(commentNames).isEqualTo(List.of("B", "C", "A"));

        createTimeSorter = CommentSorter.from(CommentSorter.CREATE_TIME, true);
        commentNames = comments().stream()
            .sorted(createTimeSorter)
            .map(comment -> comment.getMetadata().getName())
            .toList();
        assertThat(commentNames).isEqualTo(List.of("B", "C", "A"));
    }

    @Test
    void sortByCreateTimeDesc() {
        Comparator<Comment> createTimeSorter = CommentSorter.from(CommentSorter.CREATE_TIME, false);
        List<String> commentNames = comments().stream()
            .sorted(createTimeSorter)
            .map(comment -> comment.getMetadata().getName())
            .toList();
        assertThat(commentNames).isEqualTo(List.of("A", "C", "B"));
    }

    @Test
    void sortByReplyCountAsc() {
        Comparator<Comment> createTimeSorter = CommentSorter.from(CommentSorter.REPLY_COUNT);
        List<String> commentNames = comments().stream()
            .sorted(createTimeSorter)
            .map(comment -> comment.getMetadata().getName())
            .toList();
        assertThat(commentNames).isEqualTo(List.of("A", "B", "C"));

        createTimeSorter = CommentSorter.from(CommentSorter.REPLY_COUNT, true);
        commentNames = comments().stream()
            .sorted(createTimeSorter)
            .map(comment -> comment.getMetadata().getName())
            .toList();
        assertThat(commentNames).isEqualTo(List.of("A", "B", "C"));
    }

    @Test
    void sortByReplyCountDesc() {
        Comparator<Comment> createTimeSorter = CommentSorter.from(CommentSorter.REPLY_COUNT, false);
        List<String> commentNames = comments().stream()
            .sorted(createTimeSorter)
            .map(comment -> comment.getMetadata().getName())
            .toList();
        assertThat(commentNames).isEqualTo(List.of("C", "B", "A"));
    }

    @Test
    void sortByLastReplyTimeAsc() {
        Comparator<Comment> createTimeSorter = CommentSorter.from(CommentSorter.LAST_REPLY_TIME);
        List<String> commentNames = comments().stream()
            .sorted(createTimeSorter)
            .map(comment -> comment.getMetadata().getName())
            .toList();
        assertThat(commentNames).isEqualTo(List.of("C", "A", "B"));

        createTimeSorter = CommentSorter.from(CommentSorter.LAST_REPLY_TIME, true);
        commentNames = comments().stream()
            .sorted(createTimeSorter)
            .map(comment -> comment.getMetadata().getName())
            .toList();
        assertThat(commentNames).isEqualTo(List.of("C", "A", "B"));
    }

    @Test
    void sortByLastReplyTimeDesc() {
        Comparator<Comment> createTimeSorter =
            CommentSorter.from(CommentSorter.LAST_REPLY_TIME, false);
        List<String> commentNames = comments().stream()
            .sorted(createTimeSorter)
            .map(comment -> comment.getMetadata().getName())
            .toList();
        assertThat(commentNames).isEqualTo(List.of("B", "A", "C"));
    }

    @Test
    void sortByDefaultDesc() {
        Comparator<Comment> defaultComparator = CommentSorter.lastReplyTimeComparator().reversed();
        List<String> commentNames = comments().stream()
            .sorted(defaultComparator)
            .map(comment -> comment.getMetadata().getName())
            .toList();
        assertThat(commentNames).isEqualTo(List.of("B", "A", "C"));


        List<String> commentList = commentsIncludeNoReply().stream()
            .sorted(defaultComparator)
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