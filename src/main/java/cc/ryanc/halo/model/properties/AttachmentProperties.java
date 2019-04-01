package cc.ryanc.halo.model.properties;

import cc.ryanc.halo.model.enums.AttachmentType;

/**
 * Attachment properties.
 *
 * @author johnniang
 * @date 4/1/19
 */
public enum AttachmentProperties implements PropertyEnum {
    
    ATTACHMENT_TYPE("attachment_type", AttachmentType.class);

    private final String value;

    private final Class<?> type;

    AttachmentProperties(String value, Class<?> type) {
        this.value = value;
        this.type = type;
    }


    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public String getValue() {
        return value;
    }}
