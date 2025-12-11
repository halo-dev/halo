package run.halo.app.infra;

import static java.util.Objects.requireNonNullElse;
import static run.halo.app.core.extension.Role.ROLE_AGGREGATE_LABEL_PREFIX;
import static run.halo.app.core.extension.content.Comment.CommentOwner.ownerIdentity;
import static run.halo.app.extension.index.IndexAttributeFactory.simpleAttribute;

import com.fasterxml.jackson.core.type.TypeReference;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;
import run.halo.app.content.Stats;
import run.halo.app.core.attachment.extension.LocalThumbnail;
import run.halo.app.core.attachment.extension.Thumbnail;
import run.halo.app.core.extension.AnnotationSetting;
import run.halo.app.core.extension.AuthProvider;
import run.halo.app.core.extension.Counter;
import run.halo.app.core.extension.Device;
import run.halo.app.core.extension.Menu;
import run.halo.app.core.extension.MenuItem;
import run.halo.app.core.extension.Plugin;
import run.halo.app.core.extension.RememberMeToken;
import run.halo.app.core.extension.ReverseProxy;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.RoleBinding;
import run.halo.app.core.extension.Setting;
import run.halo.app.core.extension.Theme;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.UserConnection;
import run.halo.app.core.extension.UserConnection.UserConnectionSpec;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.core.extension.attachment.Attachment.AttachmentSpec;
import run.halo.app.core.extension.attachment.Attachment.AttachmentStatus;
import run.halo.app.core.extension.attachment.Group;
import run.halo.app.core.extension.attachment.Policy;
import run.halo.app.core.extension.attachment.PolicyTemplate;
import run.halo.app.core.extension.content.Category;
import run.halo.app.core.extension.content.Category.CategorySpec;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.core.extension.content.Comment.CommentSpec;
import run.halo.app.core.extension.content.Comment.CommentStatus;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.Post.PostSpec;
import run.halo.app.core.extension.content.Post.PostStatus;
import run.halo.app.core.extension.content.Post.VisibleEnum;
import run.halo.app.core.extension.content.Reply;
import run.halo.app.core.extension.content.Reply.ReplySpec;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.core.extension.content.SinglePage.SinglePageSpec;
import run.halo.app.core.extension.content.Snapshot;
import run.halo.app.core.extension.content.Tag;
import run.halo.app.core.extension.content.Tag.TagSpec;
import run.halo.app.core.extension.content.Tag.TagStatus;
import run.halo.app.core.extension.notification.Notification;
import run.halo.app.core.extension.notification.Notification.NotificationSpec;
import run.halo.app.core.extension.notification.NotificationTemplate;
import run.halo.app.core.extension.notification.NotifierDescriptor;
import run.halo.app.core.extension.notification.Reason;
import run.halo.app.core.extension.notification.ReasonType;
import run.halo.app.core.extension.notification.Subscription;
import run.halo.app.core.extension.notification.Subscription.InterestReason;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.MetadataOperator;
import run.halo.app.extension.SchemeManager;
import run.halo.app.extension.Secret;
import run.halo.app.extension.index.IndexSpec;
import run.halo.app.extension.index.IndexSpecs;
import run.halo.app.infra.utils.JsonUtils;
import run.halo.app.migration.Backup;
import run.halo.app.plugin.extensionpoint.ExtensionDefinition;
import run.halo.app.plugin.extensionpoint.ExtensionPointDefinition;
import run.halo.app.security.PersonalAccessToken;

@Component
class SchemeInitializer implements SmartLifecycle {

    private final SchemeManager schemeManager;

    private volatile boolean running;

    public SchemeInitializer(SchemeManager schemeManager) {
        this.schemeManager = schemeManager;
    }

