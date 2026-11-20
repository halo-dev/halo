package run.halo.app.extension;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ExtensionClientContractTest {

    @Test
    void presentationClientsShareReadsWithoutInheritingWrites() {
        assertThat(ReactiveExtensionReadClient.class.isAssignableFrom(ReactiveExtensionClient.class))
                .isTrue();
        assertThat(ReactiveExtensionReadClient.class.isAssignableFrom(ReactivePresentationExtensionClient.class))
                .isTrue();
        assertThat(ReactiveExtensionClient.class.isAssignableFrom(ReactivePresentationExtensionClient.class))
                .isFalse();
        assertThat(ExtensionReadClient.class.isAssignableFrom(ExtensionClient.class))
                .isTrue();
    }
}
