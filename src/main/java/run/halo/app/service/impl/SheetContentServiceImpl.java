package run.halo.app.service.impl;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import run.halo.app.model.entity.ContentPatchLog;
import run.halo.app.model.entity.SheetContent;
import run.halo.app.repository.SheetContentRepository;
import run.halo.app.service.SheetContentPatchLogService;
import run.halo.app.service.SheetContentService;

/**
 * @author guqing
 * @since 2022-01-07
 */
@Slf4j
@Service
public class SheetContentServiceImpl extends BaseContentServiceImpl<SheetContent>
    implements SheetContentService {

    private final SheetContentPatchLogService sheetContentPatchLogService;

    protected SheetContentServiceImpl(SheetContentRepository sheetContentRepository,
        SheetContentPatchLogService sheetContentPatchLogService) {
        super(sheetContentRepository, sheetContentPatchLogService);
        this.sheetContentPatchLogService = sheetContentPatchLogService;
    }

    @Override
    public SheetContent removeById(Integer sheetId) {
        List<ContentPatchLog> patchLogs = sheetContentPatchLogService.removeByPostId(sheetId);
        log.debug("Removed sheet content patch logs: [{}]", patchLogs);

        return super.removeById(sheetId);
    }
}
