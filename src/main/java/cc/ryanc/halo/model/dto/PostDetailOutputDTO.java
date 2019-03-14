package cc.ryanc.halo.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Post detail output dto.
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
public class PostDetailOutputDTO extends PostSimpleOutputDTO {

    /**
     * 源内容
     */
    private String originalContent;

    /**
     * 渲染后内容
     */
    private String formatContent;
}
