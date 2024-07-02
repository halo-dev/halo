package run.halo.app.search;

import org.springframework.stereotype.Service;
import org.springframework.validation.Validator;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import run.halo.app.infra.exception.RequestBodyValidationException;
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
        // validate the option
        var errors = validator.validateObject(option);
        if (errors.hasErrors()) {
            return Mono.error(new RequestBodyValidationException(errors));
        }
        return extensionGetter.getEnabledExtension(SearchEngine.class)
            .filter(SearchEngine::available)
            .switchIfEmpty(Mono.error(SearchEngineUnavailableException::new))
            .flatMap(searchEngine -> Mono.fromSupplier(() ->
                searchEngine.search(option)
            ).subscribeOn(Schedulers.boundedElastic()));
    }
}
