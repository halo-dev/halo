package run.halo.app.localization;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.ReactivePresentationExtensionClient;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;

/** Real presentation boundaries for Finder regression fixtures. */
public final class PresentationTestSupport {
    private PresentationTestSupport() {}

    public static ReactivePresentationExtensionClient client(ReactiveExtensionClient source) {
        return client(source, mock(ResourceLocalizationService.class));
    }

    public static ReactivePresentationExtensionClient client(
            ReactiveExtensionClient source, ResourceLocalizationService localization) {
        return new DefaultReactivePresentationExtensionClient(source, service(localization));
    }

    public static ResourcePresentationService service(ResourceLocalizationService localization) {
        var extensions = mock(ExtensionGetter.class);
        lenient()
                .when(extensions.getEnabledExtension(ResourceLocalizationProvider.class))
                .thenReturn(reactor.core.publisher.Mono.empty());
        return new DefaultResourcePresentationService(localization, extensions);
    }
}
