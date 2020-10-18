package run.halo.app.model.vo;

import lombok.Data;
import lombok.ToString;
import run.halo.app.model.dto.EmailDTO;
import run.halo.app.model.dto.LinkDTO;

import java.util.List;

/**
 * Email email vo.
 *
 * @author ryanwang
 * @date 2019/3/22
 */
@Data
@ToString
public class EmailVO {

    private String email;

    private List<EmailDTO> emails;
}
