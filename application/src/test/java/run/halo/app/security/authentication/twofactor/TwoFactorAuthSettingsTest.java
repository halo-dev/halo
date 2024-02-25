package run.halo.app.security.authentication.twofactor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TwoFactorAuthSettingsTest {

    @ParameterizedTest
    @MethodSource("isAvailableCases")
    void isAvailableTest(TwoFactorAuthSettings settings, boolean expectAvailable) {
        assertEquals(expectAvailable, settings.isAvailable());
    }

    static Stream<Arguments> isAvailableCases() {
        return Stream.of(
            arguments(settings(false, true, true), false),
            arguments(settings(false, false, false), false),
            arguments(settings(false, false, true), false),
            arguments(settings(false, true, false), false),
            arguments(settings(true, true, true), true),
            arguments(settings(true, false, false), false),
            arguments(settings(true, false, true), true),
            arguments(settings(true, true, false), false)
        );
    }

    static TwoFactorAuthSettings settings(boolean enabled, boolean emailVerified,
        boolean totpConfigured) {
        var settings = new TwoFactorAuthSettings();
        settings.setEnabled(enabled);
        settings.setEmailVerified(emailVerified);
        settings.setTotpConfigured(totpConfigured);
        return settings;
    }

}