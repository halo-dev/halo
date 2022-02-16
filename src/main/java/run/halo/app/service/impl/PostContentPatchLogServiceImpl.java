package run.halo.app.service.impl;

import java.util.Optional;
import org.springframework.stereotype.Service;
import run.halo.app.model.entity.PostContent;
import run.halo.app.repository.ContentPatchLogRepository;
import run.halo.app.repository.PostContentRepository;
import run.halo.app.service.PostContentPatchLogService;

/**
 * Post content patch log service.
 *
 * @author guqing
 * @since 2022-01-07
 */
@Service
public class PostContentPatchLogServiceImpl extends BaseContentPatchLogServiceImpl
    implements PostContentPatchLogService {

    private final PostContentRepository postContentRepository;

    public PostContentPatchLogServiceImpl(
        ContentPatchLogRepository contentPatchLogRepository,
        PostContentRepository postContentRepository) {
        super(contentPatchLogRepository);
        this.postContentRepository = postContentRepository;
    }

    @Override
    protected Optional<PostContent> getContentByPostId(Integer postId) {
        return postContentRepository.findById(postId);
    }
}
