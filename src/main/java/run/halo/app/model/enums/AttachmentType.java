package run.halo.app.model.enums;

/**
 * Attach origin.
 *
 * @author ryanwang
 * @date : 2019-03-12
 */
public enum AttachmentType implements ValueEnum<Integer> {

    /**
     * 服务器
     */
    LOCAL(0),

    /**
     * 又拍云
     */
    UPYUN(1),

    /**
     * 七牛云
     */
    QNYUN(2),

    /**
     * sm.ms
     */
    SMMS(3),

    /**
     * 阿里云
     */
    ALIYUN(4);

    private Integer value;

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
