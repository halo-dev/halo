package run.halo.app.handler.migrate;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;
import run.halo.app.model.enums.MigrateType;

/**
 * Migrate handler interface.
 *
 * @author ryanwang
 * @date 2019-10-28
 */
public interface MigrateHandler {

    /**
     * Migrate
     *
     * @param file multipart file must not be null
     */
    void migrate(@NonNull MultipartFile file);

    /**
     * Checks if the given type is supported.
     *
     * @param type migrate type
     * @return true if supported; false or else
     */
    boolean supportType(@Nullable MigrateType type);
}
