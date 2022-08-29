package run.halo.app.theme.finders.vo;

import java.util.Map;
import lombok.experimental.SuperBuilder;
import run.halo.app.core.extension.Comment;

@SuperBuilder
public class BaseCommentVo {
    String name;

    String raw;

    String content;

    Comment.CommentOwner owner;

    String userAgent;

    Integer priority;

    Boolean top;

    Boolean allowNotification;

    Map<String, String> annotations;
}
