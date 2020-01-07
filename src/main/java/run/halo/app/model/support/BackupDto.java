package run.halo.app.model.support;

import lombok.Data;

import java.util.Date;

/**
 * <pre>
 *     备份信息
 * </pre>
 *
 * @author ryanwang
 * @date 2018/6/4
 */
@Data
@Deprecated
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
