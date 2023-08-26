package run.halo.app.core.extension.content;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/**
 * @author guqing
 * @see <a href="https://github.com/halo-dev/halo/issues/2322">issue#2322</a>
 * @since 2.0.0
 */
@Data
@ToString(callSuper = true)
@GVK(group = Constant.GROUP, version = Constant.VERSION, kind = Reply.KIND,
    plural = "replies", singular = "reply")
@EqualsAndHashCode(callSuper = true)
public class Reply extends AbstractExtension {

    public static final String KIND = "Reply";

    @Schema(requiredMode = REQUIRED)
    private ReplySpec spec;

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class ReplySpec extends Comment.BaseCommentSpec {

        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String commentName;

        private String quoteReply;
    }
}
