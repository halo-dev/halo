package run.halo.app.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import run.halo.app.model.entity.PostContent;
import run.halo.app.repository.base.BaseContentRepository;

/**
 * Post content repository.
 *
 * @author guqing
 * @date 2022-01-07
 */
public interface PostContentRepository extends BaseContentRepository<PostContent>,
    JpaSpecificationExecutor<PostContent> {

}
