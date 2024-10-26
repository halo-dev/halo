package run.halo.app.theme.dialect;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.thymeleaf.spring6.expression.ThymeleafEvaluationContext.THYMELEAF_EVALUATION_CONTEXT_CONTEXT_VARIABLE_NAME;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.ITemplateHandler;
import org.thymeleaf.model.IOpenElementTag;
import org.thymeleaf.model.IStandaloneElementTag;
import org.thymeleaf.spring6.expression.ThymeleafEvaluationContext;
import reactor.core.publisher.Mono;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;

@ExtendWith(MockitoExtension.class)
class HaloPostTemplateHandlerTest {

    HaloPostTemplateHandler postHandler;

    @Mock
    ITemplateContext templateContext;

    @Mock
    ITemplateHandler next;

    @Mock
    ApplicationContext applicationContext;

    @Mock
    IStandaloneElementTag standaloneElementTag;

    @Mock
    IOpenElementTag openElementTag;

    @Mock
    ObjectProvider<ExtensionGetter> extensionGetterProvider;

    @Mock
    ExtensionGetter extensionGetter;


    @BeforeEach
    void setUp() {
        postHandler = new HaloPostTemplateHandler();
        var evaluationContext = mock(ThymeleafEvaluationContext.class);
        when(evaluationContext.getApplicationContext()).thenReturn(applicationContext);
        when(templateContext.getVariable(THYMELEAF_EVALUATION_CONTEXT_CONTEXT_VARIABLE_NAME))
            .thenReturn(evaluationContext);
        when(applicationContext.getBeanProvider(ExtensionGetter.class))
            .thenReturn(extensionGetterProvider);
        when(extensionGetterProvider.getIfUnique()).thenReturn(extensionGetter);
    }

    @ParameterizedTest
    @MethodSource("provideEmptyElementTagProcessors")
    void shouldHandleStandaloneElementIfNoElementTagProcessors(
        List<ElementTagPostProcessor> processors
    ) {
        when(extensionGetter.getExtensionList(ElementTagPostProcessor.class))
            .thenReturn(processors);

        postHandler.setContext(templateContext);
        postHandler.setNext(next);
        postHandler.handleStandaloneElement(standaloneElementTag);
        verify(next).handleStandaloneElement(standaloneElementTag);
    }

    @Test
    void shouldHandleStandaloneElementIfOneElementTagProcessorProvided() {
        var processor = mock(ElementTagPostProcessor.class);
        var newTag = mock(IStandaloneElementTag.class);
        when(processor.process(SecureTemplateContextWrapper.wrap(templateContext),
            standaloneElementTag))
            .thenReturn(Mono.just(newTag));
        when(extensionGetter.getExtensionList(ElementTagPostProcessor.class))
            .thenReturn(List.of(processor));

        postHandler.setContext(templateContext);
        postHandler.setNext(next);
        postHandler.handleStandaloneElement(standaloneElementTag);
        verify(next).handleStandaloneElement(newTag);
    }

    @Test
    void shouldHandleStandaloneElementIfTagTypeChanged() {
        var processor = mock(ElementTagPostProcessor.class);
        var newTag = mock(IStandaloneElementTag.class);
        when(processor.process(SecureTemplateContextWrapper.wrap(templateContext),
            standaloneElementTag))
            .thenReturn(Mono.just(newTag));
        when(extensionGetter.getExtensionList(ElementTagPostProcessor.class))
            .thenReturn(List.of(processor));

        postHandler.setContext(templateContext);
        postHandler.setNext(next);
        postHandler.handleStandaloneElement(standaloneElementTag);
        verify(next).handleStandaloneElement(newTag);
    }

    @Test
    void shouldHandleStandaloneElementIfMoreElementTagProcessorsProvided() {
        var processor1 = mock(ElementTagPostProcessor.class);
        var processor2 = mock(ElementTagPostProcessor.class);
        var newTag1 = mock(IStandaloneElementTag.class);
        var newTag2 = mock(IStandaloneElementTag.class);
        when(processor1.process(SecureTemplateContextWrapper.wrap(templateContext),
            standaloneElementTag))
            .thenReturn(Mono.just(newTag1));
        when(processor2.process(SecureTemplateContextWrapper.wrap(templateContext), newTag1))
            .thenReturn(Mono.just(newTag2));
        when(extensionGetter.getExtensionList(ElementTagPostProcessor.class))
            .thenReturn(List.of(processor1, processor2));

        postHandler.setContext(templateContext);
        postHandler.setNext(next);
        postHandler.handleStandaloneElement(standaloneElementTag);
        verify(next).handleStandaloneElement(newTag2);
    }

    @Test
    void shouldNotHandleIfProcessedTagTypeChanged() {
        var processor = mock(ElementTagPostProcessor.class);
        var newTag = mock(IOpenElementTag.class);
        when(processor.process(SecureTemplateContextWrapper.wrap(templateContext),
            standaloneElementTag))
            .thenReturn(Mono.just(newTag));
        when(extensionGetter.getExtensionList(ElementTagPostProcessor.class))
            .thenReturn(List.of(processor));

        postHandler.setContext(templateContext);
        postHandler.setNext(next);
        assertThrows(ClassCastException.class,
            () -> postHandler.handleStandaloneElement(standaloneElementTag)
        );
    }

    static Stream<List<ElementTagPostProcessor>> provideEmptyElementTagProcessors() {
        return Stream.of(
            null,
            List.of()
        );
    }

}