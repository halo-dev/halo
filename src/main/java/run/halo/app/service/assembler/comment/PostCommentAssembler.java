package run.halo.app.service.assembler.comment;

import static run.halo.app.model.support.HaloConst.URL_SEPARATOR;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import run.halo.app.model.dto.post.BasePostMinimalDTO;
import run.halo.app.model.entity.Post;
import run.halo.app.model.entity.PostComment;
import run.halo.app.model.enums.PostPermalinkType;
import run.halo.app.model.vo.PostCommentWithPostVO;
import run.halo.app.repository.PostRepository;
import run.halo.app.service.OptionService;
import run.halo.app.utils.DateUtils;
import run.halo.app.utils.ServiceUtils;

/**
 * Post comment assembler.
 *
 * @author guqing
 * @date 2022-03-08
 */
@Component
public class PostCommentAssembler extends BaseCommentAssembler<PostComment> {

    private final OptionService optionService;

    private final PostRepository postRepository;

    public PostCommentAssembler(OptionService optionService,
        PostRepository postRepository) {
        super(optionService);
        this.postRepository = postRepository;
        this.optionService = optionService;
    }


    /**
     * Converts to with post vo.
     *
     * @param commentPage comment page must not be null
     * @return a page of comment with post vo
     */
    @NonNull
    public Page<PostCommentWithPostVO> convertToWithPostVo(@NonNull Page<PostComment> commentPage) {
        Assert.notNull(commentPage, "PostComment page must not be null");

        return new PageImpl<>(convertToWithPostVo(commentPage.getContent()),
            commentPage.getPageable(), commentPage.getTotalElements());
    }

    /**
     * Converts to with post vo
     *
     * @param comment comment
     * @return a comment with post vo
     */
    @NonNull
    public PostCommentWithPostVO convertToWithPostVo(@NonNull PostComment comment) {
        Assert.notNull(comment, "PostComment must not be null");
        PostCommentWithPostVO postCommentWithPostVo =
            new PostCommentWithPostVO().convertFrom(comment);

        BasePostMinimalDTO basePostMinimalDto =
            new BasePostMinimalDTO().convertFrom(postRepository.getById(comment.getPostId()));

        postCommentWithPostVo.setPost(buildPostFullPath(basePostMinimalDto));

        postCommentWithPostVo.setAvatar(buildAvatarUrl(comment.getGravatarMd5()));

        return postCommentWithPostVo;
    }

    /**
     * Converts to with post vo
     *
     * @param postComments comment list
     * @return a list of comment with post vo
     */
    @NonNull
    public List<PostCommentWithPostVO> convertToWithPostVo(List<PostComment> postComments) {
        if (CollectionUtils.isEmpty(postComments)) {
            return Collections.emptyList();
        }

        // Fetch goods ids
        Set<Integer> postIds = ServiceUtils.fetchProperty(postComments, PostComment::getPostId);

        // Get all posts
        Map<Integer, Post> postMap =
            ServiceUtils.convertToMap(postRepository.findAllById(postIds), Post::getId);

        return postComments.stream()
            .filter(comment -> postMap.containsKey(comment.getPostId()))
            .map(comment -> {
                // Convert to vo
                PostCommentWithPostVO postCommentWithPostVo =
                    new PostCommentWithPostVO().convertFrom(comment);

                BasePostMinimalDTO basePostMinimalDto =
                    new BasePostMinimalDTO().convertFrom(postMap.get(comment.getPostId()));

                postCommentWithPostVo.setPost(buildPostFullPath(basePostMinimalDto));

                postCommentWithPostVo.setAvatar(buildAvatarUrl(comment.getGravatarMd5()));

                return postCommentWithPostVo;
            }).collect(Collectors.toList());
    }

    private BasePostMinimalDTO buildPostFullPath(BasePostMinimalDTO post) {
        PostPermalinkType permalinkType = optionService.getPostPermalinkType();

        String pathSuffix = optionService.getPathSuffix();

        String archivesPrefix = optionService.getArchivesPrefix();

        int month = DateUtils.month(post.getCreateTime()) + 1;

        String monthString = month < 10 ? "0" + month : String.valueOf(month);

        int day = DateUtils.dayOfMonth(post.getCreateTime());

        String dayString = day < 10 ? "0" + day : String.valueOf(day);

        StringBuilder fullPath = new StringBuilder();

        if (optionService.isEnabledAbsolutePath()) {
            fullPath.append(optionService.getBlogBaseUrl());
        }

        fullPath.append(URL_SEPARATOR);

        if (permalinkType.equals(PostPermalinkType.DEFAULT)) {
            fullPath.append(archivesPrefix)
                .append(URL_SEPARATOR)
                .append(post.getSlug())
                .append(pathSuffix);
        } else if (permalinkType.equals(PostPermalinkType.ID)) {
            fullPath.append("?p=")
                .append(post.getId());
        } else if (permalinkType.equals(PostPermalinkType.DATE)) {
            fullPath.append(DateUtils.year(post.getCreateTime()))
                .append(URL_SEPARATOR)
                .append(monthString)
                .append(URL_SEPARATOR)
                .append(post.getSlug())
                .append(pathSuffix);
        } else if (permalinkType.equals(PostPermalinkType.DAY)) {
            fullPath.append(DateUtils.year(post.getCreateTime()))
                .append(URL_SEPARATOR)
                .append(monthString)
                .append(URL_SEPARATOR)
                .append(dayString)
                .append(URL_SEPARATOR)
                .append(post.getSlug())
                .append(pathSuffix);
        } else if (permalinkType.equals(PostPermalinkType.YEAR)) {
            fullPath.append(DateUtils.year(post.getCreateTime()))
                .append(URL_SEPARATOR)
                .append(post.getSlug())
                .append(pathSuffix);
        } else if (permalinkType.equals(PostPermalinkType.ID_SLUG)) {
            fullPath.append(archivesPrefix)
                .append(URL_SEPARATOR)
                .append(post.getId())
                .append(pathSuffix);
        }

        post.setFullPath(fullPath.toString());

        return post;
    }
}
