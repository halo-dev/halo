package cc.ryanc.halo.utils;

import cc.ryanc.halo.model.domain.Comment;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 组装评论
 */
public class CommentUtil {

    public static List<Comment> getComments(List<Comment> allComments) {
        if (CollectionUtils.isEmpty(allComments)) {
            return Collections.emptyList();
        }
        List<Comment> levelFirstComments = allComments.stream()
                .filter(x -> x.getCommentParent() == 0).collect(Collectors.toList());
        getCommentsRecursion(allComments, levelFirstComments);
        Collections.reverse(levelFirstComments);
        return levelFirstComments;
    }

    public static List<Comment> getChildComments(Long commentId, List<Comment> otherLevelComments) {
        List<Comment> sonComments = otherLevelComments.stream()
                .filter(x -> commentId.equals(x.getCommentParent())).collect(Collectors.toList());
        return getCommentsRecursion(otherLevelComments, sonComments);
    }

    private static List<Comment> getCommentsRecursion(List<Comment> otherLevelComments, List<Comment> sonComments) {
        if (sonComments.size() > 0) {
            List<Comment> notSonComments = (List<Comment>) CollectionUtils.subtract(otherLevelComments, sonComments);
            if (notSonComments.size() > 0) {
                sonComments.stream().forEach(x -> x.setChildComments(getChildComments(x.getCommentId(), notSonComments)));
            }
        }
        return sonComments;
    }

}
