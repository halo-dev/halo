package run.halo.app.core.attachment.thumbnail;

import java.io.IOException;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.resource.ResourceTransformer;
import org.springframework.web.reactive.resource.ResourceTransformerChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import run.halo.app.core.attachment.ThumbnailSize;

/**
 * A {@link ResourceTransformer} to generate and serve image thumbnails on the fly.
 *
 * @author johnniang
 * @since 2.22.0
 */
@Slf4j
public class ThumbnailResourceTransformer implements ResourceTransformer {

    private final LocalThumbnailService localThumbnailService;

    public ThumbnailResourceTransformer(LocalThumbnailService localThumbnailService) {
        this.localThumbnailService = localThumbnailService;
    }

    @Override
    public Mono<Resource> transform(ServerWebExchange exchange, Resource resource,
        ResourceTransformerChain transformerChain) {
        var width = exchange.getRequest().getQueryParams().getFirst("width");
        if (!StringUtils.hasText(width) || !resource.isFile()) {
            return transformerChain.transform(exchange, resource);
        }
        var extension = StringUtils.getFilenameExtension(resource.getFilename());
        if (!ThumbnailUtils.isSupportedImage(extension)) {
            log.trace("Not a supported image type: {}", extension);
            return transformerChain.transform(exchange, resource);
        }
        var size = ThumbnailSize.fromWidth(width);
        try {
            var source = Path.of(resource.getURI());

            var thumbnailPath = localThumbnailService.resolveThumbnailPath(source, size);
            if (thumbnailPath.isEmpty()) {
                return transformerChain.transform(exchange, resource);
            }

            var thumbnailResource = createThumbnailResource(thumbnailPath.get());
            if (thumbnailResource.isReadable()) {
                log.trace("Thumbnail already exists: {}", thumbnailPath);
                return transformerChain.transform(exchange, thumbnailResource);
            }
            return localThumbnailService.generate(source, size)
                .flatMap(transformed -> transformerChain.transform(exchange, transformed));
        } catch (IOException e) {
            return Mono.error(e);
        }
    }

    /**
     * Creates a {@link Resource} for the given thumbnail path. Mainly for testing purpose.
     *
     * @param thumbnailPath the thumbnail path
     * @return the created resource
     */
    Resource createThumbnailResource(Path thumbnailPath) {
        return new PathResource(thumbnailPath);
    }

}
