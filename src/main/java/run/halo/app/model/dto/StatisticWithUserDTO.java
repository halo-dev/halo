package run.halo.app.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.model.dto.base.OutputConverter;

/**
 * Statistic with user info DTO.
 *
 * @author ryanwang
 * @date 2019-12-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StatisticWithUserDTO extends StatisticDTO implements OutputConverter<StatisticWithUserDTO, StatisticDTO> {

    private UserDTO user;
}
