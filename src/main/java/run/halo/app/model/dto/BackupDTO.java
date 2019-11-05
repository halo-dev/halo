package run.halo.app.model.dto;

import lombok.Data;

import java.util.Date;

/**
 * @author ryanwang
 * @date 2019-05-25
 */
@Data
public class BackupDTO {

    private String fileName;

    private Date createTime;

    private String fileSize;

    private String fileType;

    private String type;
}
