package run.halo.app.service.base;

import run.halo.app.model.entity.BaseContent;

/**
 * Base content service interface.
 *
 * @author guqing
 * @date 2022-01-07
 */
public interface BaseContentService<CONTENT extends BaseContent>
    extends CrudService<CONTENT, Integer> {

    CONTENT publishContent(Integer postId);

    @SuppressWarnings("unchecked")
    CONTENT createGenericClassInstance();

    CONTENT createOrUpdateDraftBy(Integer postId, String content, String originalContent);
}
