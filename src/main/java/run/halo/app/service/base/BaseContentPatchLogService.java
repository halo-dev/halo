package run.halo.app.service.base;

import java.util.List;
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

    /**
     * Creates or updates the {@link ContentPatchLog}.
     *
     * @param contentPatchLog param to create or update
     */
    void save(ContentPatchLog contentPatchLog);

    /**
     * Gets the patch log record of the draft status of the content by post id.
     *
     * @param postId post id.
     * @return content patch log record.
     */
    ContentPatchLog getDraftByPostId(Integer postId);

    /**
     * Gets content patch log by id.
     *
     * @param id id
     * @return a content patch log
     * @throws NotFoundException if record not found.
     */
    ContentPatchLog getById(Integer id);

    /**
     * Gets content patch log by post id.
     *
     * @param postId a post id
     * @return a real content of post.
     */
    PatchedContent getByPostId(Integer postId);

    /**
     * Gets real post content by id.
     *
     * @param id id
     * @return Actual content of patches applied based on V1 version.
     */
    PatchedContent getPatchedContentById(Integer id);

    /**
     * Permanently delete post contentPatchLog by post id.
     *
     * @param postId post id
     * @return deleted post content patch logs.
     */
    List<ContentPatchLog> removeByPostId(Integer postId);
}
