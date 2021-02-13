package run.halo.app.model.enums.converter;

import javax.persistence.Converter;
import run.halo.app.model.enums.DataType;

/**
 * Data type converter.
 *
 * @author johnniang
 * @date 4/10/19
 */
@Converter(autoApply = true)
public class DataTypeConverter extends AbstractConverter<DataType, Integer> {

}
