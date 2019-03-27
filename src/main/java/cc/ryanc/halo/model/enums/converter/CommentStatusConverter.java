package cc.ryanc.halo.model.enums.converter;

import cc.ryanc.halo.model.enums.CommentStatus;

import javax.persistence.Converter;

/**
 * Comment status converter.
 *
 * @author johnniang
 * @date 3/27/19
 */
@Converter(autoApply = true)
public class CommentStatusConverter extends AbstractConverter<CommentStatus, Integer> {

    public CommentStatusConverter() {
        super(CommentStatus.class);
    }

}
