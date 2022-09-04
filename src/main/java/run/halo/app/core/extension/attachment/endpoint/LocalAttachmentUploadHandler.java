package run.halo.app.core.extension.attachment.endpoint;

import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static run.halo.app.infra.utils.FileUtils.checkDirectoryTraversal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.core.extension.attachment.Attachment.AttachmentSpec;
import run.halo.app.core.extension.attachment.Constant;
import run.halo.app.core.extension.attachment.Policy;
import run.halo.app.extension.Metadata;
import run.halo.app.infra.properties.HaloProperties;
import run.halo.app.infra.utils.JsonUtils;

@Slf4j
@Component
class LocalAttachmentUploadHandler implements AttachmentHandler {

    private final HaloProperties haloProp;

    public LocalAttachmentUploadHandler(HaloProperties haloProp) {
        this.haloProp = haloProp;
    }

    Path getAttachmentsRoot() {
        return haloProp.getWorkDir().resolve("attachments");
    }

    @Override
    public Mono<Attachment> upload(UploadContext uploadOption) {
        return Mono.just(uploadOption)
            .filter(option -> this.shouldHandle(option.policy()))
            .flatMap(option -> {
                var configMap = option.configMap();
                var settingJson = configMap.getData().getOrDefault("default", "{}");
                var setting = JsonUtils.jsonToObject(settingJson, PolicySetting.class);

                var attachmentsRoot = getAttachmentsRoot();
                var attachmentRoot = attachmentsRoot;
                if (StringUtils.hasText(setting.getLocation())) {
                    attachmentRoot = attachmentsRoot.resolve(setting.getLocation());
                }
                var file = option.file();
                var attachmentPath = attachmentRoot.resolve(file.filename());
                // check the directory traversal before saving
                checkDirectoryTraversal(attachmentsRoot, attachmentPath);

                return Mono.fromRunnable(
                        () -> {
                            try {
                                // init parent folders
                                Files.createDirectories(attachmentPath.getParent());
                            } catch (IOException e) {
                                throw Exceptions.propagate(e);
                            }
                        })
                    .subscribeOn(Schedulers.boundedElastic())
                    // save the attachment
                    .then(DataBufferUtils.write(file.content(), attachmentPath, CREATE_NEW))
                    .then(Mono.fromCallable(() -> {
                        log.info("Wrote attachment {} into {}", file.filename(), attachmentPath);
                        // TODO check the file extension
                        var metadata = new Metadata();
                        metadata.setName(UUID.randomUUID().toString());
                        metadata.setAnnotations(Map.of(Constant.LOCAL_REL_PATH_ANNO_KEY,
                            attachmentsRoot.relativize(attachmentPath).toString()));
                        var spec = new AttachmentSpec();
                        spec.setSize(attachmentPath.toFile().length());
                        file.headers().getContentType();
                        spec.setMediaType(Optional.ofNullable(file.headers().getContentType())
                            .map(MediaType::toString)
                            .orElse(null));
                        spec.setDisplayName(file.filename());
                        var attachment = new Attachment();
                        attachment.setMetadata(metadata);
                        attachment.setSpec(spec);
                        return attachment;
                    }));
            });
    }

    @Override
    public Mono<Attachment> delete(DeleteContext deleteContext) {
        return Mono.just(deleteContext)
            .filter(context -> this.shouldHandle(context.policy()))
            .publishOn(Schedulers.boundedElastic())
            .doOnNext(context -> {
                var attachment = context.attachment();
                log.info("Trying to delete {} from local", attachment.getMetadata().getName());
                var annotations = attachment.getMetadata().getAnnotations();
                if (annotations != null) {
                    var localRelativePath = annotations.get(Constant.LOCAL_REL_PATH_ANNO_KEY);
                    if (StringUtils.hasText(localRelativePath)) {
                        var attachmentsRoot = getAttachmentsRoot();
                        var attachmentPath = attachmentsRoot.resolve(localRelativePath);
                        checkDirectoryTraversal(attachmentsRoot, attachmentPath);

                        // delete it permanently
                        try {
                            log.info("{} is being deleted", attachmentPath);
                            boolean deleted = Files.deleteIfExists(attachmentPath);
                            if (deleted) {
                                log.info("{} was deleted successfully", attachment);
                            } else {
                                log.info("{} was not exist", attachment);
                            }
                        } catch (IOException e) {
                            throw Exceptions.propagate(e);
                        }
                    }
                }
            })
            .map(DeleteContext::attachment);
    }

    private boolean shouldHandle(Policy policy) {
        if (policy == null
            || policy.getSpec() == null
            || policy.getSpec().getTemplateRef() == null) {
            return false;
        }
        return "local".equals(policy.getSpec().getTemplateRef().getName());
    }

    @Data
    public static class PolicySetting {

        private String location;

    }
}
