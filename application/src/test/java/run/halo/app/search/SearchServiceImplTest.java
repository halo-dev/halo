package run.halo.app.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.infra.exception.RequestBodyValidationException;
import run.halo.app.localization.LocalizationContext;
import run.halo.app.localization.LocalizationScope;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;

@ExtendWith(MockitoExtension.class)
class SearchServiceImplTest {

    @Mock
    Validator validator;

    @Mock
    ExtensionGetter extensionGetter;

    @InjectMocks
    SearchServiceImpl searchService;

    @Test
    void shouldThrowValidationErrorIfOptionIsInvalid() {
        var option = new SearchOption();
        option.setKeyword("halo");

        var errors = mock(Errors.class);
        when(errors.hasErrors()).thenReturn(true);
        when(validator.validateObject(option)).thenReturn(errors);

        searchService
                .search(option)
                .as(StepVerifier::create)
                .expectError(RequestBodyValidationException.class)
                .verify();
    }

    @Test
    void shouldThrowSearchEngineUnavailableExceptionIfNoSearchEngineFound() {
        var option = new SearchOption();
        option.setKeyword("halo");

        var errors = mock(Errors.class);
        when(errors.hasErrors()).thenReturn(false);
        when(validator.validateObject(option)).thenReturn(errors);

        when(extensionGetter.getEnabledExtension(SearchEngine.class)).thenReturn(Mono.empty());

        searchService
                .search(option)
                .as(StepVerifier::create)
                .expectError(SearchEngineUnavailableException.class)
                .verify();
    }

    @Test
    void shouldThrowSearchEngineUnavailableExceptionIfNoSearchEngineAvailable() {
        var option = new SearchOption();
        option.setKeyword("halo");

        var errors = mock(Errors.class);
        when(errors.hasErrors()).thenReturn(false);
        when(validator.validateObject(option)).thenReturn(errors);

        when(extensionGetter.getEnabledExtension(SearchEngine.class))
                .thenAnswer(invocation -> Mono.fromSupplier(() -> {
                    var searchEngine = mock(SearchEngine.class);
                    when(searchEngine.available()).thenReturn(false);
                    return searchEngine;
                }));

        searchService
                .search(option)
                .as(StepVerifier::create)
                .expectError(SearchEngineUnavailableException.class)
                .verify();
    }

    @Test
    void shouldSearch() {
        var option = new SearchOption();
        option.setKeyword("halo");

        var errors = mock(Errors.class);
        when(errors.hasErrors()).thenReturn(false);
        when(validator.validateObject(option)).thenReturn(errors);

        var searchResult = mock(SearchResult.class);
        when(extensionGetter.getEnabledExtension(SearchEngine.class))
                .thenAnswer(invocation -> Mono.fromSupplier(() -> {
                    var searchEngine = mock(SearchEngine.class);
                    when(searchEngine.available()).thenReturn(true);
                    when(searchEngine.search(option)).thenReturn(searchResult);
                    return searchEngine;
                }));

        searchService
                .search(option)
                .as(StepVerifier::create)
                .expectNext(searchResult)
                .verifyComplete();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void directSearchUsesTrustedLocaleOnlyForSupportedEngine(boolean localized) {
        var option = new SearchOption();
        option.setKeyword("halo");
        option.setLocale("de");
        var errors = mock(Errors.class);
        when(validator.validateObject(any(SearchOption.class))).thenReturn(errors);

        var engine = mock(SearchEngine.class);
        var result = new SearchResult();
        when(extensionGetter.getEnabledExtension(SearchEngine.class)).thenReturn(Mono.just(engine));
        when(engine.available()).thenReturn(true);
        when(engine.supportsLocalizedDocuments()).thenReturn(localized);
        when(engine.search(any(SearchOption.class))).thenReturn(result);
        if (localized) {
            HaloDocumentVariantProvider provider = source -> Flux.empty();
            when(extensionGetter.getEnabledExtension(HaloDocumentVariantProvider.class))
                    .thenReturn(Mono.just(provider));
        }
        var context = new LocalizationContext(
                Locale.SIMPLIFIED_CHINESE, Locale.ENGLISH, List.of(), LocalizationScope.PUBLIC_PAGE);

        StepVerifier.create(searchService
                        .search(option)
                        .contextWrite(reactor -> reactor.put(LocalizationContext.class, context)))
                .expectNext(result)
                .verifyComplete();
        verify(engine).search(assertArg(effective -> {
            assertThat(effective.getKeyword()).isEqualTo("halo");
            assertThat(effective.getLocale()).isEqualTo(localized ? "en" : null);
        }));
        assertThat(option.getLocale()).isEqualTo("de");
        if (!localized) {
            verify(extensionGetter, never()).getEnabledExtension(HaloDocumentVariantProvider.class);
        }
    }
}
