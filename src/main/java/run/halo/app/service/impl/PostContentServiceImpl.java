package run.halo.app.service.impl;

import org.springframework.stereotype.Service;
import run.halo.app.model.entity.PostContent;
import run.halo.app.repository.base.BaseContentRepository;
import run.halo.app.service.PostContentPatchLogService;
import run.halo.app.service.PostContentService;

/**
 * @author guqing
 * @since 2022-01-07
 */
@Service
public class PostContentServiceImpl extends BaseContentServiceImpl<PostContent>
    implements PostContentService {

    protected PostContentServiceImpl(BaseContentRepository<PostContent> baseContentRepository,
        PostContentPatchLogService contentPatchLogService) {
        super(baseContentRepository, contentPatchLogService);
    }
}
