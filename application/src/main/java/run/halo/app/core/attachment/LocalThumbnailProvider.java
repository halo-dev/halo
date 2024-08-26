package run.halo.app.core.attachment;

import java.net.URI;
import java.net.URL;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;
import run.halo.app.infra.ExternalUrlSupplier;

@Component
@RequiredArgsConstructor
public class LocalThumbnailProvider implements ThumbnailProvider {
    private final ExternalUrlSupplier externalUrlSupplier;
    private final LocalThumbnailService localThumbnailService;

    @Override
    public Mono<URI> generate(ThumbnailContext context) {
        return localThumbnailService.create(context.getImageUrl(), context.getSize())
            .map(localThumbnail -> localThumbnail.getSpec().getThumbnailUri())
            .map(URI::create);
    }

    @Override
    public Mono<Void> delete(URL imageUrl) {
        Assert.notNull(imageUrl, "Image URL must not be null");
        return localThumbnailService.delete(URI.create(imageUrl.toString()));
    }

    @Override
    public Mono<Boolean> supports(ThumbnailContext context) {
        var imageUrl = context.getImageUrl();
        var externalUrl = externalUrlSupplier.getRaw();
        return Mono.fromSupplier(() -> externalUrl != null
            && isSameOrigin(imageUrl, externalUrl));
    }

    private boolean isSameOrigin(URL imageUrl, URL externalUrl) {
        return StringUtils.equals(imageUrl.getHost(), externalUrl.getHost())
            && imageUrl.getPort() == externalUrl.getPort();
    }
}
