package run.halo.app.core.extension.attachment.endpoint;

import java.nio.file.Path;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * SimpleFilePart is an adapter of simple data for uploading.
 *
 * @param filename is name of the attachment file.
 * @param content is binary data of the attachment file.
 * @param mediaType is media type of the attachment file.
 */
record SimpleFilePart(
    String filename,
    Flux<DataBuffer> content,
    MediaType mediaType
) implements FilePart {
    @Override
    public Mono<Void> transferTo(Path dest) {
        return DataBufferUtils.write(content(), dest);
    }

    @Override
    public String name() {
        return filename();
    }

    @Override
    public HttpHeaders headers() {
        var headers = new HttpHeaders();
        headers.setContentType(mediaType);
        return HttpHeaders.readOnlyHttpHeaders(headers);
    }
}
