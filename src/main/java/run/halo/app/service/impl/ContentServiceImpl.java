package run.halo.app.service.impl;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import run.halo.app.exception.NotFoundException;
import run.halo.app.model.entity.Content;
import run.halo.app.model.entity.Content.PatchedContent;
import run.halo.app.model.entity.ContentPatchLog;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.repository.ContentRepository;
import run.halo.app.service.ContentPatchLogService;
import run.halo.app.service.ContentService;
import run.halo.app.service.base.AbstractCrudService;

/**
 * Base content service implementation.
 *
 * @author guqing
 * @date 2022-01-07
 */
@Service
public class ContentServiceImpl extends AbstractCrudService<Content, Integer>
    implements ContentService {

    private final ContentRepository contentRepository;

    private final ContentPatchLogService contentPatchLogService;

    protected ContentServiceImpl(ContentRepository contentRepository,
        ContentPatchLogService contentPatchLogService) {
        super(contentRepository);
        this.contentRepository = contentRepository;
        this.contentPatchLogService = contentPatchLogService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOrUpdateDraftBy(Integer postId, String content,
        String originalContent) {
        Assert.notNull(postId, "The postId must not be null.");
        // First, we need to save the contentPatchLog
        ContentPatchLog contentPatchLog =
            contentPatchLogService.createOrUpdate(postId, content, originalContent);

        // then update the value of headPatchLogId field.
        Optional<Content> savedContentOptional = contentRepository.findById(postId);
        if (savedContentOptional.isPresent()) {
            Content savedContent = savedContentOptional.get();
            savedContent.setHeadPatchLogId(contentPatchLog.getId());
            contentRepository.save(savedContent);
            return;
        }

        // If the content record does not exist, it needs to be created
        Content postContent = new Content();
        postContent.setPatchLogId(contentPatchLog.getId());
        postContent.setContent(content);
        postContent.setOriginalContent(originalContent);
        postContent.setId(postId);
        postContent.setStatus(PostStatus.DRAFT);
        postContent.setHeadPatchLogId(contentPatchLog.getId());
        contentRepository.save(postContent);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Content publishContent(Integer postId) {
        ContentPatchLog contentPatchLog = contentPatchLogService.getDraftByPostId(postId);
        if (contentPatchLog == null) {
            return contentRepository.getById(postId);
        }
        contentPatchLog.setStatus(PostStatus.PUBLISHED);
        contentPatchLog.setPublishTime(new Date());
        contentPatchLogService.create(contentPatchLog);

        Content postContent = getById(postId);
        postContent.setPatchLogId(contentPatchLog.getId());
        postContent.setStatus(PostStatus.PUBLISHED);

        PatchedContent patchedContent = contentPatchLogService.applyPatch(contentPatchLog);
        postContent.setContent(patchedContent.getContent());
        postContent.setOriginalContent(patchedContent.getOriginalContent());

        contentRepository.save(postContent);

        return postContent;
    }

    @Override
    @NonNull
    public Content getById(@NonNull Integer postId) {
        Assert.notNull(postId, "The postId must not be null.");
        return contentRepository.findById(postId)
            .orElseThrow(() -> new NotFoundException("content was not found or has been deleted"));
    }

    @Override
    public Boolean draftingInProgress(Integer postId) {
        ContentPatchLog draft = contentPatchLogService.getDraftByPostId(postId);
        return Objects.nonNull(draft);
    }
}
