package cc.ryanc.halo.model.dto;

import lombok.Data;

import java.util.Date;

/**
 * <pre>
 *     备份信息
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2018/6/4
 */
@Data
public class BackupDto {

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 创建时间
     */
    private Date createAt;

    /**
     * 文件大小
     */
    private String fileSize;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 备份类型
     */
    private String backupType;
}
