package run.halo.app.infra;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.webflux.autoconfigure.WebFluxProperties;
import org.springframework.http.HttpRequest;
import reactor.core.publisher.Mono;
import run.halo.app.infra.properties.HaloProperties;

@ExtendWith(MockitoExtension.class)
class SystemConfigFirstExternalUrlSupplierTest {

    @Mock
    HaloProperties haloProperties;

    @Mock
    WebFluxProperties webFluxProperties;

    @Mock
    SystemConfigFetcher systemConfigFetcher;

    @InjectMocks
    SystemConfigFirstExternalUrlSupplier externalUrl;

    @Nested
    class HaloPropertiesSupplier {

        @BeforeEach
        void setUp() {
            when(systemConfigFetcher.getBasic()).thenReturn(Mono.empty());
            externalUrl.onExtensionInitialized(null);
        }

        @Test
        void getURIWhenUsingAbsolutePermalink() throws MalformedURLException {
            var fakeUri = URI.create("https://halo.run/fake");
            when(haloProperties.getExternalUrl()).thenReturn(fakeUri.toURL());
            when(haloProperties.isUseAbsolutePermalink()).thenReturn(true);

            assertEquals(fakeUri, externalUrl.get());
        }

        @Test
        void getURIWhenBasePathSetAndNotUsingAbsolutePermalink() {
            when(webFluxProperties.getBasePath()).thenReturn("/blog");
            when(haloProperties.isUseAbsolutePermalink()).thenReturn(false);

            assertEquals(URI.create("/blog"), externalUrl.get());
        }

        @Test
        void getURIWhenBasePathSetAndUsingAbsolutePermalink() throws MalformedURLException {
            var fakeUri = URI.create("https://halo.run/fake");
            when(haloProperties.getExternalUrl()).thenReturn(fakeUri.toURL());
            lenient().when(webFluxProperties.getBasePath()).thenReturn("/blog");
            when(haloProperties.isUseAbsolutePermalink()).thenReturn(true);

            assertEquals(URI.create("https://halo.run/fake"), externalUrl.get());
        }

        @Test
        void getURIWhenUsingRelativePermalink() throws MalformedURLException {
            when(haloProperties.isUseAbsolutePermalink()).thenReturn(false);

            assertEquals(URI.create("/"), externalUrl.get());
        }

        @Test
        void getURLWhenExternalURLProvided() throws MalformedURLException {
            var fakeUri = URI.create("https://halo.run/fake");
            when(haloProperties.getExternalUrl()).thenReturn(fakeUri.toURL());
            var mockRequest = mock(HttpRequest.class);
            var url = externalUrl.getURL(mockRequest);
            assertEquals(fakeUri.toURL(), url);
        }

        @Test
        void getURLWhenExternalURLAbsent() throws MalformedURLException {
            var fakeUri = URI.create("https://localhost/fake");
            when(haloProperties.getExternalUrl()).thenReturn(null);
            var mockRequest = mock(HttpRequest.class);
            when(mockRequest.getURI()).thenReturn(fakeUri);
            var url = externalUrl.getURL(mockRequest);
            assertEquals(new URL("https://localhost/"), url);
        }

        @Test
        void getURLWhenBasePathSetAndExternalURLProvided() throws MalformedURLException {
            var fakeUri = URI.create("https://localhost/fake");
            when(haloProperties.getExternalUrl()).thenReturn(fakeUri.toURL());
            lenient().when(webFluxProperties.getBasePath()).thenReturn("/blog");
            var mockRequest = mock(HttpRequest.class);
            lenient().when(mockRequest.getURI()).thenReturn(fakeUri);
            var url = externalUrl.getURL(mockRequest);
            assertEquals(new URL("https://localhost/fake"), url);
        }

        @Test
        void getURLWhenBasePathSetAndExternalURLAbsent() throws MalformedURLException {
            var fakeUri = URI.create("https://localhost/fake");
            when(haloProperties.getExternalUrl()).thenReturn(null);
            when(webFluxProperties.getBasePath()).thenReturn("/blog");
            var mockRequest = mock(HttpRequest.class);
            when(mockRequest.getURI()).thenReturn(fakeUri);
            var url = externalUrl.getURL(mockRequest);
            assertEquals(new URL("https://localhost/blog"), url);
        }

