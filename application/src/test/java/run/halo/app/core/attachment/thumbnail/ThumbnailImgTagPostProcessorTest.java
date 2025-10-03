package run.halo.app.core.attachment.thumbnail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.thymeleaf.templatemode.TemplateMode.HTML;

import java.net.URI;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.StandardModelFactory;
import org.thymeleaf.model.AttributeValueQuotes;
import org.thymeleaf.model.IModelFactory;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.attachment.ThumbnailSize;

@ExtendWith(MockitoExtension.class)
class ThumbnailImgTagPostProcessorTest {

    @Mock
    ThumbnailService thumbnailService;

    @Mock
    ITemplateContext templateContext;

    @InjectMocks
    ThumbnailImgTagPostProcessor processor;

    IModelFactory modelFactory;

    @BeforeEach
    void setUp() {
        var templateEngine = new TemplateEngine();
        this.modelFactory = new StandardModelFactory(templateEngine.getConfiguration(), HTML);
    }

    @Test
    void shouldReturnEmptyIfImgTagWithoutSrc() {
        var imgTag = modelFactory.createStandaloneElementTag("img");

        processor.process(templateContext, imgTag)
            .as(StepVerifier::create)
            .verifyComplete();
    }

    @Test
    void shouldReturnEmptyIfImgTagWithSrcSet() {
        var imgTag = modelFactory.createStandaloneElementTag(
            "img",
            Map.of("src", "/halo.png",
                "srcset", "fake-srcset"),
            AttributeValueQuotes.DOUBLE,
            true,
            true);

        processor.process(templateContext, imgTag)
            .as(StepVerifier::create)
            .verifyComplete();
    }

    @Test
    void shouldReturnEmptyIfNotImgTag() {
        var imgTag = modelFactory.createStandaloneElementTag("not-a-img");

        processor.process(templateContext, imgTag)
            .as(StepVerifier::create)
            .verifyComplete();
    }

    @Test
    void shouldReturnEmptyIfNoThumbnailsFound() {
        var imgTag = modelFactory.createStandaloneElementTag("img", "src", "/halo.png");

        when(thumbnailService.get(URI.create("/halo.png")))
            .thenReturn(Mono.just(Map.of()));

        processor.process(templateContext, imgTag)
            .as(StepVerifier::create)
            .verifyComplete();
    }

    @Test
    void shouldReturnTagIfImgTagWithSrc() {
        var imgTag = modelFactory.createStandaloneElementTag("img", "src", "/halo.png");

        when(templateContext.getModelFactory()).thenReturn(modelFactory);
        when(thumbnailService.get(URI.create("/halo.png")))
            .thenReturn(Mono.just(Map.of(ThumbnailSize.S, URI.create("/halo.png?width=400"))));

        processor.process(templateContext, imgTag)
            .as(StepVerifier::create)
            .assertNext(tag -> {
                var srcset = tag.getAttribute("srcset");
                assertEquals("/halo.png?width=400 400w", srcset.getValue());
                assertTrue(tag.hasAttribute("sizes"));
            })
            .verifyComplete();
    }

    @Test
    void shouldReturnTagIfImgTagWithSrcAndSizes() {
        var imgTag = modelFactory.createStandaloneElementTag(
            "img",
            Map.of("src", "/halo.png",
                "sizes", "fake-sizes"),
            AttributeValueQuotes.DOUBLE,
            true,
            true);

        when(templateContext.getModelFactory()).thenReturn(modelFactory);
        when(thumbnailService.get(URI.create("/halo.png")))
            .thenReturn(Mono.just(Map.of(ThumbnailSize.S, URI.create("/halo.png?width=400"))));

        processor.process(templateContext, imgTag)
            .as(StepVerifier::create)
            .assertNext(tag -> {
                assertEquals("/halo.png?width=400 400w", tag.getAttribute("srcset").getValue());
                assertEquals("fake-sizes", tag.getAttribute("sizes").getValue());
            })
            .verifyComplete();
    }

}