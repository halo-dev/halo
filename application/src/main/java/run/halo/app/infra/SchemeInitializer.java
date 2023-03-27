package run.halo.app.infra;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.AnnotationSetting;
import run.halo.app.core.extension.AuthProvider;
import run.halo.app.core.extension.Counter;
import run.halo.app.core.extension.Menu;
import run.halo.app.core.extension.MenuItem;
import run.halo.app.core.extension.Plugin;
import run.halo.app.core.extension.ReverseProxy;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.RoleBinding;
import run.halo.app.core.extension.Setting;
import run.halo.app.core.extension.Theme;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.UserConnection;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.core.extension.attachment.Group;
import run.halo.app.core.extension.attachment.Policy;
import run.halo.app.core.extension.attachment.PolicyTemplate;
import run.halo.app.core.extension.content.Category;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.Reply;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.core.extension.content.Snapshot;
import run.halo.app.core.extension.content.Tag;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.SchemeManager;
import run.halo.app.extension.Secret;
import run.halo.app.plugin.extensionpoint.ExtensionDefinition;
import run.halo.app.plugin.extensionpoint.ExtensionPointDefinition;
import run.halo.app.search.extension.SearchEngine;
import run.halo.app.security.authentication.pat.PersonalAccessToken;

@Component
public class SchemeInitializer implements ApplicationListener<ApplicationStartedEvent> {

    private final SchemeManager schemeManager;

    private final ApplicationEventPublisher eventPublisher;

    public SchemeInitializer(SchemeManager schemeManager,
        ApplicationEventPublisher eventPublisher) {
        this.schemeManager = schemeManager;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void onApplicationEvent(@NonNull ApplicationStartedEvent event) {
        schemeManager.register(Role.class);
        schemeManager.register(PersonalAccessToken.class);

        // plugin.halo.run
        schemeManager.register(Plugin.class);
        schemeManager.register(SearchEngine.class);
        schemeManager.register(ExtensionPointDefinition.class);
        schemeManager.register(ExtensionDefinition.class);

        schemeManager.register(RoleBinding.class);
        schemeManager.register(User.class);
        schemeManager.register(ReverseProxy.class);
        schemeManager.register(Setting.class);
        schemeManager.register(AnnotationSetting.class);
        schemeManager.register(ConfigMap.class);
        schemeManager.register(Secret.class);
        schemeManager.register(Theme.class);
        schemeManager.register(Menu.class);
        schemeManager.register(MenuItem.class);
        schemeManager.register(Post.class);
        schemeManager.register(Category.class);
        schemeManager.register(Tag.class);
        schemeManager.register(Snapshot.class);
        schemeManager.register(Comment.class);
        schemeManager.register(Reply.class);
        schemeManager.register(SinglePage.class);
        // storage.halo.run
        schemeManager.register(Group.class);
        schemeManager.register(Policy.class);
        schemeManager.register(Attachment.class);
        schemeManager.register(PolicyTemplate.class);
        // metrics.halo.run
        schemeManager.register(Counter.class);
        // auth.halo.run
        schemeManager.register(AuthProvider.class);
        schemeManager.register(UserConnection.class);

        eventPublisher.publishEvent(new SchemeInitializedEvent(this));
    }
}
