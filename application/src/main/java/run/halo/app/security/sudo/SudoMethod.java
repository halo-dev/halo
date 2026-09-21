package run.halo.app.security.sudo;

/**
 * A sudo confirmation method available to the current user.
 *
 * @param name method name, such as {@code totp} or {@code email}
 * @param sendable whether a one-time code must be sent first
 * @param maskedTarget optional masked destination shown in the UI
 * @author johnniang
 * @since 2.27.0
 */
record SudoMethod(String name, boolean sendable, String maskedTarget) {}
