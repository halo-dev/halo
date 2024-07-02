package run.halo.app.search;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.infra.exception.RequestBodyValidationException;
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

        searchService.search(option)
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

        searchService.search(option)
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

        searchService.search(option)
            .as(StepVerifier::create)
            .expectError(SearchEngineUnavailableException.class);
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

        searchService.search(option)
            .as(StepVerifier::create)
            .expectNext(searchResult)
            .verifyComplete();
    }
}
