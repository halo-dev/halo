package run.halo.app.repository;

import org.springframework.lang.NonNull;
import run.halo.app.model.entity.Post;
import run.halo.app.model.entity.Sheet;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.repository.base.BasePostRepository;

import java.util.Optional;

/**
 * Sheet repository.
 *
 * @author johnniang
 * @date 3/22/19
 */
public interface SheetRepository extends BasePostRepository<Sheet> {

    @NonNull
    Optional<Sheet> getByUrlAndStatus(@NonNull String url, @NonNull PostStatus status);
}
