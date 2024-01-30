package run.halo.app.infra;

/**
 * {@link ExternalLinkProcessor} to process an in-site link to an external link.
 *
 * @author guqing
 * @see ExternalUrlSupplier
 * @since 2.9.0
 */
public interface ExternalLinkProcessor {

    /**
     * If the link is in-site link, then process it to an external link with
     * {@link ExternalUrlSupplier#getRaw()}, otherwise return the original link.
     *
     * @param link link to process
     * @return processed link or original link
     */
    String processLink(String link);
}
