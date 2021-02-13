package run.halo.app.model.enums.converter;

import javax.persistence.Converter;
import run.halo.app.model.enums.PostType;

/**
 * PostType converter.
 *
 * @author johnniang
 * @date 3/27/19
 */
@Converter(autoApply = true)
@Deprecated
public class PostTypeConverter extends AbstractConverter<PostType, Integer> {

}
