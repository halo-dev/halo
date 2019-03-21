package cc.ryanc.halo.model.dto;

import cc.ryanc.halo.model.dto.base.OutputConverter;
import cc.ryanc.halo.model.entity.Attachment;
import lombok.Data;

/**
 * Attachment output dto.
 *
 * @author johnniang
 * @date 3/21/19
 */
@Data
public class AttachmentOutputDTO implements OutputConverter<AttachmentOutputDTO, Attachment> {

    /**
     * Attachment id.
     */
    private Integer id;

    /**
     * 附件名称
     */
    private String name;

    /**
     * 附件路径
     */
    private String path;

    /**
     * 缩略图路径
     */
    private String thumbPath;

    /**
     * 附件类型
     */
    private String mediaType;

    /**
     * 附件后缀
     */
    private String suffix;

    /**
     * 附件尺寸
     */
    private String dimension;

    /**
     * 附件大小
     */
    private String size;

    /**
     * 附件上传类型
     */
    private Integer type;
}
