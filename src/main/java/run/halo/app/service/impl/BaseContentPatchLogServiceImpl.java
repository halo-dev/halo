package run.halo.app.service.impl;

import java.util.Objects;
import org.springframework.data.domain.Example;
import org.springframework.util.Assert;
import run.halo.app.exception.NotFoundException;
import run.halo.app.model.entity.BaseContent.ContentDiff;
import run.halo.app.model.entity.BaseContent.PatchedContent;
import run.halo.app.model.entity.ContentPatchLog;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.repository.ContentPatchLogRepository;
import run.halo.app.repository.PostRepository;
import run.halo.app.service.base.BaseContentPatchLogService;
import run.halo.app.utils.PatchUtils;

/**
 * Content patch log service.
 *
 * @author guqing
 * @since 2022-01-04
 */
public abstract class BaseContentPatchLogServiceImpl implements
    BaseContentPatchLogService {

    /**
     * base version of content patch log.
     */
    public static final int BASE_VERSION = 1;

    private final ContentPatchLogRepository contentPatchLogRepository;
    private final PostRepository postRepository;
    // private final BaseContentRepository<CONTENT> contentRepository;

    public BaseContentPatchLogServiceImpl(ContentPatchLogRepository contentPatchLogRepository,
        PostRepository postRepository) {
        this.contentPatchLogRepository = contentPatchLogRepository;
        this.postRepository = postRepository;
        // this.contentRepository = contentRepository;
    }

    @Override
    public ContentPatchLog createOrUpdate(Integer postId, String content, String originalContent) {
        // 不存在草稿，不存在v1，存在v1
        Integer version = getVersionByPostId(postId);
        if (existDraftBy(postId)) {
            // 存在草稿，直接使用版本号
            return updateDraftBy(postId, version, content, originalContent);
        }
        return createDraftContent(postId, version, content, originalContent);
    }

    private Integer getVersionByPostId(Integer postId) {
        Integer version;
        ContentPatchLog latestPatchLog =
            contentPatchLogRepository.findFirstByPostIdOrderByVersionDesc(postId);
        if (latestPatchLog == null) {
            version = 1;
        } else if (PostStatus.PUBLISHED.equals(latestPatchLog.getStatus())) {
            // 不存在草稿，需要创建则version+1
            version = latestPatchLog.getVersion() + 1;
        } else {
            version = latestPatchLog.getVersion();
        }
        return version;
    }

    @Override
    public void save(ContentPatchLog contentPatchLog) {
        contentPatchLogRepository.save(contentPatchLog);
    }

    private ContentPatchLog createDraftContent(Integer postId, Integer version,
        String formatContent, String originalContent) {
        ContentPatchLog contentPatchLog =
            buildPatchLog(postId, version, formatContent, originalContent);
        contentPatchLog.setSourceId(0);
        contentPatchLogRepository.save(contentPatchLog);
        return contentPatchLog;
    }

    private ContentPatchLog buildPatchLog(Integer postId, Integer version, String formatContent,
        String originalContent) {
        ContentPatchLog contentPatchLog = new ContentPatchLog();
        if (Objects.equals(version, BASE_VERSION)) {
            contentPatchLog.setContentDiff(formatContent);
            contentPatchLog.setOriginalContentDiff(originalContent);
        } else {
            ContentDiff contentDiff = generateDiff(postId, formatContent, originalContent);
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

    private ContentPatchLog updateDraftBy(Integer postId, Integer version, String formatContent,
        String originalContent) {
        ContentPatchLog draftPatchLog =
            contentPatchLogRepository.findFirstByPostIdAndStatusOrderByVersionDesc(postId,
                PostStatus.DRAFT);
        // Is the draft version 1
        if (Objects.equals(draftPatchLog.getVersion(), BASE_VERSION)) {
            // If it is V1, modify the content directly.
            draftPatchLog.setContentDiff(formatContent);
            draftPatchLog.setOriginalContentDiff(originalContent);
            contentPatchLogRepository.save(draftPatchLog);
            return draftPatchLog;
        }
        // Generate content diff.
        ContentDiff contentDiff = generateDiff(postId, formatContent, originalContent);
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
    public ContentDiff generateDiff(Integer postId, String formatContent, String originalContent) {
        ContentPatchLog basePatchLog =
            contentPatchLogRepository.findByPostIdAndVersion(postId, BASE_VERSION);

        ContentDiff contentDiff = new ContentDiff();
        String contentChanges =
            PatchUtils.diffToJsonPatch(basePatchLog.getContentDiff(), formatContent);
        contentDiff.setDiff(contentChanges);

        String originalContentChanges =
            PatchUtils.diffToJsonPatch(basePatchLog.getOriginalContentDiff(), originalContent);
        contentDiff.setOriginalDiff(originalContentChanges);
        return contentDiff;
    }

    @Override
    public ContentPatchLog getDraftByPostId(Integer postId) {
        return contentPatchLogRepository.findFirstByPostIdAndStatusOrderByVersionDesc(postId,
            PostStatus.DRAFT);
    }

    @Override
    public PatchedContent getByPostId(Integer postId) {
        ContentPatchLog contentPatchLog =
            contentPatchLogRepository.findFirstByPostIdOrderByVersionDesc(postId);
        if (contentPatchLog == null) {
            throw new NotFoundException(
                "Post content patch log was not found or has been deleted.");
        }
        return applyPatch(contentPatchLog);
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
