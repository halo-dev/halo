package run.halo.app.core.attachment.reconciler;

import static org.springframework.data.domain.Sort.Order.desc;
import static run.halo.app.core.attachment.extension.LocalThumbnail.REQUEST_TO_GENERATE_ANNO;
import static run.halo.app.extension.MetadataUtil.nullSafeAnnotations;
import static run.halo.app.extension.index.query.QueryFactory.and;
import static run.halo.app.extension.index.query.QueryFactory.equal;
import static run.halo.app.extension.index.query.QueryFactory.isNull;
import static run.halo.app.infra.utils.FileUtils.checkDirectoryTraversal;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import run.halo.app.core.attachment.AttachmentRootGetter;
import run.halo.app.core.attachment.AttachmentUtils;
import run.halo.app.core.attachment.LocalThumbnailService;
import run.halo.app.core.attachment.ThumbnailGenerator;
import run.halo.app.core.attachment.extension.LocalThumbnail;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.core.extension.attachment.Constant;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ExtensionUtil;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.infra.ExternalLinkProcessor;

@Slf4j
@Component
@RequiredArgsConstructor
public class LocalThumbnailsReconciler implements Reconciler<Reconciler.Request> {
    private final LocalThumbnailService localThumbnailService;
    private final ExtensionClient client;
    private final ExternalLinkProcessor externalLinkProcessor;
    private final AttachmentRootGetter attachmentRootGetter;

    @Override
    public Result reconcile(Request request) {
        client.fetch(LocalThumbnail.class, request.name())
            .ifPresent(thumbnail -> {
                if (ExtensionUtil.isDeleted(thumbnail)) {
                    return;
                }
                if (shouldGenerate(thumbnail)) {
                    requestGenerateThumbnail(thumbnail);
                    nullSafeAnnotations(thumbnail).remove(REQUEST_TO_GENERATE_ANNO);
                    client.update(thumbnail);
                }
            });
        return Result.doNotRetry();
    }

    private boolean shouldGenerate(LocalThumbnail thumbnail) {
        var annotations = nullSafeAnnotations(thumbnail);
        return annotations.containsKey(REQUEST_TO_GENERATE_ANNO)
            || thumbnailFileNotExists(thumbnail);
    }

    private boolean thumbnailFileNotExists(LocalThumbnail thumbnail) {
        var filePath = localThumbnailService.toFilePath(thumbnail.getSpec().getFilePath());
        return !Files.exists(filePath);
    }

    void requestGenerateThumbnail(LocalThumbnail thumbnail) {
        // If the thumbnail generation has failed, we should not retry it
        if (isGenerationFailed(thumbnail)) {
            return;
        }
        var imageUri = thumbnail.getSpec().getImageUri();
        var filePath = localThumbnailService.toFilePath(thumbnail.getSpec().getFilePath());
        if (Files.exists(filePath)) {
            return;
        }
        var generator = new ThumbnailGenerator(thumbnail.getSpec().getSize(), filePath);
        var imageUrlOpt = toImageUrl(imageUri);
        if (imageUrlOpt.isEmpty()) {
            if (tryGenerateByAttachment(imageUri, generator)) {
                thumbnail.getStatus().setPhase(LocalThumbnail.Phase.SUCCEEDED);
            } else {
                log.debug("Failed to parse image URL，please check external-url configuration for "
                    + "record: {}", thumbnail.getMetadata().getName());
                thumbnail.getStatus().setPhase(LocalThumbnail.Phase.FAILED);
            }
            return;
        }
        var imageUrl = imageUrlOpt.get();
        if (generateThumbnail(thumbnail, imageUrl, generator)) {
            thumbnail.getStatus().setPhase(LocalThumbnail.Phase.SUCCEEDED);
        } else {
            thumbnail.getStatus().setPhase(LocalThumbnail.Phase.FAILED);
        }
    }

    private boolean isGenerationFailed(LocalThumbnail thumbnail) {
        return LocalThumbnail.Phase.FAILED.equals(thumbnail.getStatus().getPhase());
    }

    private boolean generateThumbnail(LocalThumbnail thumbnail, URL imageUrl,
        ThumbnailGenerator generator) {
        return tryGenerateByAttachment(thumbnail.getSpec().getImageUri(), generator)
            || tryGenerateByUrl(imageUrl, generator);
    }

    private boolean tryGenerate(String resourceIdentifier, Runnable generateAction) {
        try {
            generateAction.run();
            return true;
        } catch (Throwable e) {
            log.debug("Failed to generate thumbnail for: {}", resourceIdentifier, e);
            return false;
        }
    }

    private boolean tryGenerateByUrl(URL imageUrl, ThumbnailGenerator generator) {
        return tryGenerate(imageUrl.toString(), () -> {
            log.debug("Generating thumbnail for image URL: {}", imageUrl);
            generator.generate(imageUrl);
        });
    }

    private boolean tryGenerateByAttachment(String imageUri, ThumbnailGenerator generator) {
        return fetchAttachmentFilePath(imageUri)
            .map(path -> tryGenerate(imageUri, () -> {
                log.debug("Generating thumbnail for attachment file path: {}", path);
                generator.generate(path);
            }))
            .orElse(false);
    }

    Optional<URL> toImageUrl(String imageUriStr) {
        var imageUri = URI.create(imageUriStr);
        try {
            var url = new URL(externalLinkProcessor.processLink(imageUri.toString()));
            return Optional.of(url);
        } catch (MalformedURLException e) {
            // Ignore
        }
        return Optional.empty();
    }

    Optional<Path> fetchAttachmentFilePath(String imageUri) {
        return fetchAttachmentByPermalink(imageUri)
            .filter(AttachmentUtils::isImage)
            .map(attachment -> {
                var annotations = nullSafeAnnotations(attachment);
                var localRelativePath = annotations.get(Constant.LOCAL_REL_PATH_ANNO_KEY);
                if (StringUtils.isBlank(localRelativePath)) {
                    return null;
                }
                var attachmentsRoot = attachmentRootGetter.get();
                var filePath = attachmentsRoot.resolve(localRelativePath);
                checkDirectoryTraversal(attachmentsRoot, filePath);
                return filePath;
            });
    }

    Optional<Attachment> fetchAttachmentByPermalink(String permalink) {
        var listOptions = ListOptions.builder()
            .fieldQuery(and(
                equal("status.permalink", permalink),
                isNull("metadata.deletionTimestamp")
            ))
            .build();
        var pageRequest = PageRequestImpl.ofSize(1)
            .withSort(Sort.by(desc("metadata.creationTimestamp")));
        return client.listBy(Attachment.class, listOptions, pageRequest)
            .get()
            .findFirst();
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder
            .extension(new LocalThumbnail())
            .syncAllOnStart(true)
            .build();
    }
}
