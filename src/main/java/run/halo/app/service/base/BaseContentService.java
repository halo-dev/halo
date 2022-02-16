package run.halo.app.service.base;

import run.halo.app.model.entity.BaseContent;
import run.halo.app.model.entity.ContentPatchLog;

/**
 * Base content service interface.
 *
 * @author guqing
 * @date 2022-01-07
 */
public interface BaseContentService<CONTENT extends BaseContent>
    extends CrudService<CONTENT, Integer> {

    /**
     * <p>Publish post content.</p>
     * <ul>
     * <li>Copy the latest record in {@link ContentPatchLog} to the {@link CONTENT}.
     * <li>Set status to PUBLISHED.
     * <li>Set patchLogId to the latest.
     * </ul>
     *
     * @param postId post id
     * @return published content record.
     */
    CONTENT publishContent(Integer postId);

    /**
     * Create a CONTENT instance.
     *
     * @return a CONTENT instance.
     */
    @SuppressWarnings("unchecked")
    CONTENT createGenericClassInstance();

    /**
     * If the content record does not exist, it will be created; otherwise, it will be updated.
     *
     * @param postId post id
     * @param content post format content
     * @param originalContent post original content
     * @return a created or updated content of post.
     */
    CONTENT createOrUpdateDraftBy(Integer postId, String content, String originalContent);

    /**
     * There is a draft being drafted.
     *
     * @param postId post id
     * @return {@code true} if find a draft record from {@link ContentPatchLog},
     *  otherwise {@code false}
     */
    Boolean draftingInProgress(Integer postId);
}
