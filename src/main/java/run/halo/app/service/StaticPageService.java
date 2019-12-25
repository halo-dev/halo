package run.halo.app.service;

/**
 * Static Page service interface.
 *
 * @author ryanwang
 * @date 2019-12-25
 */
public interface StaticPageService {

    /**
     * Static page folder location.
     */
    String PAGES_FOLDER = "static_pages";

    /**
     * Generate pages.
     */
    void generate();
}
