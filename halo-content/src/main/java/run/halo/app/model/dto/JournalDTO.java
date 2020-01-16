package run.halo.app.model.dto;

import lombok.Data;
import run.halo.app.model.dto.base.OutputConverter;
import run.halo.app.model.entity.Journal;
import run.halo.app.model.enums.JournalType;

import java.util.Date;

/**
 * Journal dto.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-04-24
 */
@Data
public class JournalDTO implements OutputConverter<JournalDTO, Journal> {

    private Integer id;

    private String sourceContent;

    private String content;

    private Long likes;

    private Date createTime;

    private JournalType type;
}
