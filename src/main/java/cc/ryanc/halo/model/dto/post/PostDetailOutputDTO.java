package cc.ryanc.halo.model.dto.post;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Post detail output dto.
 *
 * @author johnniang
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
public class PostDetailOutputDTO extends PostSimpleOutputDTO {

    private String originalContent;

    private String formatContent;
}
