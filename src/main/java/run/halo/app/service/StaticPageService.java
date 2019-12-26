package run.halo.app.service;

import run.halo.app.model.support.StaticPageFile;

import java.util.List;

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

    /**
     * Deploy static pages.
     */
    void deploy();

    /**
     * List file of generated static page.
     *
     * @return a list of generated static page.
     */
    List<StaticPageFile> listFile();
}
