package cc.ryanc.halo.model.dto;

import cc.ryanc.halo.model.dto.base.OutputConverter;
import cc.ryanc.halo.model.entity.Option;
import lombok.Data;

/**
 * Option output dto.
 *
 * @author johnniang
 * @date 3/20/19
 */
@Data
public class OptionOutputDTO implements OutputConverter<OptionOutputDTO, Option> {

    private String optionKey;

    private String optionValue;

}
