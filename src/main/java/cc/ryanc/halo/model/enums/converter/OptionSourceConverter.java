package cc.ryanc.halo.model.enums.converter;

import cc.ryanc.halo.model.enums.OptionSource;

import javax.persistence.Converter;

/**
 * OptionSource converter.
 *
 * @author johnniang
 * @date 4/1/19
 */
@Converter(autoApply = true)
public class OptionSourceConverter extends AbstractConverter<OptionSource, Integer> {

    public OptionSourceConverter() {
        super(OptionSource.class);
    }
}
