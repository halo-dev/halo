package run.halo.app.theme.finders.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.attachment.ThumbnailService;

/**
 * Tests for {@link ThumbnailFinderImpl}.
 *
 * @author guqing
 * @since 2.20.0
 */
@ExtendWith(MockitoExtension.class)
class ThumbnailFinderImplTest {

    @Mock
    ThumbnailService thumbnailService;

    @InjectMocks
    ThumbnailFinderImpl thumbnailFinder;

    @Test
    void shouldNotGenWhenUriIsInvalid() {
        thumbnailFinder.gen("invalid uri", "l")
            .as(StepVerifier::create)
            .expectNext("invalid uri")
            .verifyComplete();

        verify(thumbnailService, times(0)).generate(any(), any());
    }

    @Test
    void shouldGenWhenUriIsValid() {
        when(thumbnailService.get(any(), any()))
            .thenReturn(Mono.just(URI.create("/test-thumb.jpg")));
        thumbnailFinder.gen("/test.jpg", "l")
            .as(StepVerifier::create)
            .expectNext("/test-thumb.jpg")
            .verifyComplete();

        verify(thumbnailService).get(any(), any());
    }
}
