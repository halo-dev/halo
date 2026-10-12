package run.halo.app.content.comment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.counter.CounterService;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.core.extension.content.Reply;
import run.halo.app.core.user.service.RoleService;
import run.halo.app.core.user.service.UserService;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
class ReplyServiceImplTest {

    @Mock
    RoleService roleService;

    @Mock
    ReactiveExtensionClient client;

    @Mock
    UserService userService;

    @Mock
    CounterService counterService;

    @Mock
    CommentPermalinkService permalinkService;

    @InjectMocks
    ReplyServiceImpl replyService;

    @ParameterizedTest
    @WithMockUser
    @CsvSource(
            nullValues = "NULL",
            value = {
                "false, true,  false, false, true",
                "false, false, false, false, false",
                "false, NULL,  false, false, false",
                "true,  false, false, false, true",
                "false, false, true,  true,  true",
                "false, true,  true,  false, true"
            })
    void preservesPrivateReplyChoiceAndParentVisibility(
            boolean parentHidden,
            Boolean requestedHidden,
            boolean quotePresent,
            boolean quotedHidden,
            boolean expectedHidden) {
        var comment = new Comment();
        comment.setMetadata(new Metadata());
        comment.getMetadata().setName("parent");
        comment.setSpec(new Comment.CommentSpec());
        comment.getSpec().setApproved(true);
        comment.getSpec().setHidden(parentHidden);
        when(client.get(Comment.class, "parent")).thenReturn(Mono.just(comment));
        when(roleService.contains(any(), any())).thenReturn(Mono.just(false));

        var request = new ReplyRequest();
        request.setRaw("Reply body");
        request.setContent("<p>Reply body</p>");
        request.setHidden(requestedHidden);
        var reply = request.toReply();
        reply.getSpec().setOwner(new Comment.CommentOwner());
        if (quotePresent) {
            var quoted = new Reply();
            quoted.setSpec(new Reply.ReplySpec());
            quoted.getSpec().setApproved(true);
            quoted.getSpec().setHidden(quotedHidden);
            reply.getSpec().setQuoteReply("quoted");
            when(client.get(Reply.class, "quoted")).thenReturn(Mono.just(quoted));
        }
        when(client.create(any(Reply.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(replyService.create("parent", reply))
                .consumeNextWith(
                        created -> assertThat(created.getSpec().getHidden()).isEqualTo(expectedHidden))
                .verifyComplete();
    }
}
