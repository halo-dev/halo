package run.halo.app.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.server.handler.ResponseStatusExceptionHandler;
import reactor.core.publisher.Mono;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;

@ExtendWith(MockitoExtension.class)
class IndexEndpointTest {

    @Mock
    ExtensionGetter extensionGetter;

    @Mock
    Validator validator;

    @InjectMocks
    IndexEndpoint endpoint;

    WebTestClient client;

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToRouterFunction(endpoint.endpoint())
            .handlerStrategies(HandlerStrategies.builder()
                .exceptionHandler(new ResponseStatusExceptionHandler())
                .build())
            .build();
    }

    @Test
    void shouldResponseBadRequestIfNotRequestBody() {
        client.post().uri("/indices/-/search")
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    void shouldResponseBadRequestIfRequestBodyValidationFailed() {
        var option = new SearchOption();
        var errors = mock(Errors.class);
        when(errors.hasErrors()).thenReturn(true);
        when(validator.validateObject(any(SearchOption.class))).thenReturn(errors);

        client.post().uri("/indices/-/search")
            .bodyValue(option)
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    void shouldSearchCorrectly() {
        var option = new SearchOption();
        option.setKeyword("halo");

        var errors = mock(Errors.class);
        when(errors.hasErrors()).thenReturn(false);
        when(validator.validateObject(any(SearchOption.class))).thenReturn(errors);

        var searchEngine = mock(SearchEngine.class);
        when(searchEngine.available()).thenReturn(true);
        var searchResult = new SearchResult();
        when(searchEngine.search(any(SearchOption.class))).thenReturn(searchResult);
        when(extensionGetter.getEnabledExtension(SearchEngine.class))
            .thenReturn(Mono.just(searchEngine));

        client.post().uri("/indices/-/search")
            .bodyValue(option)
            .exchange()
            .expectStatus().isOk()
            .expectBody(SearchResult.class)
            .isEqualTo(searchResult);

        verify(searchEngine).search(assertArg(o -> {
            assertEquals("halo", o.getKeyword());
            // make sure the filters are overwritten
            assertTrue(o.getFilterExposed());
            assertTrue(o.getFilterPublished());
            assertFalse(o.getFilterRecycled());
        }));
    }

    @Test
    void shouldBeCompatibleWithOldSearchApi() {
        var searchResult = new SearchResult();
        var searchEngine = mock(SearchEngine.class);
        when(searchEngine.available()).thenReturn(true);
        when(searchEngine.search(any(SearchOption.class))).thenReturn(searchResult);
        when(extensionGetter.getEnabledExtension(SearchEngine.class))
            .thenReturn(Mono.just(searchEngine));

        var errors = mock(Errors.class);
        when(errors.hasErrors()).thenReturn(false);
        when(validator.validateObject(any(SearchOption.class))).thenReturn(errors);

        client.get().uri(uriBuilder -> uriBuilder.path("/indices/post")
                .queryParam("keyword", "halo")
                .build())
            .exchange()
            .expectStatus().isOk()
            .expectBody(SearchResult.class)
            .isEqualTo(searchResult);

        verify(searchEngine).search(assertArg(o -> {
            assertEquals("halo", o.getKeyword());
            // make sure the filters are overwritten
            assertTrue(o.getFilterExposed());
            assertTrue(o.getFilterPublished());
            assertFalse(o.getFilterRecycled());
        }));
    }

    @Test
    void shouldFailWhenSearchEngineIsUnavailable() {
        var searchEngine = mock(SearchEngine.class);
        when(searchEngine.available()).thenReturn(false);
        when(extensionGetter.getEnabledExtension(SearchEngine.class))
            .thenReturn(Mono.just(searchEngine));

        var errors = mock(Errors.class);
        when(errors.hasErrors()).thenReturn(false);
        when(validator.validateObject(any(SearchOption.class))).thenReturn(errors);

        client.post().uri("/indices/-/search")
            .bodyValue(new SearchOption())
            .exchange()
            .expectStatus().is4xxClientError();
    }

    @Test
    void ensureGroupVersionNotModified() {
        assertEquals("api.halo.run/v1alpha1", endpoint.groupVersion().toString());
    }
}