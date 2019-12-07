package run.halo.app.service;

import run.halo.app.model.support.StaticFile;

import java.util.List;

/**
 * Static storage service interface class.
 *
 * @author ryanwang
 * @date 2019-12-06
 */
public interface StaticStorageService {

    /**
     * Static folder location.
     */
    String STATIC_FOLDER = "static";

    /**
     * Lists static folder.
     *
     * @return List<StaticFile>
     */
    List<StaticFile> listStaticFolder();
}
