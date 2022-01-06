package run.halo.app.service.impl;

import org.springframework.stereotype.Service;
import run.halo.app.model.entity.PostContent;
import run.halo.app.repository.ContentPatchLogRepository;
import run.halo.app.repository.PostRepository;
import run.halo.app.repository.base.BaseContentRepository;
import run.halo.app.service.PostContentPatchLogService;

/**
 * @author guqing
 * @since 2022-01-07
 */
@Service
public class PostContentPatchLogServiceImpl extends BaseContentPatchLogServiceImpl<PostContent>
    implements PostContentPatchLogService {

    public PostContentPatchLogServiceImpl(
        ContentPatchLogRepository contentPatchLogRepository,
        PostRepository postRepository,
        BaseContentRepository<PostContent> contentRepository) {
        super(contentPatchLogRepository, postRepository, contentRepository);
    }
}
