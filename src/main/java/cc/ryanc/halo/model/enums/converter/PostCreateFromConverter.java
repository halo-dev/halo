package cc.ryanc.halo.model.enums.converter;

import cc.ryanc.halo.model.enums.PostCreateFrom;

import javax.persistence.Converter;

/**
 * Post create from converter.
 *
 * @author johnniang
 * @date 3/27/19
 */
@Converter(autoApply = true)
public class PostCreateFromConverter extends AbstractConverter<PostCreateFrom, Integer> {

    public PostCreateFromConverter() {
        super(PostCreateFrom.class);
    }
}
