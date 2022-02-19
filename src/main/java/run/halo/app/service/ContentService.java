package run.halo.app.service;

import run.halo.app.model.entity.Content;
import run.halo.app.model.entity.ContentPatchLog;
import run.halo.app.service.base.CrudService;

/**
 * Base content service interface.
 *
 * @author guqing
 * @date 2022-01-07
 */
public interface ContentService extends CrudService<Content, Integer> {

    /**
     * <p>Publish post content.</p>
     * <ul>
     * <li>Copy the latest record in {@link ContentPatchLog} to the {@link Content}.
     * <li>Set status to PUBLISHED.
     * <li>Set patchLogId to the latest.
     * </ul>
     *
     * @param postId post id
     * @return published content record.
     */
    Content publishContent(Integer postId);

    /**
     * If the content record does not exist, it will be created; otherwise, it will be updated.
     *
     * @param postId post id
     * @param content post format content
     * @param originalContent post original content
     */
    void createOrUpdateDraftBy(Integer postId, String content, String originalContent);

    /**
     * There is a draft being drafted.
     *
     * @param postId post id
     * @return {@code true} if find a draft record from {@link ContentPatchLog},
     *  otherwise {@code false}
     */
    Boolean draftingInProgress(Integer postId);
}
