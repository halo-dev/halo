package run.halo.app.handler.migrate;

import org.springframework.web.multipart.MultipartFile;
import run.halo.app.model.enums.MigrateType;

/**
 * Cnblogs(https://cnblogs.com) migrate handler.
 *
 * @author ryanwang
 * @date 2019-10-30
 */
public class CnBlogsMigrateHandler implements MigrateHandler {

    @Override
    public void migrate(MultipartFile file) {
        // TODO
    }

    @Override
    public boolean supportType(MigrateType type) {
        return MigrateType.CNBLOGS.equals(type);
    }
}
