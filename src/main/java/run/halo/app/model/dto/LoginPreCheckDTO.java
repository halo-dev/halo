package run.halo.app.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

/**
 * User login env.
 *
 * @author Mu_Wooo
 * @version 1.0
 * @date 2020/3/31 2:39 下午
 * @project halo
 */
@Data
@ToString
@AllArgsConstructor
public class LoginPreCheckDTO {

    private boolean needMFACode;

}
