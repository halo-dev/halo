package run.halo.app.model.dto;

import java.util.Date;
import lombok.Data;
import run.halo.app.model.dto.base.OutputConverter;
import run.halo.app.model.entity.Attachment;
import run.halo.app.model.enums.AttachmentType;

/**
 * Attachment output dto.
 *
 * @author johnniang
 * @date 3/21/19
 */
@Data
public class AttachmentDTO implements OutputConverter<AttachmentDTO, Attachment> {

    private Integer id;

    private String name;

    private String path;

    private String fileKey;

    private String thumbPath;

    private String mediaType;

    private String suffix;

    private Integer width;

    private Integer height;

    private Long size;

    private AttachmentType type;

    private Date createTime;
}
