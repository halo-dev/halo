package run.halo.app.core.extension.attachment.endpoint;

import static java.nio.file.StandardOpenOption.CREATE_NEW;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.core.extension.attachment.Attachment.AttachmentSpec;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.Ref;
import run.halo.app.infra.properties.HaloProperties;
import run.halo.app.infra.utils.FileNameUtils;

@Component
@Slf4j
public class LocalAttachmentUploadHandler implements AttachmentUploadHandler {

    private final HaloProperties haloProp;

    public LocalAttachmentUploadHandler(HaloProperties haloProp) {
        this.haloProp = haloProp;
    }

    @Override
    public Mono<Attachment> upload(UploadOption uploadOption) {
        return Mono.just(uploadOption)
            .filter(option -> "local".equals(option.policy().getMetadata().getName()))
            .flatMap(option -> {
                var file = option.file();
                var attachmentPath =
                    haloProp.getWorkDir().resolve("attachments").resolve(file.filename());
                // TODO check the file extension

                var metadata = new Metadata();
                metadata.setName(UUID.randomUUID().toString());
                metadata.setLabels(Map.of("storage.halo.run/file-name", file.filename()));
                var spec = new AttachmentSpec();
                spec.setUploadedBy(Ref.of(option.username()));
                spec.setPolicyRef(Ref.of(option.policy()));
                file.headers().getContentType();
                spec.setMediaType(Optional.ofNullable(file.headers().getContentType())
                    .map(MediaType::toString)
                    .orElse(null));
                spec.setDisplayName(FileNameUtils.removeFileExtension(file.filename(), true));
                var attachment = new Attachment();
                attachment.setMetadata(metadata);
                attachment.setSpec(spec);

                log.info("Trying to write attachment into {}", attachmentPath);
                return DataBufferUtils.write(file.content(), attachmentPath, CREATE_NEW)
                    .then(Mono.fromRunnable(() -> spec.setSize(attachmentPath.toFile().length())))
                    .thenReturn(attachment);
            });
    }

}
