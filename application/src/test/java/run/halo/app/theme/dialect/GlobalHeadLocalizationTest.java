package run.halo.app.theme.dialect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import org.thymeleaf.spring6.expression.ThymeleafEvaluationContext;
import org.thymeleaf.web.IWebExchange;
import reactor.core.publisher.Flux;
import run.halo.app.localization.LocalizationContext;
import run.halo.app.localization.LocalizationScope;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;

class GlobalHeadLocalizationTest {
    @Test
    void headProcessorsRetainTheRequestLocaleAcrossTheirBlockingSubscription() {
        var locale = new LocalizationContext(Locale.CHINESE, Locale.ENGLISH, List.of(), LocalizationScope.PUBLIC_PAGE);
        assertThat(processHead(locale)).isEqualTo(locale);
    }

    @Test
    void requestsWithoutLocalizationDoNotInventALocale() {
        assertThat(processHead(null)).isNull();
    }

    private LocalizationContext processHead(LocalizationContext locale) {
        var context = mock(ITemplateContext.class, withSettings().extraInterfaces(IWebContext.class));
        var exchange = mock(IWebExchange.class);
        when(((IWebContext) context).getExchange()).thenReturn(exchange);
        when(exchange.getAttributeValue(LocalizationContext.class.getName())).thenReturn(locale);
        var applicationContext = mock(ApplicationContext.class);
        when(context.getVariable(ThymeleafEvaluationContext.THYMELEAF_EVALUATION_CONTEXT_CONTEXT_VARIABLE_NAME))
                .thenReturn(new ThymeleafEvaluationContext(applicationContext, null));
        var extensions = mock(ExtensionGetter.class);
        @SuppressWarnings("unchecked")
        var provider = (ObjectProvider<ExtensionGetter>) mock(ObjectProvider.class);
        when(applicationContext.getBeanProvider(ExtensionGetter.class)).thenReturn(provider);
        when(provider.getIfUnique()).thenReturn(extensions);
        var received = new AtomicReference<LocalizationContext>();
        TemplateHeadProcessor processor = (templateContext, model, handler) ->
                LocalizationContext.current().doOnNext(received::set).then();
        when(extensions.getExtensions(TemplateHeadProcessor.class)).thenReturn(Flux.just(processor));
        var model = mock(IModel.class);
        var working = mock(IModel.class);
        when(model.size()).thenReturn(2);
        when(model.cloneModel()).thenReturn(working);
        when(working.size()).thenReturn(2);
        new GlobalHeadInjectionProcessor("halo").doProcess(context, model, mock(IElementModelStructureHandler.class));
        return received.get();
    }
}
