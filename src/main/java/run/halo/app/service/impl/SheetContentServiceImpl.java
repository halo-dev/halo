package run.halo.app.service.impl;

import org.springframework.stereotype.Service;
import run.halo.app.model.entity.SheetContent;
import run.halo.app.repository.SheetContentRepository;
import run.halo.app.service.SheetContentPatchLogService;
import run.halo.app.service.SheetContentService;

/**
 * @author guqing
 * @since 2022-01-07
 */
@Service
public class SheetContentServiceImpl extends BaseContentServiceImpl<SheetContent>
    implements SheetContentService {

    protected SheetContentServiceImpl(SheetContentRepository sheetContentRepository,
        SheetContentPatchLogService sheetContentPatchLogService) {
        super(sheetContentRepository, sheetContentPatchLogService);
    }
}
