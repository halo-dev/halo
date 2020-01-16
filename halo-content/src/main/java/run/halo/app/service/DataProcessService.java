package run.halo.app.service;

import org.springframework.lang.NonNull;

/**
 * Data process service interface.
 *
 * @author ryanwang
 * @date 2019-12-29
 */
public interface DataProcessService {

    /**
     * Replace all url.
     *
     * @param oldUrl old url must not be null.
     * @param newUrl new url must not be null.
     */
    void replaceAllUrl(@NonNull String oldUrl, @NonNull String newUrl);
}
