package run.halo.app.security.sudo;

/**
 * A sudo confirmation method available to the current user.
 *
 * @param name method name, such as {@code totp} or {@code email}
 * @param canSendCode whether the server can send a one-time code for this method
 * @param maskedTarget optional masked destination shown in the UI
 * @author johnniang
 * @since 2.27.0
 */
record SudoMethod(String name, boolean canSendCode, String maskedTarget) {}
