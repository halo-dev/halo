package run.halo.app.service.impl;

import java.util.Optional;
import org.springframework.stereotype.Service;
import run.halo.app.model.entity.SheetContent;
import run.halo.app.repository.ContentPatchLogRepository;
import run.halo.app.repository.SheetContentRepository;
import run.halo.app.service.SheetContentPatchLogService;

/**
 * Sheet content patch log service implementation.
 *
 * @author guqing
 * @date 2022-01-07
 */
@Service
public class SheetContentPatchLogServiceImpl extends BaseContentPatchLogServiceImpl
    implements SheetContentPatchLogService {

    private final SheetContentRepository sheetContentRepository;

    public SheetContentPatchLogServiceImpl(
        ContentPatchLogRepository contentPatchLogRepository,
        SheetContentRepository sheetContentRepository) {
        super(contentPatchLogRepository);
        this.sheetContentRepository = sheetContentRepository;
    }

    @Override
    protected Optional<SheetContent> getContentByPostId(Integer postId) {
        return sheetContentRepository.findById(postId);
    }
}
