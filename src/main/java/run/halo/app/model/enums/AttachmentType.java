package run.halo.app.model.enums;

/**
 * Attach origin.
 *
 * @author ryanwang
 * @date 2019-03-12
 */
public enum AttachmentType implements ValueEnum<Integer> {

    /**
     * 服务器
     */
    LOCAL(0),

    /**
     * 又拍云
     */
    UPOSS(1),

    /**
     * 七牛云
     */
    QINIUOSS(2),

    /**
     * sm.ms
     */
    SMMS(3),

    /**
     * 阿里云
     */
    ALIOSS(4),

    /**
     * 百度云
     */
    BAIDUBOS(5),

    /**
     * 腾讯云
     */
    TENCENTCOS(6),

    /**
     * 华为云
     */
    HUAWEIOBS(7),

    /**
     * MINIO
     */
    MINIO(8);

    private final Integer value;

    AttachmentType(Integer value) {
        this.value = value;
    }

    /**
     * Get enum value.
     *
     * @return enum value
     */
    @Override
    public Integer getValue() {
        return value;
    }
}
