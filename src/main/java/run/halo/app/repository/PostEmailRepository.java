package run.halo.app.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import run.halo.app.model.entity.PostEmail;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.model.projection.EmailPostPostCountProjection;
import run.halo.app.repository.base.BaseRepository;

import java.util.Collection;
import java.util.List;
import java.util.Set;


/**
 * Post email repository.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-03-19
 */
public interface PostEmailRepository extends BaseRepository<PostEmail, Integer> {

    /**
     * Finds all post emails by post id.
     *
     * @param postId post id must not be null
     * @return a list of post emails
     */
    @NonNull
    List<PostEmail> findAllByPostId(@NonNull Integer postId);

    /**
     * Finds all email ids by post id.
     *
     * @param postId post id must not be null
     * @return a set of email id
     */
    @Query("select postEmail.emailId from PostEmail postEmail where postEmail.postId = ?1")
    @NonNull
    Set<Integer> findAllEmailIdsByPostId(@NonNull Integer postId);

    /**
     * Finds all post emails by email id.
     *
     * @param emailId email id must not be null
     * @return a list of post emails
     */
    @NonNull
    List<PostEmail> findAllByEmailId(@NonNull Integer emailId);

    /**
     * Finds all post id by email id.
     *
     * @param emailId email id must not be null
     * @return a set of post id
     */
    @Query("select postEmail.postId from PostEmail postEmail where postEmail.emailId = ?1")
    @NonNull
    Set<Integer> findAllPostIdsByEmailId(@NonNull Integer emailId);

    /**
     * Finds all post id by email id and post status.
     *
     * @param emailId  email id must not be null
     * @param status post status
     * @return a set of post id
     */
    @Query("select postEmail.postId from PostEmail postEmail,Post post where postEmail.emailId = ?1 and post.id = postEmail.postId and post.status = ?2")
    @NonNull
    Set<Integer> findAllPostIdsByEmailId(@NonNull Integer emailId, @NonNull PostStatus status);

    /**
     * Finds all emails by post id in.
     *
     * @param postIds post id collection
     * @return a list of post emails
     */
    @NonNull
    List<PostEmail> findAllByPostIdIn(@NonNull Collection<Integer> postIds);

    /**
     * Deletes post emails by post id.
     *
     * @param postId post id must not be null
     * @return a list of post email deleted
     */
    @NonNull
    List<PostEmail> deleteByPostId(@NonNull Integer postId);

    /**
     * Deletes post emails by email id.
     *
     * @param emailId email id must not be null
     * @return a list of post email deleted
     */
    @NonNull
    List<PostEmail> deleteByEmailId(@NonNull Integer emailId);

    /**
     * Finds post count by email id collection.
     *
     * @param emailIds email id collection must not be null
     * @return a list of email post count projection
     */
    @Query("select new run.halo.app.model.projection.EmailPostPostCountProjection(count(pt.postId), pt.emailId) from PostEmail pt where pt.emailId in ?1 group by pt.emailId")
    @NonNull
    List<EmailPostPostCountProjection> findPostCountByEmailIds(@NonNull Collection<Integer> emailIds);

    /**
     * Finds post count of email.
     *
     * @return a list of email post count projection
     */
    @Query("select new run.halo.app.model.projection.EmailPostPostCountProjection(count(pt.postId), pt.emailId) from PostEmail pt group by pt.emailId")
    @NonNull
    List<EmailPostPostCountProjection> findPostCount();
}
