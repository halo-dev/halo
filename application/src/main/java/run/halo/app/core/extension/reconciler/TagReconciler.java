package run.halo.app.core.extension.reconciler;

import static run.halo.app.extension.ExtensionUtil.addFinalizers;
import static run.halo.app.extension.ExtensionUtil.removeFinalizers;
import static run.halo.app.extension.index.query.QueryFactory.equal;

import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Component;
import run.halo.app.content.permalinks.TagPermalinkPolicy;
import run.halo.app.core.extension.content.Constant;
import run.halo.app.core.extension.content.Tag;
import run.halo.app.extension.DefaultExtensionMatcher;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ExtensionUtil;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.router.selector.FieldSelector;

/**
 * Reconciler for {@link Tag}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
@RequiredArgsConstructor
public class TagReconciler implements Reconciler<Reconciler.Request> {
    static final String FINALIZER_NAME = "tag-protection";
    private final ExtensionClient client;
    private final TagPermalinkPolicy tagPermalinkPolicy;

    @Override
    public Result reconcile(Request request) {
        client.fetch(Tag.class, request.name())
            .ifPresent(tag -> {
                if (ExtensionUtil.isDeleted(tag)) {
                    if (removeFinalizers(tag.getMetadata(), Set.of(FINALIZER_NAME))) {
                        client.update(tag);
                    }
                    return;
                }

                addFinalizers(tag.getMetadata(), Set.of(FINALIZER_NAME));

                Map<String, String> annotations = MetadataUtil.nullSafeAnnotations(tag);

                String newPattern = tagPermalinkPolicy.pattern();
                annotations.put(Constant.PERMALINK_PATTERN_ANNO, newPattern);

                String permalink = tagPermalinkPolicy.permalink(tag);
                var status = tag.getStatusOrDefault();
                status.setPermalink(permalink);

                // Update the observed version.
                status.setObservedVersion(tag.getMetadata().getVersion() + 1);

                client.update(tag);
            });
        return Result.doNotRetry();
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder
            .extension(new Tag())
            .onAddMatcher(DefaultExtensionMatcher.builder(client, Tag.GVK)
                .fieldSelector(FieldSelector.of(
                    equal(Tag.REQUIRE_SYNC_ON_STARTUP_INDEX_NAME, BooleanUtils.TRUE))
                )
                .build()
            )
            .build();
    }
}
