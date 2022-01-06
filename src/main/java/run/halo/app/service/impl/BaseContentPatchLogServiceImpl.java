package run.halo.app.service.impl;

import java.util.Objects;
import org.springframework.data.domain.Example;
import org.springframework.util.Assert;
import run.halo.app.exception.NotFoundException;
import run.halo.app.model.entity.BaseContent;
import run.halo.app.model.entity.BaseContent.ContentDiff;
import run.halo.app.model.entity.BaseContent.PatchedContent;
import run.halo.app.model.entity.ContentPatchLog;
import run.halo.app.model.entity.Post;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.repository.ContentPatchLogRepository;
import run.halo.app.repository.PostRepository;
import run.halo.app.repository.base.BaseContentRepository;
import run.halo.app.service.base.BaseContentPatchLogService;
import run.halo.app.utils.PatchUtils;

/**
 * Content patch log service.
 *
 * @author guqing
 * @since 2022-01-04
 */
public abstract class BaseContentPatchLogServiceImpl<CONTENT extends BaseContent> implements
    BaseContentPatchLogService {

    /**
     * base version of content patch log.
     */
    public static final int BASE_VERSION = 1;

    private final ContentPatchLogRepository contentPatchLogRepository;
    private final PostRepository postRepository;
    private final BaseContentRepository<CONTENT> contentRepository;

    public BaseContentPatchLogServiceImpl(ContentPatchLogRepository contentPatchLogRepository,
        PostRepository postRepository, BaseContentRepository<CONTENT> contentRepository) {
        this.contentPatchLogRepository = contentPatchLogRepository;
        this.postRepository = postRepository;
        this.contentRepository = contentRepository;
    }

    @Override
    public ContentPatchLog createOrUpdate(Integer postId, String content, String originalContent) {
        if (existDraftBy(postId)) {
            return updateDraftBy(postId, content, originalContent);
        }
        return createDraftContent(postId, content, originalContent);
    }

    private ContentPatchLog createDraftContent(Integer postId, String aContent,
        String anOriginalContent) {
        Post post = postRepository.getById(postId);
        BaseContent content = contentRepository.getById(postId);

        ContentPatchLog contentPatchLog = buildPatchLog(post, aContent, anOriginalContent);
        contentPatchLog.setSourceId(content.getPatchLogId());

        contentPatchLogRepository.save(contentPatchLog);
        return contentPatchLog;
    }

    private ContentPatchLog buildPatchLog(Post post, String content, String originalContent) {
        final Integer postId = post.getId();
        ContentPatchLog contentPatchLog = new ContentPatchLog();
        if (Objects.equals(post.getVersion(), BASE_VERSION) && PostStatus.DRAFT.equals(
            post.getStatus())) {
            contentPatchLog.setContentDiff(content);
            contentPatchLog.setOriginalContentDiff(originalContent);
        } else {
            ContentDiff contentDiff = generateDiff(postId, content, originalContent);
            contentPatchLog.setContentDiff(contentDiff.getDiff());
            contentPatchLog.setOriginalContentDiff(contentDiff.getOriginalDiff());
        }
        contentPatchLog.setPostId(postId);
        contentPatchLog.setStatus(PostStatus.DRAFT);
        ContentPatchLog latestPatchLog =
            contentPatchLogRepository.findFirstByPostIdOrderByVersionDesc(postId);
        if (latestPatchLog != null) {
            contentPatchLog.setVersion(latestPatchLog.getVersion() + 1);
        } else {
            contentPatchLog.setVersion(BASE_VERSION);
        }

        return contentPatchLog;
    }

    private boolean existDraftBy(Integer postId) {
        ContentPatchLog contentPatchLog = new ContentPatchLog();
        contentPatchLog.setPostId(postId);
        contentPatchLog.setStatus(PostStatus.DRAFT);
        Example<ContentPatchLog> example = Example.of(contentPatchLog);
        return contentPatchLogRepository.exists(example);
    }

    private ContentPatchLog updateDraftBy(Integer postId, String content, String originalContent) {
        ContentPatchLog draftPatchLog =
            contentPatchLogRepository.findFirstByPostIdAndStatusOrderByVersionDesc(postId,
                PostStatus.DRAFT);
        // Is the draft version 1
        if (Objects.equals(draftPatchLog.getVersion(), BASE_VERSION)) {
            // If it is V1, modify the content directly.
            draftPatchLog.setContentDiff(content);
            draftPatchLog.setOriginalContentDiff(originalContent);
            contentPatchLogRepository.save(draftPatchLog);
            return draftPatchLog;
        }
        // Generate content diff.
        ContentDiff contentDiff = generateDiff(postId, content, originalContent);
        draftPatchLog.setContentDiff(contentDiff.getDiff());
        draftPatchLog.setOriginalContentDiff(contentDiff.getOriginalDiff());
        contentPatchLogRepository.save(draftPatchLog);
        return draftPatchLog;
    }

    @Override
    public PatchedContent applyPatch(ContentPatchLog patchLog) {
        Assert.notNull(patchLog, "The contentRecord must not be null.");
        Assert.notNull(patchLog.getVersion(), "The contentRecord.version must not be null.");
        Assert.notNull(patchLog.getPostId(), "The contentRecord.postId must not be null.");

        PatchedContent patchedContent = new PatchedContent();
        if (patchLog.getVersion() == BASE_VERSION) {
            patchedContent.setContent(patchLog.getContentDiff());
            patchedContent.setOriginalContent(patchLog.getOriginalContentDiff());
            return patchedContent;
        }

        ContentPatchLog baseContentRecord =
            contentPatchLogRepository.findByPostIdAndVersion(patchLog.getPostId(), BASE_VERSION);

        String content = PatchUtils.restoreContent(patchLog.getContentDiff(),
            baseContentRecord.getContentDiff());
        patchedContent.setContent(content);

        String originalContent = PatchUtils.restoreContent(patchLog.getOriginalContentDiff(),
            baseContentRecord.getOriginalContentDiff());
        patchedContent.setOriginalContent(originalContent);
        return patchedContent;
    }

    @Override
    public ContentDiff generateDiff(Integer postId, String content, String originalContent) {
        ContentPatchLog baseContentRecord =
            contentPatchLogRepository.findByPostIdAndVersion(postId, BASE_VERSION);

        ContentDiff contentDiff = new ContentDiff();
        String contentChanges =
            PatchUtils.diffToJsonPatch(baseContentRecord.getContentDiff(), content);
        contentDiff.setDiff(contentChanges);

        String originalContentChanges =
            PatchUtils.diffToJsonPatch(baseContentRecord.getOriginalContentDiff(), originalContent);
        contentDiff.setOriginalDiff(originalContentChanges);
        return contentDiff;
    }

    @Override
    public void createOrUpdate(ContentPatchLog contentPatchLog) {
        contentPatchLogRepository.save(contentPatchLog);
    }

    @Override
    public ContentPatchLog getDraftByPostId(Integer postId) {
        return contentPatchLogRepository.findFirstByPostIdAndStatusOrderByVersionDesc(postId,
            PostStatus.DRAFT);
    }

    @Override
    public PatchedContent getPatchedContentById(Integer id) {
        ContentPatchLog contentPatchLog = getById(id);
        return applyPatch(contentPatchLog);
    }

    @Override
    public ContentPatchLog getById(Integer id) {
        return contentPatchLogRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(
                "Post content patch log was not found or has been deleted."));
    }
}
