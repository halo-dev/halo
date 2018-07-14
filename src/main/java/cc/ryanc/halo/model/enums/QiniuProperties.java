package cc.ryanc.halo.model.enums;

/**
 * @author : RYAN0UP
 * @date : 2018/7/14
 */
public enum QiniuProperties {

    /**
     * 七牛云域名
     */
    QINIU_DOMAIN("qiniu_domain"),

    /**
     * 七牛云AccessKey
     */
    QINIU_ACCESS_KEY("qiniu_access_key"),

    /**
     * 七牛云SecretKey
     */
    QINIU_SECRET_KEY("qiniu_secret_key"),

    /**
     * 七牛云空间名
     */
    QINIU_BUCKET("qiniu_bucket");

    private String prop;

    QiniuProperties(String prop) {
        this.prop = prop;
    }

    public String getProp() {
        return prop;
    }
}
