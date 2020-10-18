package run.halo.app.model.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.model.dto.EmailDTO;
import run.halo.app.model.dto.PostEmailDTO;
import run.halo.app.model.dto.base.OutputConverter;
import run.halo.app.model.dto.post.BasePostMinimalDTO;
import run.halo.app.model.entity.BasePost;
import run.halo.app.model.entity.PostEmail;

import java.io.Serializable;
import java.util.List;

/**
 * Email email vo.
 *
 * @author ryanwang
 * @date 2019/3/22
 */
@Data
@ToString(callSuper = true)
public class PostEmailVO implements Serializable {

    private List<EmailDTO> emails;

}
