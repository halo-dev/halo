package run.halo.app.service.impl;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import run.halo.app.model.entity.ContentPatchLog;
import run.halo.app.model.entity.PostContent;
import run.halo.app.repository.base.BaseContentRepository;
import run.halo.app.service.PostContentPatchLogService;
import run.halo.app.service.PostContentService;

/**
 * @author guqing
 * @since 2022-01-07
 */
@Slf4j
@Service
public class PostContentServiceImpl extends BaseContentServiceImpl<PostContent>
    implements PostContentService {

    private final PostContentPatchLogService postContentPatchLogService;

    protected PostContentServiceImpl(BaseContentRepository<PostContent> baseContentRepository,
        PostContentPatchLogService contentPatchLogService) {
        super(baseContentRepository, contentPatchLogService);
        this.postContentPatchLogService = contentPatchLogService;
    }

    @Override
    public PostContent removeById(Integer sheetId) {
        List<ContentPatchLog> patchLogs = postContentPatchLogService.removeByPostId(sheetId);
        log.debug("Removed post content patch logs: [{}]", patchLogs);

        return super.removeById(sheetId);
    }
}
