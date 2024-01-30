package run.halo.app.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URI;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link DefaultExternalLinkProcessor}.
 *
 * @author guqing
 * @since 2.9.0
 */
@ExtendWith(MockitoExtension.class)
class DefaultExternalLinkProcessorTest {

    @Mock
    private ExternalUrlSupplier externalUrlSupplier;

    @InjectMocks
    DefaultExternalLinkProcessor externalLinkProcessor;

    @Test
    void processWhenLinkIsEmpty() {
        assertThat(externalLinkProcessor.processLink(null)).isNull();
        assertThat(externalLinkProcessor.processLink("")).isEmpty();
    }

    @Test
    void process() throws MalformedURLException {
        when(externalUrlSupplier.getRaw()).thenReturn(null);
        assertThat(externalLinkProcessor.processLink("/test")).isEqualTo("/test");

        when(externalUrlSupplier.getRaw()).thenReturn(URI.create("https://halo.run").toURL());
        assertThat(externalLinkProcessor.processLink("/test")).isEqualTo("https://halo.run/test");

        assertThat(externalLinkProcessor.processLink("https://guqing.xyz/test"))
            .isEqualTo("https://guqing.xyz/test");

        when(externalUrlSupplier.getRaw()).thenReturn(URI.create("https://halo.run/").toURL());
        assertThat(externalLinkProcessor.processLink("/test")).isEqualTo("https://halo.run/test");
    }
}