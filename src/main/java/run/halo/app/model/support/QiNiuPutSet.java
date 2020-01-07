package run.halo.app.model.support;

import lombok.Data;

/**
 * <pre>
 *     七牛上传自定义凭证回调解析
 * </pre>
 *
 * @author : Yawn
 * @date 2018/12/3
 */
@Data
public class QiNiuPutSet {

    /**
     * 文件hash值
     */
    public String hash;

    /**
     * 文件名
     */
    public String key;

    /**
     * 图片大小
     */
    private Long size;

    /**
     * 长
     */
    private Integer width;

    /**
     * 宽
     */
    private Integer height;
}
