package run.halo.app.model.enums.converter;

import javax.persistence.Converter;
import run.halo.app.model.enums.PostStatus;

/**
 * PostStatus converter.
 *
 * @author johnniang
 * @date 3/27/19
 */
@Converter(autoApply = true)
public class PostStatusConverter extends AbstractConverter<PostStatus, Integer> {

}
