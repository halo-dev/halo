package run.halo.app.infra;

import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
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
import run.halo.app.core.extension.notification.Notification;
import run.halo.app.core.extension.notification.NotificationTemplate;
import run.halo.app.core.extension.notification.NotifierDescriptor;
import run.halo.app.core.extension.notification.Reason;
import run.halo.app.core.extension.notification.ReasonType;
import run.halo.app.core.extension.notification.Subscription;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.DefaultSchemeManager;
import run.halo.app.extension.DefaultSchemeWatcherManager;
import run.halo.app.extension.Secret;
import run.halo.app.migration.Backup;
import run.halo.app.plugin.extensionpoint.ExtensionDefinition;
import run.halo.app.plugin.extensionpoint.ExtensionPointDefinition;
import run.halo.app.search.extension.SearchEngine;
import run.halo.app.security.PersonalAccessToken;

@Component
public class SchemeInitializer implements ApplicationListener<ApplicationContextInitializedEvent> {

    @Override
    public void onApplicationEvent(@NonNull ApplicationContextInitializedEvent event) {
        var watcherManager = new DefaultSchemeWatcherManager();
        var schemeManager = new DefaultSchemeManager(watcherManager);

        var beanFactory = event.getApplicationContext().getBeanFactory();
        beanFactory.registerSingleton("schemeWatcherManager", watcherManager);
        beanFactory.registerSingleton("schemeManager", schemeManager);

        schemeManager.register(Role.class);

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

        // security.halo.run
        schemeManager.register(PersonalAccessToken.class);

        // migration.halo.run
        schemeManager.register(Backup.class);

        // notification.halo.run
        schemeManager.register(ReasonType.class);
        schemeManager.register(Reason.class);
        schemeManager.register(NotificationTemplate.class);
        schemeManager.register(Subscription.class);
        schemeManager.register(NotifierDescriptor.class);
        schemeManager.register(Notification.class);
    }
}