        @Test
        void getRaw() throws MalformedURLException {
            var fakeUri = URI.create("http://localhost/fake");
            when(haloProperties.getExternalUrl()).thenReturn(fakeUri.toURL());
            assertEquals(fakeUri.toURL(), externalUrl.getRaw());

            when(haloProperties.getExternalUrl()).thenReturn(null);
            assertNull(externalUrl.getRaw());
        }

        @Test
        void shouldConvertHostToAsciiWhenExternalUrlContainsIDN() throws MalformedURLException {
            var fakeUri = URI.create("https://abcd.中国");
            when(haloProperties.getExternalUrl()).thenReturn(fakeUri.toURL());
            when(haloProperties.isUseAbsolutePermalink()).thenReturn(true);

            var expectedUri = URI.create("https://abcd.xn--fiqs8s");
            assertEquals(expectedUri.toURL(), externalUrl.getRaw());
            assertEquals(expectedUri, externalUrl.get());

            var mockRequest = mock(HttpRequest.class);
            assertEquals(expectedUri.toURL(), externalUrl.getURL(mockRequest));
        }

        @Test
        void shouldKeepOriginalUrlWhenNormalizedUrlCannotBeParsed() throws MalformedURLException {
            var fakeUrl = new URL("https://abcd.中国/path with space");
            when(haloProperties.getExternalUrl()).thenReturn(fakeUrl);

            assertEquals(fakeUrl, externalUrl.getRaw());

            var mockRequest = mock(HttpRequest.class);
            assertEquals(fakeUrl, externalUrl.getURL(mockRequest));
        }
    }

    @Nested
    class SystemConfigSupplier {

        @Test
        void shouldGetUrlWhenUseAbsolutePermalink() throws Exception {
            var basic = new SystemSetting.Basic();
            basic.setExternalUrl("https://www.halo.run");
            when(systemConfigFetcher.getBasic()).thenReturn(Mono.just(basic));
            when(haloProperties.isUseAbsolutePermalink()).thenReturn(true);
            externalUrl.onExtensionInitialized(null);
            assertEquals(URI.create("https://www.halo.run").toURL(), externalUrl.getRaw());
            assertEquals(URI.create("https://www.halo.run"), externalUrl.get());

            var mockRequest = mock(HttpRequest.class);
            assertEquals(URI.create("https://www.halo.run").toURL(), externalUrl.getURL(mockRequest));
        }

        @Test
        void shouldGetUrlWhenNotUsingAbsolutePermalink() throws MalformedURLException {
            var basic = new SystemSetting.Basic();
            basic.setExternalUrl("https://www.halo.run");
            when(systemConfigFetcher.getBasic()).thenReturn(Mono.just(basic));
            when(haloProperties.isUseAbsolutePermalink()).thenReturn(false);
            when(webFluxProperties.getBasePath()).thenReturn("/fake");
            externalUrl.onExtensionInitialized(null);

            assertEquals(URI.create("https://www.halo.run").toURL(), externalUrl.getRaw());
            assertEquals(URI.create("/fake"), externalUrl.get());
            var mockRequest = mock(HttpRequest.class);
            assertEquals(URI.create("https://www.halo.run").toURL(), externalUrl.getURL(mockRequest));
        }

        @Test
        void shouldConvertHostToAsciiWhenExternalUrlContainsIDN() throws MalformedURLException {
            var basic = new SystemSetting.Basic();
            basic.setExternalUrl("https://abcd.中国");
            when(systemConfigFetcher.getBasic()).thenReturn(Mono.just(basic));
            when(haloProperties.isUseAbsolutePermalink()).thenReturn(true);
            externalUrl.onExtensionInitialized(null);

            var expectedUri = URI.create("https://abcd.xn--fiqs8s");
            assertEquals(expectedUri.toURL(), externalUrl.getRaw());
            assertEquals(expectedUri, externalUrl.get());

            var mockRequest = mock(HttpRequest.class);
            assertEquals(expectedUri.toURL(), externalUrl.getURL(mockRequest));
        }
    }
}
