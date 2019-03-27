package cc.ryanc.halo.model.enums.converter;

import cc.ryanc.halo.model.enums.LogType;

import javax.persistence.Converter;

/**
 * Log type converter.
 *
 * @author johnniang
 * @date 3/27/19
 */
@Converter(autoApply = true)
public class LogTypeConverter extends AbstractConverter<LogType, Integer> {

    public LogTypeConverter() {
        super(LogType.class);
    }
}
