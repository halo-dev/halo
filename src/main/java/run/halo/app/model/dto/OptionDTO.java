package run.halo.app.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import run.halo.app.model.dto.base.OutputConverter;
import run.halo.app.model.entity.Option;

/**
 * Option output dto.
 *
 * @author johnniang
 * @date 3/20/19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OptionDTO implements OutputConverter<OptionDTO, Option> {

    private String key;

    private Object value;

}
