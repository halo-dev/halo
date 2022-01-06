package run.halo.app.service.base;

import run.halo.app.exception.NotFoundException;
import run.halo.app.model.entity.BaseContent.ContentDiff;
import run.halo.app.model.entity.BaseContent.PatchedContent;
import run.halo.app.model.entity.ContentPatchLog;

/**
 * Base Content patch log service.
 *
 * @author guqing
 * @since 2022-01-04
 */
public interface BaseContentPatchLogService {

    /**
     * Create or update content patch log by post content.
     *
     * @param postId          post id must not be null.
     * @param content         post formatted content must not be null.
     * @param originalContent post original content must not be null.
     * @return created or updated content patch log record.
     */
    ContentPatchLog createOrUpdate(Integer postId, String content, String originalContent);

    /**
     * Apply content patch to v1.
     *
     * @param patchLog content patch log
     * @return real content of the post.
     */
    PatchedContent applyPatch(ContentPatchLog patchLog);

    /**
     * generate content diff based v1.
     *
     * @param postId          post id must not be null.
     * @param content         post formatted content must not be null.
     * @param originalContent post original content must not be null.
     * @return a content diff object.
     */
    ContentDiff generateDiff(Integer postId, String content, String originalContent);

    void createOrUpdate(ContentPatchLog contentPatchLog);

    /**
     * Get the patch log record of the draft status of the content by post id.
     *
     * @param postId post id.
     * @return content patch log record.
     */
    ContentPatchLog getDraftByPostId(Integer postId);

    /**
     * Get content patch log by id.
     *
     * @param id id
     * @return a content patch log
     * @throws NotFoundException if record not found.
     */
    ContentPatchLog getById(Integer id);

    /**
     * Get real post content by id.
     *
     * @param id id
     * @return Actual content of patches applied based on V1 version.
     */
    PatchedContent getPatchedContentById(Integer id);
}
