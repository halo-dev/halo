package run.halo.app.core.extension.attachment.endpoint;

import lombok.Builder;
import org.jspecify.annotations.Nullable;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import run.halo.app.core.extension.attachment.Group;
import run.halo.app.core.extension.attachment.Policy;
import run.halo.app.extension.ConfigMap;

@Builder
public record UploadOption(FilePart file,
                           Policy policy,
                           ConfigMap configMap,
                           @Nullable Group group) implements AttachmentHandler.UploadContext {

    public static UploadOption from(String filename,
        Flux<DataBuffer> content,
        MediaType mediaType,
        Policy policy,
        ConfigMap configMap) {
        var filePart = new SimpleFilePart(filename, content, mediaType);
        return new UploadOption(filePart, policy, configMap, null);
    }

}
