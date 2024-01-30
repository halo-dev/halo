package run.halo.app.infra;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
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
        if (StringUtils.isBlank(link)) {
            return link;
        }
        if (externalLink == null || !linkInSite(externalLink, link)) {
            return link;
        }

        return append(externalLink.toString(), link);
    }

    String append(String externalLink, String link) {
        return StringUtils.removeEnd(externalLink, "/")
            + StringUtils.prependIfMissing(link, "/");
    }

    boolean linkInSite(@NonNull URL externalUrl, @NonNull String link) {
        if (!PathUtils.isAbsoluteUri(link)) {
            // relative uri is always in site
            return true;
        }
        try {
            URI requestUri = new URI(link);
            return StringUtils.equals(externalUrl.getAuthority(), requestUri.getAuthority());
        } catch (URISyntaxException e) {
            // ignore this link
        }
        return false;
    }
}
