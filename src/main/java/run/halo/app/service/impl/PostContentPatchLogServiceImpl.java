package run.halo.app.service.impl;

import org.springframework.stereotype.Service;
import run.halo.app.repository.ContentPatchLogRepository;
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

    public PostContentPatchLogServiceImpl(
        ContentPatchLogRepository contentPatchLogRepository) {
        super(contentPatchLogRepository);
    }
}
