package cc.ryanc.halo.model.enums.converter;

import cc.ryanc.halo.model.enums.PostStatus;

import javax.persistence.Converter;

/**
 * PostStatus converter.
 *
 * @author johnniang
 * @date 3/27/19
 */
@Converter(autoApply = true)
public class PostStatusConverter extends AbstractConverter<PostStatus, Integer> {

    public PostStatusConverter() {
        super(PostStatus.class);
    }
}
