package run.halo.app.model.dto;

import lombok.Data;

/**
 * @author ryanwang
 * @date 2019-05-25
 */
@Data
public class BackupDTO {

    private String downloadLink;

    private String filename;

    private Long updateTime;

    private Long fileSize;
}
