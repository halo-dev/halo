package cc.ryanc.halo.model.support;

import lombok.*;

/**
 * Global response entity.
 *
 * @author johnniang
 */
@Data
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse {

    private Integer status;

    private String message;

    private String devMessage;

    private Object data;

}
