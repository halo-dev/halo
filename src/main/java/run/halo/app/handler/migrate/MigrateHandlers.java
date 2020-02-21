package run.halo.app.handler.migrate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import run.halo.app.exception.FileOperationException;
import run.halo.app.model.enums.MigrateType;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Migrate handler manager.
 *
 * @author ryanwang
 * @date 2019-10-28
 */
@Slf4j
@Component
public class MigrateHandlers {

    /**
     * Migrate handler container.
     */
    private final Collection<MigrateHandler> migrateHandlers = new LinkedList<>();

    public MigrateHandlers(ApplicationContext applicationContext) {
        // Add all migrate handler
        addMigrateHandlers(applicationContext.getBeansOfType(MigrateHandler.class).values());
    }

    @NonNull
    public void upload(@NonNull MultipartFile file, @NonNull MigrateType migrateType) {
        Assert.notNull(file, "Multipart file must not be null");
        Assert.notNull(migrateType, "Migrate type must not be null");

        for (MigrateHandler migrateHandler : migrateHandlers) {
            if (migrateHandler.supportType(migrateType)) {
                migrateHandler.migrate(file);
                return;
            }
        }

        throw new FileOperationException("No available migrate handler to migrate the file").setErrorData(migrateType);
    }

    /**
     * Adds migrate handlers.
     *
     * @param migrateHandlers migrate handler collection
     * @return current migrate handlers
     */
    @NonNull
    private MigrateHandlers addMigrateHandlers(@Nullable Collection<MigrateHandler> migrateHandlers) {
        if (!CollectionUtils.isEmpty(migrateHandlers)) {
            this.migrateHandlers.addAll(migrateHandlers);
        }
        return this;
    }
}
