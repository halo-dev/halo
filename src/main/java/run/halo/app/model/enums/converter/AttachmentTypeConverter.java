package run.halo.app.model.enums.converter;

import javax.persistence.Converter;
import run.halo.app.model.enums.AttachmentType;

/**
 * Attachment type converter
 *
 * @author johnniang
 * @date 3/27/19
 */
@Converter(autoApply = true)
public class AttachmentTypeConverter extends AbstractConverter<AttachmentType, Integer> {

}
