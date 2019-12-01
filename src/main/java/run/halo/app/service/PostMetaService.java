package run.halo.app.service;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import run.halo.app.model.dto.PostMetaDTO;
import run.halo.app.model.entity.PostMeta;
import run.halo.app.service.base.BaseMetaService;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Post meta service interface.
 *
 * @author ryanwang
 * @author ikaisec
 * @author guqing
 * @date 2019-08-04
 */
public interface PostMetaService extends BaseMetaService<PostMeta> {
    List<PostMeta> createOrUpdateByPostId(Integer id, Set<PostMeta> postMetas);

    List<PostMeta> listPostMetasBy(Integer id);

    List<PostMeta> removeByPostId(Integer postId);

    Map<Integer, List<PostMeta>> listPostMetaListMap(Set<Integer> postIds);

    @NonNull
    PostMetaDTO convertTo(@NonNull PostMeta postMeta);

    @NonNull
    List<PostMetaDTO> convertTo(@Nullable List<PostMeta> postMetaList);
}
