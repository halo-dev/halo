package run.halo.app.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import run.halo.app.model.dto.post.BasePostDetailDTO;
import run.halo.app.model.entity.Post;
import run.halo.app.model.entity.PostMeta;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.model.params.PostQuery;
import run.halo.app.model.vo.ArchiveMonthVO;
import run.halo.app.model.vo.ArchiveYearVO;
import run.halo.app.model.vo.PostDetailVO;
import run.halo.app.model.vo.PostListVO;
import run.halo.app.service.base.BasePostService;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Post service interface.
 *
 * @author johnniang
 * @author ryanwang
 * @author guqing
 * @date 2019-03-14
 */
public interface PostService extends BasePostService<Post> {

    /**
     * Pages posts.
     *
     * @param postQuery post query must not be null
     * @param pageable  page info must not be null
     * @return a page of post
     */
    @NonNull
    Page<Post> pageBy(@NonNull PostQuery postQuery, @NonNull Pageable pageable);

    /**
     * Pages post by keyword
     *
     * @param keyword  keyword
     * @param pageable pageable
     * @return a page of post
     */
    @NonNull
    Page<Post> pageBy(@NonNull String keyword, @NonNull Pageable pageable);

    /**
     * Creates post by post param.
     *
     * @param post        post must not be null
     * @param tagIds      tag id set
     * @param categoryIds category id set
     * @param postMetas   post metas
     * @param autoSave    autoSave
     * @return post created
     */
    @NonNull
    PostDetailVO createBy(@NonNull Post post, Set<Integer> tagIds, Set<Integer> categoryIds, Set<PostMeta> postMetas, boolean autoSave);

    /**
     * Creates post by post param.
     *
     * @param post        post must not be null
     * @param tagIds      tag id set
     * @param categoryIds category id set
     * @param autoSave    autoSave
     * @return post created
     */
    @NonNull
    PostDetailVO createBy(@NonNull Post post, Set<Integer> tagIds, Set<Integer> categoryIds, boolean autoSave);

    /**
     * Updates post by post, tag id set and category id set.
     *
     * @param postToUpdate post to update must not be null
     * @param tagIds       tag id set
     * @param categoryIds  category id set
     * @param autoSave     autoSave
     * @return updated post
     */
    @NonNull
    PostDetailVO updateBy(@NonNull Post postToUpdate, Set<Integer> tagIds, Set<Integer> categoryIds, Set<PostMeta> postMetas, boolean autoSave);

    /**
     * Gets post by post status and url.
     *
     * @param status post status must not be null
     * @param url    post url must not be blank
     * @return post info
     */
    @NonNull
    @Override
    Post getBy(@NonNull PostStatus status, @NonNull String url);

    /**
     * Removes posts in batch.
     *
     * @param ids ids must not be null.
     * @return a list of deleted post.
     */
    @NonNull
    List<Post> removeByIds(@NonNull Collection<Integer> ids);

    /**
     * Lists year archives.
     *
     * @return a list of year archive
     */
    @NonNull
    List<ArchiveYearVO> listYearArchives();

    /**
     * Lists month archives.
     *
     * @return a list of month archive
     */
    @NonNull
    List<ArchiveMonthVO> listMonthArchives();

    /**
     * Import post from markdown document.
     *
     * @param markdown markdown document.
     * @param filename filename
     * @return imported post
     */
    @NonNull
    PostDetailVO importMarkdown(@NonNull String markdown, String filename);

    /**
     * Export post to markdown file by post id.
     *
     * @param id post id
     * @return markdown file content
     */
    @NonNull
    String exportMarkdown(@NonNull Integer id);

    /**
     * Export post to markdown file by post.
     *
     * @param post current post
     * @return markdown file content
     */
    @NonNull
    String exportMarkdown(@NonNull Post post);

    /**
     * Converts to detail vo.
     *
     * @param post post must not be null
     * @return post detail vo
     */
    @NonNull
    PostDetailVO convertToDetailVo(@NonNull Post post);

    /**
     * Converts to a page of post list vo.
     *
     * @param postPage post page must not be null
     * @return a page of post list vo
     */
    @NonNull
    Page<PostListVO> convertToListVo(@NonNull Page<Post> postPage);

    /**
     * Converts to a page of post detail dto.
     *
     * @param postPage post page must not be null
     * @return a page of post detail dto
     */
    Page<BasePostDetailDTO> convertToDetailDto(@NonNull Page<Post> postPage);

    /**
     * Converts to a page of detail vo.
     *
     * @param postPage post page must not be null
     * @return a page of post detail vo
     */
    Page<PostDetailVO> convertToDetailVo(@NonNull Page<Post> postPage);

    /**
     * Publish a post visit event.
     *
     * @param postId postId must not be null
     */
    void publishVisitEvent(@NonNull Integer postId);
}
