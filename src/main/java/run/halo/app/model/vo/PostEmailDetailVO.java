package run.halo.app.model.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.model.dto.EmailDTO;
import run.halo.app.model.dto.post.BasePostDetailDTO;

import java.util.List;
import java.util.Set;

/**
 * Email email vo.
 *
 * @author ryanwang
 * @date 2019/3/22
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PostEmailDetailVO extends BasePostDetailDTO {

    private Set<Integer> emailIds;

    private List<EmailDTO> emails;
}
