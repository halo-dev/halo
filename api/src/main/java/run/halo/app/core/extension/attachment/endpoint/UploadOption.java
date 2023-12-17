package run.halo.app.core.extension.attachment.endpoint;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import run.halo.app.core.extension.attachment.Policy;
import run.halo.app.extension.ConfigMap;

public record UploadOption(FilePart file,
                           Policy policy,
                           ConfigMap configMap) implements AttachmentHandler.UploadContext {

    public static UploadOption from(String filename,
        Flux<DataBuffer> content,
        MediaType mediaType,
        Policy policy,
        ConfigMap configMap) {
        var filePart = new SimpleFilePart(filename, content, mediaType);
        return new UploadOption(filePart, policy, configMap);
    }

}
