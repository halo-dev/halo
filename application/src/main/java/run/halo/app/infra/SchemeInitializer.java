package run.halo.app.infra;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static run.halo.app.extension.index.IndexAttributeFactory.multiValueAttribute;
import static run.halo.app.extension.index.IndexAttributeFactory.simpleAttribute;

import java.util.Set;
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
import run.halo.app.extension.index.IndexSpec;
import run.halo.app.extension.index.IndexSpecRegistryImpl;
import run.halo.app.migration.Backup;
import run.halo.app.plugin.extensionpoint.ExtensionDefinition;
import run.halo.app.plugin.extensionpoint.ExtensionPointDefinition;
import run.halo.app.search.extension.SearchEngine;
import run.halo.app.security.PersonalAccessToken;

@Component
public class SchemeInitializer implements ApplicationListener<ApplicationContextInitializedEvent> {

    @Override
    public void onApplicationEvent(@NonNull ApplicationContextInitializedEvent event) {
        var schemeManager = createSchemeManager(event);

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
        schemeManager.register(Post.class, indexSpecs -> {
            indexSpecs.add(new IndexSpec()
                .setName("spec.title")
                .setIndexFunc(simpleAttribute(Post.class, post -> post.getSpec().getTitle())));
            indexSpecs.add(new IndexSpec()
                .setName("spec.slug")
                // Compatible with old data, hoping to set it to true in the future
                .setUnique(false)
                .setIndexFunc(simpleAttribute(Post.class, post -> post.getSpec().getSlug())));
            indexSpecs.add(new IndexSpec()
                .setName("spec.publishTime")
                .setIndexFunc(simpleAttribute(Post.class, post -> {
                    var publishTime = post.getSpec().getPublishTime();
                    return publishTime == null ? null : publishTime.toString();
                })));
            indexSpecs.add(new IndexSpec()
                .setName("spec.owner")
                .setIndexFunc(simpleAttribute(Post.class, post -> post.getSpec().getOwner())));
            indexSpecs.add(new IndexSpec()
                .setName("spec.deleted")
                .setIndexFunc(simpleAttribute(Post.class, post -> {
                    var deleted = post.getSpec().getDeleted();
                    return deleted == null ? "false" : deleted.toString();
                })));
            indexSpecs.add(new IndexSpec()
                .setName("spec.pinned")
                .setIndexFunc(simpleAttribute(Post.class, post -> {
                    var pinned = post.getSpec().getPinned();
                    return pinned == null ? "false" : pinned.toString();
                })));
            indexSpecs.add(new IndexSpec()
                .setName("spec.priority")
                .setIndexFunc(simpleAttribute(Post.class, post -> {
                    var priority = post.getSpec().getPriority();
                    return priority == null ? "0" : priority.toString();
                })));
            indexSpecs.add(new IndexSpec()
                .setName("spec.visible")
                .setIndexFunc(
                    simpleAttribute(Post.class, post -> post.getSpec().getVisible().name())));
            indexSpecs.add(new IndexSpec()
                .setName("spec.tags")
                .setIndexFunc(multiValueAttribute(Post.class, post -> {
                    var tags = post.getSpec().getTags();
                    return tags == null ? Set.of() : Set.copyOf(tags);
                })));
            indexSpecs.add(new IndexSpec()
                .setName("spec.categories")
                .setIndexFunc(multiValueAttribute(Post.class, post -> {
                    var categories = post.getSpec().getCategories();
                    return categories == null ? Set.of() : Set.copyOf(categories);
                })));
            indexSpecs.add(new IndexSpec()
                .setName("status.contributors")
                .setIndexFunc(multiValueAttribute(Post.class, post -> {
                    var contributors = post.getStatusOrDefault().getContributors();
                    return contributors == null ? Set.of() : Set.copyOf(contributors);
                })));
            indexSpecs.add(new IndexSpec()
                .setName("status.categories")
                .setIndexFunc(
                    simpleAttribute(Post.class, post -> post.getStatusOrDefault().getExcerpt())));
            indexSpecs.add(new IndexSpec()
                .setName("status.phase")
                .setIndexFunc(
                    simpleAttribute(Post.class, post -> post.getStatusOrDefault().getPhase())));
            indexSpecs.add(new IndexSpec()
                .setName("status.excerpt")
                .setIndexFunc(
                    simpleAttribute(Post.class, post -> post.getStatusOrDefault().getExcerpt())));
        });
        schemeManager.register(Category.class, indexSpecs -> {
            indexSpecs.add(new IndexSpec()
                .setName("spec.slug")
                .setIndexFunc(
                    simpleAttribute(Category.class, category -> category.getSpec().getSlug()))
            );
            indexSpecs.add(new IndexSpec()
                .setName("spec.priority")
                .setIndexFunc(simpleAttribute(Category.class,
                    category -> defaultIfNull(category.getSpec().getPriority(), 0).toString())));
        });
        schemeManager.register(Tag.class, indexSpecs -> {
            indexSpecs.add(new IndexSpec()
                .setName("spec.slug")
                .setIndexFunc(simpleAttribute(Tag.class, tag -> tag.getSpec().getSlug()))
            );
        });
        schemeManager.register(Snapshot.class, indexSpecs -> {
            indexSpecs.add(new IndexSpec()
                .setName("spec.subjectRef")
                .setIndexFunc(simpleAttribute(Snapshot.class,
                    snapshot -> Snapshot.toSubjectRefKey(snapshot.getSpec().getSubjectRef()))
                )
            );
        });
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

    private static DefaultSchemeManager createSchemeManager(
        ApplicationContextInitializedEvent event) {
        var indexSpecRegistry = new IndexSpecRegistryImpl();
        var watcherManager = new DefaultSchemeWatcherManager();
        var schemeManager = new DefaultSchemeManager(indexSpecRegistry, watcherManager);

        var beanFactory = event.getApplicationContext().getBeanFactory();
        beanFactory.registerSingleton("indexSpecRegistry", indexSpecRegistry);
        beanFactory.registerSingleton("schemeWatcherManager", watcherManager);
        beanFactory.registerSingleton("schemeManager", schemeManager);
        return schemeManager;
    }
}
