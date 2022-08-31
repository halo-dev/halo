package run.halo.app.core.extension.attachment.endpoint;

import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static run.halo.app.infra.utils.FileUtils.checkDirectoryTraversal;

import java.io.IOException;
import java.nio.file.Files;
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
import run.halo.app.extension.Metadata;
import run.halo.app.extension.Ref;
import run.halo.app.infra.properties.HaloProperties;
import run.halo.app.infra.utils.JsonUtils;

@Slf4j
@Component
public class LocalAttachmentUploadHandler implements AttachmentHandler {

    private final HaloProperties haloProp;

    public LocalAttachmentUploadHandler(HaloProperties haloProp) {
        this.haloProp = haloProp;
    }

    @Override
    public Mono<Attachment> upload(UploadOption uploadOption) {
        return Mono.just(uploadOption)
            .filter(this::shouldHandle)
            .flatMap(option -> {
                var configMap = option.configMap();
                var settingJson = configMap.getData().getOrDefault("default", "{}");
                var setting = JsonUtils.jsonToObject(settingJson, PolicySetting.class);

                var attachmentsRoot = haloProp.getWorkDir().resolve("attachments");
                var attachmentRoot = attachmentsRoot;
                if (StringUtils.hasText(setting.getLocation())) {
                    attachmentRoot = attachmentsRoot.resolve(setting.getLocation());
                }
                var file = option.file();
                var attachmentPath = attachmentRoot.resolve(file.filename());
                // check the directory traversal before saving
                checkDirectoryTraversal(attachmentsRoot, attachmentPath);

                return Mono.fromRunnable(() -> {
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
                        spec.setUploadedBy(Ref.of(option.username()));
                        spec.setPolicyRef(Ref.of(option.policy()));
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

    private boolean shouldHandle(UploadOption option) {
        var spec = option.policy().getSpec();
        return spec != null && spec.getTemplateRef() != null
            && "local".equals(spec.getTemplateRef().getName());
    }

    @Data
    public static class PolicySetting {

        private String location;

    }
}
