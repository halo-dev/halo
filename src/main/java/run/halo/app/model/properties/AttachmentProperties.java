package run.halo.app.model.properties;

import run.halo.app.model.enums.AttachmentType;

/**
 * Attachment properties.
 *
 * @author johnniang
 * @date 4/1/19
 */
public enum AttachmentProperties implements PropertyEnum {

    /**
     * Upload image preview enable
     */
    UPLOAD_IMAGE_PREVIEW_ENABLE("attachment_upload_image_preview_enable", Boolean.class, "true"),

    /**
     * Upload max parallel uploads
     */
    UPLOAD_MAX_PARALLEL_UPLOADS("attachment_upload_max_parallel_uploads", Integer.class, "3"),

    /**
     * Upload max files
     */
    UPLOAD_MAX_FILES("attachment_upload_max_files", Integer.class, "50"),

    /**
     * attachment_type
     */
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
