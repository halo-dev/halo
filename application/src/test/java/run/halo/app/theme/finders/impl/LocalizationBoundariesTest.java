package run.halo.app.theme.finders.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.content.comment.CommentPermalinkService;
import run.halo.app.core.counter.CounterService;
import run.halo.app.core.extension.Menu;
import run.halo.app.core.extension.MenuItem;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.core.extension.content.Reply;
import run.halo.app.core.user.service.UserService;
import run.halo.app.extension.*;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.localization.*;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;

class LocalizationBoundariesTest {
    private final LocalizationContext context =
            new LocalizationContext(Locale.CHINA, Locale.ENGLISH, List.of(), LocalizationScope.PUBLIC_PAGE);
    private final ResourceLocalizationService localization = mock(ResourceLocalizationService.class);
    private final ReactiveExtensionClient client = mock(ReactiveExtensionClient.class);

    @Test
    void primaryMenuIsProjectedOnlyOnce() {
        var source = new Menu();
        var metadata = new Metadata();
        metadata.setName("primary");
        source.setMetadata(metadata);
        source.setSpec(new Menu.Spec());
        source.getSpec().setDisplayName("主菜单");
        when(client.listAll(eq(Menu.class), any(), eq(Sort.unsorted()))).thenReturn(Flux.just(source));
        when(client.listAll(eq(MenuItem.class), any(), eq(Sort.unsorted()))).thenReturn(Flux.empty());
        var config = mock(SystemConfigFetcher.class);
        when(config.fetch(anyString(), eq(SystemSetting.Menu.class))).thenReturn(Mono.empty());
        when(localization.localize(source, context)).thenReturn(Mono.just(source));
        new MenuFinderImpl(client, localization, config)
                .getPrimary()
                .contextWrite(ctx -> ctx.put(LocalizationContext.class, context))
                .as(StepVerifier::create)
                .assertNext(menu -> assertThat(menu.getMetadata().getName()).isEqualTo("primary"))
                .verifyComplete();
        verify(localization, times(1)).localize(source, context);
        verify(localization, never()).localizeAll(any(), any());
    }

    @Test
    void registeredCommentAndReplyOwnersUseLocalizedDisplayNames() {
        var users = mock(UserService.class);
        var counters = mock(CounterService.class);
        when(counters.getByName(anyString())).thenReturn(Mono.empty());
        var source = new User();
        var metadata = new Metadata();
        metadata.setName("author");
        source.setMetadata(metadata);
        source.setSpec(new User.UserSpec());
        source.getSpec().setDisplayName("原作者");
        var translated = new User();
        translated.setMetadata(metadata);
        translated.setSpec(new User.UserSpec());
        translated.getSpec().setDisplayName("Author");
        when(users.getUserOrGhost("author")).thenReturn(Mono.just(source));
        when(localization.localize(source, context)).thenReturn(Mono.just(translated));
        var owner = new Comment.CommentOwner();
        owner.setName("author");
        owner.setKind(User.KIND);
        var comment = new Comment();
        var commentMetadata = new Metadata();
        commentMetadata.setName("comment");
        comment.setMetadata(commentMetadata);
        comment.setSpec(new Comment.CommentSpec());
        comment.getSpec().setOwner(owner);
        comment.getSpec().setContent("Content");
        var reply = new Reply();
        var replyMetadata = new Metadata();
        replyMetadata.setName("reply");
        reply.setMetadata(replyMetadata);
        reply.setSpec(new Reply.ReplySpec());
        var replyOwner = new Comment.CommentOwner();
        replyOwner.setName("author");
        replyOwner.setKind(User.KIND);
        reply.getSpec().setOwner(replyOwner);
        reply.getSpec().setContent("Reply");
        when(localization.localize(comment, context)).thenReturn(Mono.just(comment));
        when(localization.localize(reply, context)).thenReturn(Mono.just(reply));
        var service = new CommentPublicQueryServiceImpl(
                client,
                users,
                counters,
                mock(CommentPermalinkService.class),
                localization,
                mock(ExtensionGetter.class));
        service.toCommentVo(comment)
                .contextWrite(ctx -> ctx.put(LocalizationContext.class, context))
                .as(StepVerifier::create)
                .assertNext(vo -> {
                    assertThat(vo.getOwner().getDisplayName()).isEqualTo("Author");
                    assertThat(vo.getSpec().getOwner().getName()).isEmpty();
                })
                .verifyComplete();
        service.toReplyVo(reply)
                .contextWrite(ctx -> ctx.put(LocalizationContext.class, context))
                .as(StepVerifier::create)
                .assertNext(vo -> {
                    assertThat(vo.getOwner().getDisplayName()).isEqualTo("Author");
                    assertThat(vo.getSpec().getOwner().getName()).isEmpty();
                })
                .verifyComplete();
        assertThat(source.getSpec().getDisplayName()).isEqualTo("原作者");
        assertThat(comment.getSpec().getOwner().getName()).isEqualTo("author");
        assertThat(reply.getSpec().getOwner().getName()).isEqualTo("author");
    }
}
