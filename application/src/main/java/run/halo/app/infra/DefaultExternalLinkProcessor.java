package run.halo.app.infra;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import run.halo.app.infra.utils.PathUtils;

/**
 * Default implementation of {@link ExternalLinkProcessor}.
 *
 * @author guqing
 * @since 2.9.0
 */
@Component
@RequiredArgsConstructor
public class DefaultExternalLinkProcessor implements ExternalLinkProcessor {
    private final ExternalUrlSupplier externalUrlSupplier;

    @Override
    public String processLink(String link) {
        var externalLink = externalUrlSupplier.getRaw();
        if (StringUtils.isBlank(link) || externalLink == null || PathUtils.isAbsoluteUri(link)) {
            return link;
        }
        return append(externalLink.toString(), link);
    }

    String append(String externalLink, String link) {
        return StringUtils.removeEnd(externalLink, "/")
            + StringUtils.prependIfMissing(link, "/");
    }
}
