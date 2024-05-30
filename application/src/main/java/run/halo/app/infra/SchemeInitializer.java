package run.halo.app.infra;

import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.apache.commons.lang3.BooleanUtils.toStringTrueFalse;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static run.halo.app.extension.index.IndexAttributeFactory.multiValueAttribute;
import static run.halo.app.extension.index.IndexAttributeFactory.simpleAttribute;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Set;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import run.halo.app.content.Stats;
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
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.Secret;
import run.halo.app.extension.index.IndexSpec;
import run.halo.app.extension.index.IndexSpecRegistryImpl;
import run.halo.app.infra.utils.JsonUtils;
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
        schemeManager.register(ExtensionPointDefinition.class, indexSpecs -> {
            indexSpecs.add(new IndexSpec()
                .setName("spec.className")
                .setIndexFunc(simpleAttribute(ExtensionPointDefinition.class,
                    definition -> definition.getSpec().getClassName())
                ));
        });
        schemeManager.register(ExtensionDefinition.class);

        schemeManager.register(RoleBinding.class);
        schemeManager.register(User.class, indexSpecs -> {
            indexSpecs.add(new IndexSpec()
                .setName("spec.displayName")
                .setIndexFunc(
                    simpleAttribute(User.class, user -> user.getSpec().getDisplayName())));
            indexSpecs.add(new IndexSpec()
                .setName(User.USER_RELATED_ROLES_INDEX)
                .setIndexFunc(multiValueAttribute(User.class, user -> {
                    var roleNamesAnno = MetadataUtil.nullSafeAnnotations(user)
                        .get(User.ROLE_NAMES_ANNO);
                    if (StringUtils.isBlank(roleNamesAnno)) {
                        return Set.of();
                    }
                    return JsonUtils.jsonToObject(roleNamesAnno,
                        new TypeReference<>() {
                        });
                })));
        });
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
            indexSpecs.add(new IndexSpec()
                .setName("status.lastModifyTime")
                .setIndexFunc(simpleAttribute(Post.class, post -> {
                    var lastModifyTime = post.getStatus().getLastModifyTime();
                    return lastModifyTime == null ? null : lastModifyTime.toString();
                })));
            indexSpecs.add(new IndexSpec()
                .setName(Post.REQUIRE_SYNC_ON_STARTUP_INDEX_NAME)
                .setIndexFunc(simpleAttribute(Post.class, post -> {
                    var version = post.getMetadata().getVersion();
                    var observedVersion = post.getStatusOrDefault().getObservedVersion();
                    if (observedVersion == null || observedVersion < version) {
                        return BooleanUtils.TRUE;
                    }
                    // do not care about the false case so return null to avoid indexing
                    return null;
                })));

            indexSpecs.add(new IndexSpec()
                .setName("stats.visit")
                .setIndexFunc(simpleAttribute(Post.class, post -> {
                    var annotations = MetadataUtil.nullSafeAnnotations(post);
                    var statsStr = annotations.get(Post.STATS_ANNO);
                    if (StringUtils.isBlank(statsStr)) {
                        return "0";
                    }
                    var stats = JsonUtils.jsonToObject(statsStr, Stats.class);
                    return ObjectUtils.defaultIfNull(stats.getVisit(), 0).toString();
                })));

            indexSpecs.add(new IndexSpec()
                .setName("stats.totalComment")
                .setIndexFunc(simpleAttribute(Post.class, post -> {
                    var annotations = MetadataUtil.nullSafeAnnotations(post);
                    var statsStr = annotations.get(Post.STATS_ANNO);
                    if (StringUtils.isBlank(statsStr)) {
                        return "0";
                    }
                    var stats = JsonUtils.jsonToObject(statsStr, Stats.class);
                    return ObjectUtils.defaultIfNull(stats.getTotalComment(), 0).toString();
                })));
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
                .setName("spec.displayName")
                .setIndexFunc(simpleAttribute(Tag.class, tag -> tag.getSpec().getDisplayName())));
            indexSpecs.add(new IndexSpec()
                .setName("spec.slug")
                .setIndexFunc(simpleAttribute(Tag.class, tag -> tag.getSpec().getSlug()))
            );
            indexSpecs.add(new IndexSpec()
                .setName(Tag.REQUIRE_SYNC_ON_STARTUP_INDEX_NAME)
                .setIndexFunc(simpleAttribute(Tag.class, tag -> {
                    var version = tag.getMetadata().getVersion();
                    var observedVersion = tag.getStatusOrDefault().getObservedVersion();
                    if (observedVersion == null || observedVersion < version) {
                        return BooleanUtils.TRUE;
                    }
                    // do not care about the false case so return null to avoid indexing
                    return null;
                })));
        });
        schemeManager.register(Snapshot.class, indexSpecs -> {
            indexSpecs.add(new IndexSpec()
                .setName("spec.subjectRef")
                .setIndexFunc(simpleAttribute(Snapshot.class,
                    snapshot -> Snapshot.toSubjectRefKey(snapshot.getSpec().getSubjectRef()))
                )
            );
        });
        schemeManager.register(Comment.class, indexSpecs -> {
            indexSpecs.add(new IndexSpec()
                .setName("spec.creationTime")
                .setIndexFunc(simpleAttribute(Comment.class,
                    comment -> defaultIfNull(comment.getSpec().getCreationTime(),
                        comment.getMetadata().getCreationTimestamp()).toString())
                ));
            indexSpecs.add(new IndexSpec()
                .setName("spec.approved")
                .setIndexFunc(simpleAttribute(Comment.class,
                    comment -> toStringTrueFalse(isTrue(comment.getSpec().getApproved())))
                ));
            indexSpecs.add(new IndexSpec()
                .setName("spec.owner")
                .setIndexFunc(simpleAttribute(Comment.class, comment -> {
                    var owner = comment.getSpec().getOwner();
                    return Comment.CommentOwner.ownerIdentity(owner.getKind(), owner.getName());
                })));
            indexSpecs.add(new IndexSpec()
                .setName("spec.subjectRef")
                .setIndexFunc(simpleAttribute(Comment.class,
                    comment -> Comment.toSubjectRefKey(comment.getSpec().getSubjectRef()))
                ));
            indexSpecs.add(new IndexSpec()
                .setName("spec.top")
                .setIndexFunc(simpleAttribute(Comment.class,
                    comment -> toStringTrueFalse(isTrue(comment.getSpec().getTop())))
                ));
            indexSpecs.add(new IndexSpec()
                .setName("spec.hidden")
                .setIndexFunc(simpleAttribute(Comment.class,
                    comment -> toStringTrueFalse(isTrue(comment.getSpec().getHidden())))
                ));
            indexSpecs.add(new IndexSpec()
                .setName("spec.priority")
                .setIndexFunc(simpleAttribute(Comment.class,
                    comment -> {
                        var isTop = comment.getSpec().getTop();
                        // only top comments have priority
                        if (!isTop) {
                            return "0";
                        }
                        return defaultIfNull(comment.getSpec().getPriority(), 0).toString();
                    })
                ));
            indexSpecs.add(new IndexSpec()
                .setName("spec.raw")
                .setIndexFunc(simpleAttribute(Comment.class,
                    comment -> comment.getSpec().getRaw())
                ));
            indexSpecs.add(new IndexSpec()
                .setName("status.lastReplyTime")
                .setIndexFunc(simpleAttribute(Comment.class, comment -> {
                    var lastReplyTime = comment.getStatusOrDefault().getLastReplyTime();
                    return defaultIfNull(lastReplyTime,
                        comment.getSpec().getCreationTime()).toString();
                })));
            indexSpecs.add(new IndexSpec()
                .setName("status.replyCount")
                .setIndexFunc(simpleAttribute(Comment.class, comment -> {
                    var replyCount = comment.getStatusOrDefault().getReplyCount();
                    return defaultIfNull(replyCount, 0).toString();
                })));
            indexSpecs.add(new IndexSpec()
                .setName(Comment.REQUIRE_SYNC_ON_STARTUP_INDEX_NAME)
                .setIndexFunc(simpleAttribute(Comment.class, comment -> {
                    var version = comment.getMetadata().getVersion();
                    var observedVersion = comment.getStatusOrDefault().getObservedVersion();
                    if (observedVersion == null || observedVersion < version) {
                        return BooleanUtils.TRUE;
                    }
                    // do not care about the false case so return null to avoid indexing
                    return null;
                })));
        });
        schemeManager.register(Reply.class, indexSpecs -> {
            indexSpecs.add(new IndexSpec()
                .setName("spec.creationTime")
                .setIndexFunc(simpleAttribute(Reply.class,
                    reply -> defaultIfNull(reply.getSpec().getCreationTime(),
                        reply.getMetadata().getCreationTimestamp()).toString())
                ));
            indexSpecs.add(new IndexSpec()
                .setName("spec.commentName")
                .setIndexFunc(simpleAttribute(Reply.class,
                    reply -> reply.getSpec().getCommentName())
                ));
            indexSpecs.add(new IndexSpec()
                .setName("spec.hidden")
                .setIndexFunc(simpleAttribute(Reply.class,
                    reply -> toStringTrueFalse(isTrue(reply.getSpec().getHidden())))
                ));
            indexSpecs.add(new IndexSpec()
                .setName("spec.approved")
                .setIndexFunc(simpleAttribute(Reply.class,
                    reply -> toStringTrueFalse(isTrue(reply.getSpec().getApproved())))
                ));
            indexSpecs.add(new IndexSpec()
                .setName("spec.owner")
                .setIndexFunc(simpleAttribute(Reply.class, reply -> {
                    var owner = reply.getSpec().getOwner();
                    return Comment.CommentOwner.ownerIdentity(owner.getKind(), owner.getName());
                })));
            indexSpecs.add(new IndexSpec()
                .setName(Reply.REQUIRE_SYNC_ON_STARTUP_INDEX_NAME)
                .setIndexFunc(simpleAttribute(Reply.class, reply -> {
                    var version = reply.getMetadata().getVersion();
                    var observedVersion = reply.getStatus().getObservedVersion();
                    if (observedVersion == null || observedVersion < version) {
                        return BooleanUtils.TRUE;
                    }
                    // do not care about the false case so return null to avoid indexing
                    return null;
                })));
        });
        schemeManager.register(SinglePage.class);
        // storage.halo.run
        schemeManager.register(Group.class);
        schemeManager.register(Policy.class);
        schemeManager.register(Attachment.class, indexSpecs -> {
            indexSpecs.add(new IndexSpec()
                .setName("spec.displayName")
                .setIndexFunc(simpleAttribute(Attachment.class,
                    attachment -> attachment.getSpec().getDisplayName()))
            );
            indexSpecs.add(new IndexSpec()
                .setName("spec.policyName")
                .setIndexFunc(simpleAttribute(Attachment.class,
                    attachment -> attachment.getSpec().getPolicyName()))
            );
            indexSpecs.add(new IndexSpec()
                .setName("spec.groupName")
                .setIndexFunc(simpleAttribute(Attachment.class, attachment -> {
                    var group = attachment.getSpec().getGroupName();
                    return StringUtils.isBlank(group) ? null : group;
                }))
            );
            indexSpecs.add(new IndexSpec()
                .setName("spec.mediaType")
                .setIndexFunc(simpleAttribute(Attachment.class, attachment -> {
                    var mediaType = attachment.getSpec().getMediaType();
                    return StringUtils.isBlank(mediaType) ? null : mediaType;
                }))
            );
            indexSpecs.add(new IndexSpec()
                .setName("spec.ownerName")
                .setIndexFunc(simpleAttribute(Attachment.class,
                    attachment -> attachment.getSpec().getOwnerName()))
            );
            indexSpecs.add(new IndexSpec()
                .setName("spec.size")
                .setIndexFunc(simpleAttribute(Attachment.class,
                    attachment -> {
                        var size = attachment.getSpec().getSize();
                        return size != null ? size.toString() : null;
                    }))
            );
        });
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
        schemeManager.register(NotificationTemplate.class, indexSpecs -> {
            indexSpecs.add(new IndexSpec()
                .setName("spec.reasonSelector.reasonType")
                .setIndexFunc(simpleAttribute(NotificationTemplate.class,
                    template -> template.getSpec().getReasonSelector().getReasonType()))
            );
        });
        schemeManager.register(Subscription.class, indexSpecs -> {
            indexSpecs.add(new IndexSpec()
                .setName("spec.reason.reasonType")
                .setIndexFunc(simpleAttribute(Subscription.class,
                    subscription -> subscription.getSpec().getReason().getReasonType()))
            );
            indexSpecs.add(new IndexSpec()
                .setName("spec.reason.subject")
                .setIndexFunc(simpleAttribute(Subscription.class,
                    subscription -> subscription.getSpec().getReason().getSubject().toString()))
            );
            indexSpecs.add(new IndexSpec()
                .setName("spec.reason.expression")
                .setIndexFunc(simpleAttribute(Subscription.class,
                    subscription -> subscription.getSpec().getReason().getExpression()))
            );
            indexSpecs.add(new IndexSpec()
                .setName("spec.subscriber")
                .setIndexFunc(simpleAttribute(Subscription.class,
                    subscription -> subscription.getSpec().getSubscriber().toString()))
            );
        });
        schemeManager.register(NotifierDescriptor.class);
        schemeManager.register(Notification.class, indexSpecs -> {
            indexSpecs.add(new IndexSpec()
                .setName("spec.unread")
                .setIndexFunc(simpleAttribute(Notification.class,
                    notification -> String.valueOf(notification.getSpec().isUnread())))
            );
            indexSpecs.add(new IndexSpec()
                .setName("spec.reason")
                .setIndexFunc(simpleAttribute(Notification.class,
                    notification -> notification.getSpec().getReason()))
            );
            indexSpecs.add(new IndexSpec()
                .setName("spec.recipient")
                .setIndexFunc(simpleAttribute(Notification.class,
                    notification -> notification.getSpec().getRecipient()))
            );
            indexSpecs.add(new IndexSpec()
                .setName("spec.title")
                .setIndexFunc(simpleAttribute(Notification.class,
                    notification -> notification.getSpec().getTitle()))
            );
            indexSpecs.add(new IndexSpec()
                .setName("spec.rawContent")
                .setIndexFunc(simpleAttribute(Notification.class,
                    notification -> notification.getSpec().getRawContent()))
            );
        });
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
