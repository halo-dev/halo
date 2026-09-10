package run.halo.app.content.comment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.core.extension.content.Reply;
import run.halo.app.extension.Extension;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Ref;

@ExtendWith(MockitoExtension.class)
class CommentContentUpdateTest {
    @Mock
    ReactiveExtensionClient client;

    @ParameterizedTest
    @CsvSource({"false,false", "false,true", "true,false", "true,true"})
    void changesOnlyTheBody(boolean reply, boolean approved) {
        var stored = stored(reply, approved);
        var expected = resource(reply, approved);
        spec(expected).setRaw("Corrected text");
        spec(expected).setContent("<p>Corrected <strong>text</strong></p>");
        when(client.update(stored)).thenAnswer(invocation -> Mono.just(invocation.getArgument(0, Extension.class)));

        edit(reply, new CommentContentRequest("Corrected text", spec(expected).getContent(), 12L))
                .as(StepVerifier::create)
                .assertNext(updated ->
                        assertThat(updated).usingRecursiveComparison().isEqualTo(expected))
                .verifyComplete();
        verify(client).update(stored);
        verify(client, never()).create(any());
    }

    static Stream<Arguments> invalidBodies() {
        return Stream.of(false, true)
                .flatMap(reply -> Stream.of(
                                new CommentContentRequest(null, "text", 12L),
                                new CommentContentRequest(" ", "text", 12L),
                                new CommentContentRequest("text", null, 12L),
                                new CommentContentRequest("text", " ", 12L),
                                new CommentContentRequest("text", "<p><br></p>", 12L),
                                new CommentContentRequest("text", "<p>&nbsp;</p>", 12L),
                                new CommentContentRequest("text", "<img src=\"\">", 12L),
                                new CommentContentRequest("text", "<script>alert(1)</script>text", 12L),
                                new CommentContentRequest("text", "<a href=\"javascript:alert(1)\">text</a>", 12L),
                                new CommentContentRequest("text", "text", null))
                        .map(request -> Arguments.of(reply, request)));
    }

    @ParameterizedTest
    @MethodSource("invalidBodies")
    void rejectsInvalidBodiesWithoutUpdating(boolean reply, CommentContentRequest request) {
        var stored = stored(reply, false);
        edit(reply, request)
                .as(StepVerifier::create)
                .expectError(ServerWebInputException.class)
                .verify();
        assertThat(spec(stored).getRaw()).isEqualTo("Original");
        verify(client, never()).update(any());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void acceptsImageOnlyContent(boolean reply) {
        var stored = stored(reply, true);
        var image = "<img src=\"https://example.com/image.png\">";
        when(client.update(stored)).thenAnswer(invocation -> Mono.just(invocation.getArgument(0, Extension.class)));
        edit(reply, new CommentContentRequest(image, image, 12L))
                .as(StepVerifier::create)
                .assertNext(updated -> assertThat(updated).isSameAs(stored))
                .verifyComplete();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void rejectsStaleEdits(boolean reply) {
        stored(reply, true);
        edit(reply, new CommentContentRequest("corrected", "corrected", 11L))
                .as(StepVerifier::create)
                .expectError(OptimisticLockingFailureException.class)
                .verify();
        verify(client, never()).update(any());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void doesNotRetryStorageConflicts(boolean reply) {
        var stored = stored(reply, true);
        when(client.update(stored)).thenReturn(Mono.error(new OptimisticLockingFailureException("conflict")));
        edit(reply, new CommentContentRequest("corrected", "corrected", 12L))
                .as(StepVerifier::create)
                .expectError(OptimisticLockingFailureException.class)
                .verify();
        verify(client).update(stored);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void rejectsDeletedResources(boolean reply) {
        stored(reply, true).getMetadata().setDeletionTimestamp(Instant.now());
        edit(reply, new CommentContentRequest("corrected", "corrected", 12L))
                .as(StepVerifier::create)
                .expectErrorSatisfies(error -> assertThat(((ResponseStatusException) error)
                                .getStatusCode()
                                .value())
                        .isEqualTo(409))
                .verify();
        verify(client, never()).update(any());
    }

    private Mono<? extends Extension> edit(boolean reply, CommentContentRequest request) {
        return reply
                ? new ReplyServiceImpl(null, client, null, null).updateContent("entry", request)
                : new CommentServiceImpl(null, client, null, null, null, null).updateContent("entry", request);
    }

    private Extension stored(boolean reply, boolean approved) {
        var resource = resource(reply, approved);
        if (reply) {
            when(client.get(Reply.class, "entry")).thenReturn(Mono.just((Reply) resource));
        } else {
            when(client.get(Comment.class, "entry")).thenReturn(Mono.just((Comment) resource));
        }
        return resource;
    }

    private static Comment.BaseCommentSpec spec(Extension resource) {
        return resource instanceof Reply reply ? reply.getSpec() : ((Comment) resource).getSpec();
    }

    private static Extension resource(boolean reply, boolean approved) {
        Extension resource;
        if (reply) {
            var entry = new Reply();
            var spec = new Reply.ReplySpec();
            spec.setCommentName("parent");
            spec.setQuoteReply("quoted");
            entry.setSpec(spec);
            resource = entry;
        } else {
            var entry = new Comment();
            var spec = new Comment.CommentSpec();
            var subject = new Ref();
            subject.setGroup("content.halo.run");
            subject.setKind("Post");
            subject.setName("post");
            spec.setSubjectRef(subject);
            spec.setLastReadTime(Instant.EPOCH);
            entry.setSpec(spec);
            resource = entry;
        }
        var metadata = new Metadata();
        metadata.setName("entry");
        metadata.setVersion(12L);
        metadata.setCreationTimestamp(Instant.EPOCH);
        resource.setMetadata(metadata);
        var spec = spec(resource);
        spec.setRaw("Original");
        spec.setContent("<p>Original</p>");
        spec.setApproved(approved);
        spec.setApprovedTime(approved ? Instant.EPOCH : null);
        spec.setCreationTime(Instant.EPOCH);
        spec.setHidden(true);
        spec.setAllowNotification(true);
        spec.setTop(true);
        spec.setPriority(5);
        spec.setIpAddress("192.0.2.1");
        spec.setUserAgent("original browser");
        var owner = new Comment.CommentOwner();
        owner.setKind("User");
        owner.setName("original-owner");
        spec.setOwner(owner);
        return resource;
    }
}
