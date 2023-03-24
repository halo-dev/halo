package run.halo.app.infra;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import run.halo.app.infra.properties.HaloProperties;

@ExtendWith(MockitoExtension.class)
class HaloPropertiesExternalUrlSupplierTest {

    @Mock
    HaloProperties haloProperties;

    @InjectMocks
    HaloPropertiesExternalUrlSupplier externalUrl;

    @Test
    void get() {
        URI fakeUri = URI.create("fake-url");
        Mockito.when(haloProperties.getExternalUrl()).thenReturn(fakeUri);
        assertEquals(fakeUri, externalUrl.get());
    }
}