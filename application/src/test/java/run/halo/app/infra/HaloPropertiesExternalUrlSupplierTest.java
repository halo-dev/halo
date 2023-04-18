package run.halo.app.infra;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URI;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpRequest;
import run.halo.app.infra.properties.HaloProperties;

@ExtendWith(MockitoExtension.class)
class HaloPropertiesExternalUrlSupplierTest {

    @Mock
    HaloProperties haloProperties;

    @InjectMocks
    HaloPropertiesExternalUrlSupplier externalUrl;

    @Test
    void getURIWhenUsingAbsolutePermalink() throws MalformedURLException {
        var fakeUri = URI.create("https://halo.run/fake");
        when(haloProperties.getExternalUrl()).thenReturn(fakeUri.toURL());
        when(haloProperties.isUseAbsolutePermalink()).thenReturn(true);

        assertEquals(fakeUri, externalUrl.get());
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
        assertEquals(fakeUri.toURL(), url);
    }
}