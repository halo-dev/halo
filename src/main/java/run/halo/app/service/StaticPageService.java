package run.halo.app.service;

import run.halo.app.model.support.StaticPageFile;

import java.nio.file.Path;
import java.util.List;

import static run.halo.app.model.support.HaloConst.FILE_SEPARATOR;
import static run.halo.app.model.support.HaloConst.TEMP_DIR;
import static run.halo.app.utils.HaloUtils.ensureSuffix;

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


    String STATIC_PAGE_PACK_DIR = ensureSuffix(TEMP_DIR, FILE_SEPARATOR) + "static-pages-pack" + FILE_SEPARATOR;

    String[] USELESS_FILE_SUFFIX = {"ftl", "md", "yaml", "yml", "gitignore"};

    /**
     * Generate pages.
     */
    void generate();

    /**
     * Deploy static pages.
     */
    void deploy();

    /**
     * Zip static pages directory.
     *
     * @return zip path
     */
    Path zipStaticPagesDirectory();

    /**
     * List file of generated static page.
     *
     * @return a list of generated static page.
     */
    List<StaticPageFile> listFile();
}
