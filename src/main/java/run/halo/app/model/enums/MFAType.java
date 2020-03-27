package run.halo.app.model.enums;

/**
 * MFA type.
 *
 * @author xun404
 */
public enum MFAType implements ValueEnum<Integer> {

    /**
     * Time-based One-time Password (rfc6238).
     * see: https://tools.ietf.org/html/rfc6238
     */
    TFA_TOTP(1),
    ;

    private final Integer value;

    MFAType(Integer value) {
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return value;
    }
}
