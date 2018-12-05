package cc.ryanc.halo.model.dto;

import lombok.Data;

/**
 * <pre>
 *     七牛上传自定义凭证回调解析
 * </pre>
 *
 * @author : Yawn
 * @date : 2018/12/3
 */
@Data
public class QiNiuPutSet {

    private Long size;
    private Integer w;
    private Integer h;
}