    @Override
    public void start() {
        if (running) {
            return;
        }
        running = true;
        schemeManager.register(Role.class, is -> {
            is.add(IndexSpecs.<Role, String>multi("labels.aggregateToRoles", String.class)
                .indexFunc(role -> Optional.ofNullable(role.getMetadata().getLabels()).map(
                    labels -> labels.keySet().stream()
                        .filter(key -> key.startsWith(ROLE_AGGREGATE_LABEL_PREFIX))
                        .filter(key -> Boolean.parseBoolean(labels.get(key)))
                        .map(key -> StringUtils.removeStart(key, ROLE_AGGREGATE_LABEL_PREFIX))
                        .collect(Collectors.toSet())).orElseGet(Set::of)
                )
            );
        });

        // plugin.halo.run
        schemeManager.register(Plugin.class, is -> {
            is.add(IndexSpecs.<Plugin, String>single("spec.displayName", String.class)
                .indexFunc(plugin ->
                    Optional.ofNullable(plugin.getSpec())
                        .map(Plugin.PluginSpec::getDisplayName)
                        .orElse(null)
                )
            );
            is.add(IndexSpecs.<Plugin, String>single("spec.description", String.class)
                .indexFunc(plugin -> Optional.ofNullable(plugin.getSpec())
                    .map(Plugin.PluginSpec::getDescription)
                    .orElse(null)
                )
            );
            is.add(IndexSpecs.<Plugin, Boolean>single("spec.enabled", Boolean.class)
                .indexFunc(plugin -> Optional.ofNullable(plugin.getSpec())
                    .map(Plugin.PluginSpec::getEnabled)
                    .orElse(false)
                )
                .nullable(false)
            );
        });
        schemeManager.register(ExtensionPointDefinition.class, indexSpecs -> {
            indexSpecs.add(IndexSpecs.<ExtensionPointDefinition, String>single(
                        "spec.className", String.class
                    )
                    .indexFunc(definition -> definition.getSpec().getClassName())
            );
        });
        schemeManager.register(ExtensionDefinition.class, indexSpecs -> {
            indexSpecs.add(IndexSpecs.<ExtensionDefinition, String>single(
                        "spec.extensionPointName", String.class
                    )
                    .indexFunc(definition -> definition.getSpec().getExtensionPointName())
            );
        });
        schemeManager.register(RoleBinding.class, is -> {
            is.add(IndexSpecs.<RoleBinding, String>single("roleRef.name", String.class)
                .indexFunc(roleBinding -> roleBinding.getRoleRef().getName())
            );
            is.add(IndexSpecs.<RoleBinding, String>multi("subjects", String.class)
                .indexFunc(roleBinding -> roleBinding.getSubjects().stream()
                    .map(RoleBinding.Subject::toString)
                    .collect(Collectors.toSet())
                )
            );
        });
        schemeManager.register(User.class, indexSpecs -> {
            indexSpecs.add(IndexSpecs.<User, String>single("spec.displayName", String.class)
                .indexFunc(user -> user.getSpec().getDisplayName())
            );
            indexSpecs.add(IndexSpecs.<User, String>single("spec.email", String.class)
                .indexFunc(user -> user.getSpec().getEmail())
            );
            indexSpecs.add(IndexSpecs.<User, String>multi(
                        User.USER_RELATED_ROLES_INDEX, String.class
                    )
                    .indexFunc(user -> Optional.ofNullable(user.getMetadata())
                        .map(MetadataOperator::getAnnotations)
                        .map(annotations -> annotations.get(User.ROLE_NAMES_ANNO))
                        .filter(StringUtils::isNotBlank)
                        .map(rolesJson -> JsonUtils.jsonToObject(
                            rolesJson, new TypeReference<Set<String>>() {
                            })
                        )
                        .orElseGet(Set::of)
                    )
            );
            indexSpecs.add(IndexSpecs.<User, Boolean>single("spec.disabled", Boolean.class)
                .indexFunc(user -> requireNonNullElse(user.getSpec().getDisabled(), Boolean.FALSE))
                .nullable(false)
            );
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
            indexSpecs.add(IndexSpecs.<Post, String>single("spec.title", String.class)
                .indexFunc(post -> Optional.ofNullable(post.getSpec())
                    .map(PostSpec::getTitle)
                    .orElse(null)
                )
            );

            indexSpecs.add(IndexSpecs.<Post, String>single("spec.slug", String.class)
                .indexFunc(post -> Optional.ofNullable(post.getSpec())
                    .map(PostSpec::getSlug)
                    .orElse(null)
                )
            );

            indexSpecs.add(IndexSpecs.<Post, Instant>single("spec.publishTime", Instant.class)
                .indexFunc(post -> Optional.ofNullable(post.getSpec())
                    .map(PostSpec::getPublishTime)
                    .orElse(null)
                )
            );
            indexSpecs.add(IndexSpecs.<Post, String>single("spec.owner", String.class)
                .indexFunc(post -> Optional.ofNullable(post.getSpec())
                    .map(PostSpec::getOwner)
                    .orElse(null)
                )
            );
            indexSpecs.add(IndexSpecs.<Post, Boolean>single("spec.deleted", Boolean.class)
                .indexFunc(post -> Optional.ofNullable(post.getSpec())
                    .map(PostSpec::getDeleted)
                    .orElse(false)
                ).nullable(false)
            );
            indexSpecs.add(IndexSpecs.<Post, Boolean>single("spec.pinned", Boolean.class)
                .indexFunc(post ->
                    Optional.ofNullable(post.getSpec())
                        .map(PostSpec::getPinned)
                        .orElse(false)
                )
                .nullable(false)
            );
            indexSpecs.add(IndexSpecs.<Post, Integer>single("spec.priority", Integer.class)
                .indexFunc(post -> Optional.ofNullable(post.getSpec())
                    .map(PostSpec::getPriority)
                    .orElse(0)
                )
                .nullable(false)
            );
            indexSpecs.add(IndexSpecs.<Post, VisibleEnum>single("spec.visible", VisibleEnum.class)
                .indexFunc(post -> Optional.ofNullable(post.getSpec())
                    .map(PostSpec::getVisible)
                    .orElse(null)
                )
            );
            indexSpecs.add(IndexSpecs.<Post, String>multi("spec.tags", String.class)
                .indexFunc(
                    post -> Optional.ofNullable(post.getSpec())
                        .map(PostSpec::getTags)
                        .map(Set::copyOf)
                        .orElse(Set.of())
                )
            );
            indexSpecs.add(IndexSpecs.<Post, String>multi("spec.categories", String.class)
                .indexFunc(post -> Optional.ofNullable(post.getSpec())
                    .map(PostSpec::getCategories)
                    .map(Set::copyOf)
                    .orElse(Set.of())
                )
            );
            indexSpecs.add(IndexSpecs.<Post, String>multi("status.contributors", String.class)
                .indexFunc(post -> Optional.ofNullable(post.getStatus())
                    .map(PostStatus::getContributors)
                    .map(Set::copyOf).orElse(Set.of())
                )
            );
            indexSpecs.add(IndexSpecs.<Post, String>single("status.phase", String.class)
                .indexFunc(post -> Optional.ofNullable(post.getStatus())
                    .map(PostStatus::getPhase)
                    .orElse(null)
                )
            );
            indexSpecs.add(IndexSpecs.<Post, String>single("status.excerpt", String.class)
                .indexFunc(post -> Optional.ofNullable(post.getStatus())
                    .map(PostStatus::getExcerpt)
                    .orElse(null)
                )
            );
            indexSpecs.add(IndexSpecs.<Post, Instant>single("status.lastModifyTime", Instant.class)
                .indexFunc(post -> Optional.ofNullable(post.getStatus())
                    .map(PostStatus::getLastModifyTime)
                    .orElse(null)
                )
            );
            indexSpecs.add(IndexSpecs.<Post, Boolean>single("status.hideFromList", Boolean.class)
                .indexFunc(post -> Optional.ofNullable(post.getStatus())
                    .map(Post.PostStatus::getHideFromList)
                    .orElse(false)
                )
            );
            indexSpecs.add(IndexSpecs.<Post, Boolean>single(
                        Post.REQUIRE_SYNC_ON_STARTUP_INDEX_NAME, Boolean.class
                    )
                    .indexFunc(post -> {
                        var version = post.getMetadata().getVersion();
                        var status = post.getStatus();
                        if (status == null) {
                            return true;
                        }
                        var observedVersion = status.getObservedVersion();
                        return observedVersion == null || observedVersion < version;
                        // do not care about the false case so return null to avoid indexing
                    })
            );
            indexSpecs.add(IndexSpecs.<Post, Long>single("stats.visit", Long.class)
                .indexFunc(post -> Optional.ofNullable(post.getMetadata().getAnnotations())
                    .map(a -> a.get(Post.STATS_ANNO))
                    .filter(StringUtils::isNotBlank)
                    .map(json -> JsonUtils.jsonToObject(json, Stats.class))
                    .map(Stats::getVisit)
                    .map(i -> (long) i)
                    .orElse(0L)
                )
                .nullable(false)
            );
            indexSpecs.add(IndexSpecs.<Post, Long>single("stats.totalComment", Long.class)
                .indexFunc(post -> Optional.ofNullable(post.getMetadata().getAnnotations())
                    .map(a -> a.get(Post.STATS_ANNO))
                    .filter(StringUtils::isNotBlank)
                    .map(json -> JsonUtils.jsonToObject(json, Stats.class))
                    .map(Stats::getTotalComment)
                    .map(i -> (long) i)
                    .orElse(0L)
                )
                .nullable(false)
            );
        });
        schemeManager.register(Category.class, indexSpecs -> {
            indexSpecs.add(IndexSpecs.<Category, String>single("spec.slug", String.class)
                .indexFunc(category -> Optional.ofNullable(category.getSpec())
                    .map(CategorySpec::getSlug)
                    .orElse(null)
                )
            );
            indexSpecs.add(IndexSpecs.<Category, Integer>single("spec.priority", Integer.class)
                .indexFunc(category -> Optional.ofNullable(category.getSpec())
                    .map(CategorySpec::getPriority)
                    .orElse(0)
                )
            );
            indexSpecs.add(IndexSpecs.<Category, String>multi("spec.children", String.class)
                .indexFunc(category -> Optional.ofNullable(category.getSpec())
                    .map(CategorySpec::getChildren)
                    .map(Set::copyOf)
                    .orElse(Set.of())
                )
            );
            indexSpecs.add(IndexSpecs.<Category, Boolean>single("spec.hideFromList", Boolean.class)
                .indexFunc(category -> Optional.ofNullable(category.getSpec())
                    .map(CategorySpec::isHideFromList)
                    .orElse(false)
                )
            );
        });
        schemeManager.register(Tag.class, indexSpecs -> {
            indexSpecs.add(IndexSpecs.<Tag, String>single("spec.displayName", String.class)
                .indexFunc(tag -> Optional.ofNullable(tag.getSpec())
                    .map(TagSpec::getDisplayName)
                    .orElse(null)
                )
            );
            indexSpecs.add(IndexSpecs.<Tag, String>single("spec.slug", String.class)
                .indexFunc(
                    tag -> Optional.ofNullable(tag.getSpec())
                        .map(TagSpec::getSlug)
                        .orElse(null)
                )
            );
            indexSpecs.add(IndexSpecs.<Tag, Integer>single("status.postCount", Integer.class)
                .indexFunc(tag -> Optional.ofNullable(tag.getStatus())
                    .map(TagStatus::getPostCount)
                    .orElse(0)
                )
            );
            indexSpecs.add(IndexSpecs.<Tag, Boolean>single(
                        Tag.REQUIRE_SYNC_ON_STARTUP_INDEX_NAME, Boolean.class
                    )
                    .indexFunc(tag -> {
                        var version = tag.getMetadata().getVersion();
                        var status = tag.getStatus();
                        if (status == null) {
                            return true;
                        }
                        var observedVersion = status.getObservedVersion();
                        return observedVersion == null || observedVersion < version;
                        // do not care about the false case so return null to avoid indexing
                    })
            );
        });
        schemeManager.register(Snapshot.class, indexSpecs -> {
            indexSpecs.add(IndexSpecs.<Snapshot, String>single("spec.subjectRef", String.class)
                .indexFunc(snapshot -> Optional.ofNullable(snapshot.getSpec())
                    .map(Snapshot.SnapShotSpec::getSubjectRef)
                    .map(Snapshot::toSubjectRefKey)
                    .orElse(null)
                )
            );
        });
        schemeManager.register(Comment.class, indexSpecs -> {
            indexSpecs.add(IndexSpecs.<Comment, Instant>single("spec.creationTime", Instant.class)
                .indexFunc(comment -> Optional.ofNullable(comment.getSpec())
                    .map(CommentSpec::getCreationTime)
                    .orElseGet(() -> comment.getMetadata().getCreationTimestamp())
                )
                .nullable(false)
            );
            indexSpecs.add(IndexSpecs.<Comment, Boolean>single("spec.approved", Boolean.class)
                .indexFunc(comment -> Optional.ofNullable(comment.getSpec())
                    .map(CommentSpec::getApproved)
                    .orElse(false)
                )
                .nullable(false)
            );
            indexSpecs.add(IndexSpecs.<Comment, String>single("spec.owner", String.class)
                .indexFunc(comment -> Optional.ofNullable(comment.getSpec())
                    .map(CommentSpec::getOwner)
                    .map(owner -> ownerIdentity(owner.getKind(), owner.getName()))
                    .orElse(null)
                )
                .nullable(true)
            );
            indexSpecs.add(IndexSpecs.<Comment, String>single("spec.subjectRef", String.class)
                .indexFunc(comment -> Optional.ofNullable(comment.getSpec())
                    .map(CommentSpec::getSubjectRef)
                    .map(Comment::toSubjectRefKey)
                    .orElse(null)
                )
                .nullable(true)
            );
            indexSpecs.add(IndexSpecs.<Comment, Boolean>single("spec.top", Boolean.class)
                .indexFunc(comment -> Optional.ofNullable(comment.getSpec())
                    .map(CommentSpec::getTop)
                    .orElse(false)
                )
                .nullable(false)
            );
            indexSpecs.add(IndexSpecs.<Comment, Boolean>single("spec.hidden", Boolean.class)
                .indexFunc(comment -> Optional.ofNullable(comment.getSpec())
                    .map(CommentSpec::getHidden)
                    .orElse(false)
                )
                .nullable(false)
            );
            indexSpecs.add(IndexSpecs.<Comment, Integer>single("spec.priority", Integer.class)
                .indexFunc(comment -> Optional.ofNullable(comment.getSpec())
                    .filter(spec -> Boolean.TRUE.equals(spec.getTop()))
                    .map(Comment.BaseCommentSpec::getPriority)
                    .orElse(0)
                )
                .nullable(false)
            );
            indexSpecs.add(IndexSpecs.<Comment, String>single("spec.raw", String.class)
                .indexFunc(comment -> Optional.ofNullable(comment.getSpec())
                    .map(CommentSpec::getRaw)
                    .orElse(null)
                )
            );
            indexSpecs.add(IndexSpecs.<Comment, Instant>single(
                        "status.lastReplyTime", Instant.class
                    )
                    .indexFunc(comment -> Optional.ofNullable(comment.getStatus())
                        .map(CommentStatus::getLastReplyTime)
                        .orElse(null)
                    )
            );

            indexSpecs.add(IndexSpecs.<Comment, Integer>single("status.replyCount", Integer.class)
                .indexFunc(comment -> Optional.ofNullable(comment.getStatus())
                    .map(CommentStatus::getReplyCount)
                    .orElse(0)
                )
            );
            indexSpecs.add(IndexSpecs.<Comment, Boolean>single(
                        Comment.REQUIRE_SYNC_ON_STARTUP_INDEX_NAME, Boolean.class
                    )
                    .indexFunc(comment -> {
                        var version = comment.getMetadata().getVersion();
                        var status = comment.getStatus();
                        if (status == null) {
                            return true;
                        }
                        var observedVersion = status.getObservedVersion();
                        return observedVersion == null || observedVersion < version;
                        // do not care about the false case so return null to avoid indexing
                    })
            );
        });
        schemeManager.register(Reply.class, indexSpecs -> {
            indexSpecs.add(IndexSpecs.<Reply, Instant>single("spec.creationTime", Instant.class)
                .indexFunc(reply -> Optional.ofNullable(reply.getSpec())
                    .map(ReplySpec::getCreationTime)
                    .orElse(reply.getMetadata().getCreationTimestamp())
                )
            );
            indexSpecs.add(IndexSpecs.<Reply, String>single("spec.commentName", String.class)
                .indexFunc(reply -> Optional.ofNullable(reply.getSpec())
                    .map(ReplySpec::getCommentName)
                    .orElse(null)
                )
            );
            indexSpecs.add(IndexSpecs.<Reply, Boolean>single("spec.hidden", Boolean.class)
                .indexFunc(reply -> Optional.ofNullable(reply.getSpec())
                    .map(ReplySpec::getHidden)
                    .orElse(false)
                )
                .nullable(false)
            );
            indexSpecs.add(IndexSpecs.<Reply, Boolean>single("spec.approved", Boolean.class)
                .indexFunc(reply -> Optional.ofNullable(reply.getSpec())
                    .map(ReplySpec::getApproved)
                    .orElse(false)
                )
                .nullable(false)
            );
            indexSpecs.add(IndexSpecs.<Reply, String>single("spec.owner", String.class)
                .indexFunc(reply -> Optional.ofNullable(reply.getSpec())
                    .map(ReplySpec::getOwner)
                    .map(owner -> ownerIdentity(owner.getKind(), owner.getName()))
                    .orElse(null)
                )
            );
            indexSpecs.add(IndexSpecs.<Reply, Boolean>single(
                        Reply.REQUIRE_SYNC_ON_STARTUP_INDEX_NAME, Boolean.class
                    )
                    .indexFunc(reply -> {
                        var version = reply.getMetadata().getVersion();
                        var status = reply.getStatus();
                        if (status == null) {
                            return true;
                        }
                        var observedVersion = status.getObservedVersion();
                        return observedVersion == null || observedVersion < version;
                        // do not care about the false case so return null to avoid indexing
                    })
            );
        });
        schemeManager.register(SinglePage.class, is -> {
            is.add(IndexSpecs.<SinglePage, Instant>single("spec.publishTime", Instant.class)
                .indexFunc(page -> Optional.ofNullable(page.getSpec())
                    .map(SinglePageSpec::getPublishTime)
                    .orElse(null)
                )
            );
            is.add(IndexSpecs.<SinglePage, String>single("spec.title", String.class)
                .indexFunc(page -> Optional.ofNullable(page.getSpec())
                    .map(SinglePageSpec::getTitle)
                    .orElse(null)
                )
            );
            is.add(IndexSpecs.<SinglePage, String>single("spec.slug", String.class)
                .indexFunc(page -> Optional.ofNullable(page.getSpec())
                    .map(SinglePageSpec::getSlug)
                    .orElse(null)
                )
            );
            is.add(IndexSpecs.<SinglePage, Boolean>single("spec.deleted", Boolean.class)
                .indexFunc(page -> Optional.ofNullable(page.getSpec())
                    .map(SinglePageSpec::getDeleted)
                    .orElse(false)
                )
                .nullable(false)
            );
            is.add(IndexSpecs.<SinglePage, VisibleEnum>single("spec.visible", VisibleEnum.class)
                .indexFunc(page -> Optional.ofNullable(page.getSpec())
                    .map(SinglePageSpec::getVisible)
                    .orElse(null)
                )
            );
            is.add(IndexSpecs.<SinglePage, Boolean>single("spec.pinned", Boolean.class)
                .indexFunc(page -> Optional.ofNullable(page.getSpec())
                    .map(SinglePageSpec::getPinned)
                    .orElse(false)
                )
                .nullable(false)
            );

            is.add(IndexSpecs.<SinglePage, Integer>single("spec.priority", Integer.class)
                .indexFunc(page -> Optional.ofNullable(page.getSpec())
                    .map(SinglePageSpec::getPriority)
                    .orElse(0)
                )
                .nullable(false)
            );

            is.add(IndexSpecs.<SinglePage, String>single("status.excerpt", String.class)
                .indexFunc(page -> Optional.ofNullable(page.getStatus())
                    .map(SinglePage.SinglePageStatus::getExcerpt)
                    .orElse(null)
                )
            );
            is.add(IndexSpecs.<SinglePage, String>single("status.phase", String.class)
                .indexFunc(page -> Optional.ofNullable(page.getStatus())
                    .map(SinglePage.SinglePageStatus::getPhase)
                    .orElse(null)
                )
            );
            is.add(IndexSpecs.<SinglePage, String>multi("status.contributors", String.class)
                .indexFunc(page -> Optional.ofNullable(page.getStatus())
                    .map(SinglePage.SinglePageStatus::getContributors)
                    .map(Set::copyOf)
                    .orElse(Set.of())
                )
            );
        });
        // storage.halo.run
        schemeManager.register(Group.class);
        schemeManager.register(Policy.class);
        schemeManager.register(Attachment.class, indexSpecs -> {
            indexSpecs.add(IndexSpecs.<Attachment, String>single("spec.displayName", String.class)
                .indexFunc(attachment -> Optional.ofNullable(attachment.getSpec())
                    .map(AttachmentSpec::getDisplayName)
                    .orElse(null)
                )
            );
            indexSpecs.add(IndexSpecs.<Attachment, String>single("spec.policyName", String.class)
                .indexFunc(attachment -> Optional.ofNullable(attachment.getSpec())
                    .map(AttachmentSpec::getPolicyName)
                    .orElse(null)
                )
            );
            indexSpecs.add(IndexSpecs.<Attachment, String>single("spec.groupName", String.class)
                .indexFunc(attachment -> Optional.ofNullable(attachment.getSpec())
                    .map(AttachmentSpec::getGroupName)
                    .orElse(null)
                )
            );
            indexSpecs.add(IndexSpecs.<Attachment, String>single("spec.mediaType", String.class)
                .indexFunc(attachment -> Optional.ofNullable(attachment.getSpec())
                    .map(AttachmentSpec::getMediaType)
                    .orElse(null)
                )
            );
            indexSpecs.add(IndexSpecs.<Attachment, String>single("spec.ownerName", String.class)
                .indexFunc(attachment -> Optional.ofNullable(attachment.getSpec())
                    .map(AttachmentSpec::getOwnerName)
                    .orElse(null)
                )
            );
            indexSpecs.add(IndexSpecs.<Attachment, Long>single("spec.size", Long.class)
                .indexFunc(attachment -> Optional.ofNullable(attachment.getSpec())
                    .map(AttachmentSpec::getSize)
                    .orElse(null)
                )
            );
            indexSpecs.add(IndexSpecs.<Attachment, String>single("status.permalink", String.class)
                .indexFunc(attachment -> Optional.ofNullable(attachment.getStatus())
                    .map(AttachmentStatus::getPermalink)
                    .orElse(null)
                )
            );
        });
        schemeManager.register(PolicyTemplate.class);
        schemeManager.register(Thumbnail.class, indexSpec -> {
            indexSpec.add(new IndexSpec()
                // see run.halo.app.core.attachment.ThumbnailMigration
                // .setUnique(true)
                .setName(Thumbnail.ID_INDEX)
                .setIndexFunc(simpleAttribute(Thumbnail.class, Thumbnail::idIndexFunc)));
        });
        schemeManager.register(LocalThumbnail.class, indexSpec -> {
            // make sure image and size are unique
            indexSpec.add(new IndexSpec()
                // see run.halo.app.core.attachment.ThumbnailMigration
                // .setUnique(true)
                .setName(LocalThumbnail.UNIQUE_IMAGE_AND_SIZE_INDEX)
                .setIndexFunc(
                    simpleAttribute(LocalThumbnail.class, LocalThumbnail::uniqueImageAndSize)
                )
            );
            indexSpec.add(new IndexSpec().setName("spec.imageSignature")
                .setIndexFunc(simpleAttribute(LocalThumbnail.class,
                    thumbnail -> thumbnail.getSpec().getImageSignature())
                )
            );
            indexSpec.add(new IndexSpec().setName("spec.thumbSignature").setUnique(true)
                .setIndexFunc(simpleAttribute(LocalThumbnail.class,
                    thumbnail -> thumbnail.getSpec().getThumbSignature())
                )
            );
            indexSpec.add(new IndexSpec().setName("status.phase").setIndexFunc(
                simpleAttribute(LocalThumbnail.class,
                    thumbnail -> Optional.of(thumbnail.getStatus())
                        .map(LocalThumbnail.Status::getPhase)
                        .map(LocalThumbnail.Phase::name)
                        .orElse(null)
                )
            ));
        });
        // metrics.halo.run
        schemeManager.register(Counter.class);
        // auth.halo.run
        schemeManager.register(AuthProvider.class);
        schemeManager.register(UserConnection.class, is -> {
            is.add(IndexSpecs.<UserConnection, String>single("spec.username", String.class)
                .indexFunc(connection -> Optional.ofNullable(connection.getSpec())
                    .map(UserConnectionSpec::getUsername)
                    .orElse(null)
                )
            );
            is.add(IndexSpecs.<UserConnection, String>single("spec.registrationId", String.class)
                .indexFunc(connection -> Optional.ofNullable(connection.getSpec())
                    .map(UserConnectionSpec::getRegistrationId)
                    .orElse(null)
                )
            );
            is.add(IndexSpecs.<UserConnection, String>single("spec.providerUserId", String.class)
                .indexFunc(connection -> Optional.ofNullable(connection.getSpec())
                    .map(UserConnectionSpec::getProviderUserId)
                    .orElse(null)
                )
            );
        });

        // security.halo.run
        schemeManager.register(PersonalAccessToken.class);
        schemeManager.register(RememberMeToken.class, indexSpecs -> {
            indexSpecs.add(IndexSpecs.<RememberMeToken, String>single("spec.series", String.class)
                .indexFunc(token -> Optional.ofNullable(token.getSpec())
                    .map(RememberMeToken.Spec::getSeries)
                    .orElse(null)
                )
            );
            indexSpecs.add(IndexSpecs.<RememberMeToken, String>single("spec.username", String.class)
                .indexFunc(token -> Optional.ofNullable(token.getSpec())
                    .map(RememberMeToken.Spec::getUsername)
                    .orElse(null)
                )
            );
        });
        schemeManager.register(Device.class, indexSpecs -> {
            indexSpecs.add(IndexSpecs.<Device, String>single("spec.principalName", String.class)
                .indexFunc(device -> Optional.ofNullable(device.getSpec())
                    .map(Device.Spec::getPrincipalName)
                    .orElse(null)
                )
            );
        });

        // migration.halo.run
        schemeManager.register(Backup.class);

        // notification.halo.run
        schemeManager.register(ReasonType.class);
        schemeManager.register(Reason.class);
        schemeManager.register(NotificationTemplate.class, indexSpecs -> {
            indexSpecs.add(
                IndexSpecs.<NotificationTemplate, String>single("spec.reasonSelector.reasonType",
                    String.class).indexFunc(template -> Optional.ofNullable(template.getSpec())
                    .map(NotificationTemplate.Spec::getReasonSelector)
                    .map(NotificationTemplate.ReasonSelector::getReasonType)
                    .orElse(null)
                )
            );
        });
        schemeManager.register(Subscription.class, indexSpecs -> {
            indexSpecs.add(
                IndexSpecs.<Subscription, String>single("spec.reason.reasonType", String.class)
                    .indexFunc(
                        sub -> Optional.ofNullable(sub.getSpec()).map(Subscription.Spec::getReason)
                            .map(InterestReason::getReasonType)
                            .orElse(null)
                    )
            );
            indexSpecs.add(
                IndexSpecs.<Subscription, String>single("spec.reason.subject", String.class)
                    .indexFunc(sub -> Optional.ofNullable(sub.getSpec())
                        .map(Subscription.Spec::getReason)
                        .map(InterestReason::getSubject)
                        .map(Object::toString)
                        .orElse(null)
                    )
            );
            indexSpecs.add(
                IndexSpecs.<Subscription, String>single("spec.reason.expression", String.class)
                    .indexFunc(sub -> Optional.ofNullable(sub.getSpec())
                        .map(Subscription.Spec::getReason)
                        .map(InterestReason::getExpression)
                        .orElse(null)
                    )
            );
            indexSpecs.add(IndexSpecs.<Subscription, String>single("spec.subscriber", String.class)
                .indexFunc(sub -> Optional.ofNullable(sub.getSpec())
                    .map(Subscription.Spec::getSubscriber)
                    .map(Object::toString)
                    .orElse(null)
                )
            );
        });
        schemeManager.register(NotifierDescriptor.class);
        schemeManager.register(Notification.class, indexSpecs -> {
            indexSpecs.add(IndexSpecs.<Notification, Boolean>single("spec.unread", Boolean.class)
                .indexFunc(notification -> Optional.ofNullable(notification.getSpec())
                    .map(NotificationSpec::isUnread)
                    .orElse(false)
                )
            );
            indexSpecs.add(IndexSpecs.<Notification, String>single("spec.reason", String.class)
                .indexFunc(notification -> Optional.ofNullable(notification.getSpec())
                    .map(NotificationSpec::getReason)
                    .orElse(null)
                )
            );
            indexSpecs.add(IndexSpecs.<Notification, String>single("spec.recipient", String.class)
                .indexFunc(notification -> Optional.ofNullable(notification.getSpec())
                    .map(NotificationSpec::getRecipient)
                    .orElse(null)
                )
            );
        });
    }

    @Override
    public void stop() {
        if (!running) {
            return;
        }
        running = false;
        schemeManager.schemes().forEach(schemeManager::unregister);
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return InitializationPhase.SCHEME.getPhase();
    }
}
