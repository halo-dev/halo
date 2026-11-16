package run.halo.app.search;

import java.time.Duration;
import java.util.Objects;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.Validator;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import run.halo.app.infra.exception.RequestBodyValidationException;
import run.halo.app.localization.LocalizationContext;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;

@Service
public class SearchServiceImpl implements SearchService {

    private final Validator validator;

    private final ExtensionGetter extensionGetter;

    public SearchServiceImpl(Validator validator, ExtensionGetter extensionGetter) {
        this.validator = validator;
        this.extensionGetter = extensionGetter;
    }

    @Override
    public Mono<SearchResult> search(SearchOption option) {
        var sourceOption = forLocale(option, null);
        var errors = validator.validateObject(sourceOption);
        if (errors.hasErrors()) {
            return Mono.error(new RequestBodyValidationException(errors));
        }
        return extensionGetter
                .getEnabledExtension(SearchEngine.class)
                .filter(SearchEngine::available)
                .switchIfEmpty(Mono.error(SearchEngineUnavailableException::new))
                .flatMap(engine -> resolveOption(engine, sourceOption)
                        .flatMap(resolved -> Mono.fromSupplier(() -> engine.search(resolved))
                                .subscribeOn(Schedulers.boundedElastic())));
    }

    private Mono<SearchOption> resolveOption(SearchEngine engine, SearchOption sourceOption) {
        if (!engine.supportsLocalizedDocuments()) {
            return Mono.just(sourceOption);
        }
        return LocalizationContext.current()
                .filter(LocalizationContext::enabled)
                .flatMap(context -> Mono.defer(
                                () -> extensionGetter.getEnabledExtension(HaloDocumentVariantProvider.class))
                        .timeout(Duration.ofSeconds(2))
                        .map(provider ->
                                forLocale(sourceOption, context.targetLocale().toLanguageTag()))
                        .onErrorResume(error -> Mono.empty()))
                .defaultIfEmpty(sourceOption);
    }

    private static SearchOption forLocale(SearchOption option, String locale) {
        if (Objects.equals(option.getLocale(), locale)) {
            return option;
        }
        var copy = new SearchOption();
        BeanUtils.copyProperties(option, copy);
        copy.setLocale(locale);
        return copy;
    }
}
