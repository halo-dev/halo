package run.halo.app.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

/**
 * MultiFactorAuth VO.
 *
 * @author Mu_Wooo
 * @date 2020-03-30
 */
@Data
@AllArgsConstructor
@ToString
public class MultiFactorAuthVO {

    private String qrImages;

    private String optAuthUrl;

    private String mfaKey;

}
