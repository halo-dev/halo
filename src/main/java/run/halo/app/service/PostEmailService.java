package run.halo.app.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;
import run.halo.app.model.dto.EmailDTO;
import run.halo.app.model.dto.EmailWithPostCountDTO;
import run.halo.app.model.entity.Email;
import run.halo.app.model.entity.Post;
import run.halo.app.model.entity.PostEmail;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.service.base.CrudService;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Post email service interface.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-03-19
 */
public interface PostEmailService extends CrudService<PostEmail, Integer> {

    /**
     * Lists emails by post id.
     *
     * @param postId post id must not be null
     * @return a list of email
     */
    @NonNull
    List<Email> listEmailsBy(@NonNull Integer postId);

    /**
     * List email with post count output dtos.
     *
     * @param sort sort info
     * @return a list of email with post count output dto
     */
    @NonNull
    List<EmailDTO> listEmailWithCountDTOs(@NonNull Sort sort);

    /**
     * Lists emails list map by post id.
     *
     * @param postIds post id collection
     * @return email map (key: postId, value: a list of emails)
     */
    @NonNull
    Map<Integer, List<Email>> listEmailListMapBy(@Nullable Collection<Integer> postIds);

    /**
     * Lists posts by email id.
     *
     * @param emailId email id must not be null
     * @return a list of post
     */
    @NonNull
    List<Post> listPostsBy(@NonNull Integer emailId);

    /**
     * Lists posts by email id and post status.
     *
     * @param emailId  email id must not be null
     * @param status post status
     * @return a list of post
     */
    @NonNull
    List<Post> listPostsBy(@NonNull Integer emailId, @NonNull PostStatus status);

    /**
     * Lists posts by email slug and post status.
     *
     * @param slug   email slug must not be null
     * @param status post status
     * @return a list of post
     */
    @NonNull
    List<Post> listPostsBy(@NonNull String slug, @NonNull PostStatus status);

    /**
     * Pages posts by email id.
     *
     * @param emailId    must not be null
     * @param pageable must not be null
     * @return a page of post
     */
    Page<Post> pagePostsBy(@NonNull Integer emailId, Pageable pageable);

    /**
     * Pages posts by email id and post status.
     *
     * @param emailId    must not be null
     * @param status   post status
     * @param pageable must not be null
     * @return a page of post
     */
    Page<Post> pagePostsBy(@NonNull Integer emailId, @NonNull PostStatus status, Pageable pageable);

    /**
     * Merges or creates post emails by post id and email id set if absent.
     *
     * @param postId post id must not be null
     * @param emails email id list
     * @return a list of post email
     */
    @NonNull
    List<PostEmail> mergeOrCreateByIfAbsent(@NonNull Integer postId, @Nullable List<Email> emails);

    /**
     * Lists post emails by post id.
     *
     * @param postId post id must not be null
     * @return a list of post email
     */
    @NonNull
    List<PostEmail> listByPostId(@NonNull Integer postId);

    /**
     * Lists post emails by email id.
     *
     * @param emailId email id must not be null
     * @return a list of post email
     */
    @NonNull
    List<PostEmail> listByEmailId(@NonNull Integer emailId);

    /**
     * Lists email id set by post id.
     *
     * @param postId post id must not be null
     * @return a set of email id
     */
    @NonNull
    Set<Integer> listEmailIdsByPostId(@NonNull Integer postId);

    /**
     * Removes post emails by post id.
     *
     * @param postId post id must not be null
     * @return a list of post email
     */
    @NonNull
    @Transactional
    List<PostEmail> removeByPostId(@NonNull Integer postId);

    /**
     * Removes post emails by email id.
     *
     * @param emailId email id must not be null
     * @return a list of post email
     */
    @NonNull
    @Transactional
    List<PostEmail> removeByEmailId(@NonNull Integer emailId);
}
