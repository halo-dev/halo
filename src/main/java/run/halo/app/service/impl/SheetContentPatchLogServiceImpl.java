package run.halo.app.service.impl;

import org.springframework.stereotype.Service;
import run.halo.app.model.entity.SheetContent;
import run.halo.app.repository.ContentPatchLogRepository;
import run.halo.app.repository.PostRepository;
import run.halo.app.repository.SheetContentRepository;
import run.halo.app.service.SheetContentPatchLogService;

/**
 * Sheet content patch log service implementation.
 *
 * @author guqing
 * @date 2022-01-07
 */
@Service
public class SheetContentPatchLogServiceImpl extends BaseContentPatchLogServiceImpl<SheetContent>
    implements SheetContentPatchLogService {

    public SheetContentPatchLogServiceImpl(
        ContentPatchLogRepository contentPatchLogRepository,
        PostRepository postRepository, SheetContentRepository sheetContentRepository) {
        super(contentPatchLogRepository, postRepository, sheetContentRepository);
    }
}
