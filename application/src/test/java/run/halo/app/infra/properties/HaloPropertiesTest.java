package run.halo.app.infra.properties;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.validation.SimpleErrors;

class HaloPropertiesTest {

    static Stream<Arguments> validateTest() throws MalformedURLException {
        return Stream.of(
            Arguments.of(true, new URL("http://localhost:8080"), true),
            Arguments.of(false, new URL("http://localhost:8080"), true),
            Arguments.of(true, new URL("https://localhost:8080"), true),
            Arguments.of(false, new URL("https://localhost:8080"), true),
            Arguments.of(true, new URL("ftp://localhost:8080"), false),
            Arguments.of(false, new URL("ftp://localhost:8080"), false),
            Arguments.of(true, new URL("http:www/halo/run"), false),
            Arguments.of(false, new URL("http:www/halo.run"), false),
            Arguments.of(true, new URL("https:www/halo/run"), false),
            Arguments.of(false, new URL("https:www/halo/run"), false),
            Arguments.of(true, new URL("https:///path"), false),
            Arguments.of(false, new URL("https:///path"), false),
            Arguments.of(true, new URL("http:///path"), false),
            Arguments.of(false, new URL("http:///path"), false),
            Arguments.of(true, null, false),
            Arguments.of(false, null, true)
        );
    }

    @ParameterizedTest
    @MethodSource
    void validateTest(boolean useAbsolutePermalink, URL externalUrl, boolean valid) {
        var properties = new HaloProperties();
        properties.setUseAbsolutePermalink(useAbsolutePermalink);
        properties.setExternalUrl(externalUrl);
        var errors = new SimpleErrors(properties);
        properties.validate(properties, errors);
        Assertions.assertEquals(valid, !errors.hasErrors());
    }
}