package run.halo.app.model.properties;

import run.halo.app.model.enums.AttachmentType;

/**
 * Attachment properties.
 *
 * @author johnniang
 * @date 4/1/19
 */
public enum AttachmentProperties implements PropertyEnum {

    ATTACHMENT_TYPE("attachment_type", AttachmentType.class, AttachmentType.LOCAL.name());

    private final String value;

    private final Class<?> type;

    private final String defaultValue;

    AttachmentProperties(String value, Class<?> type, String defaultValue) {
        this.value = value;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public String defaultValue() {
        return defaultValue;
    }

    @Override
    public String getValue() {
        return value;
    }


}
