package run.halo.app.service;

import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartFile;

/**
 * Recovery service interface.
 *
 * @author johnniang
 * @date 2019-04-26
 */
@Deprecated
public interface RecoveryService {

    /**
     * Migrates from halo version 0.4.3.
     *
     * @param file multipart file must not be null
     */
    void migrateFromV0_4_3(@NonNull MultipartFile file);
}
