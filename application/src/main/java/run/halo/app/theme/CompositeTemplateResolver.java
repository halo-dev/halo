package run.halo.app.theme;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolution;
import run.halo.app.infra.exception.NotFoundException;

/**
 * Composite template resolver to control execution flow of multiple template resolvers.
 *
 * @author johnniang
 */
class CompositeTemplateResolver implements ITemplateResolver {

    private final List<ITemplateResolver> resolvers;

    public CompositeTemplateResolver(Collection<ITemplateResolver> resolvers) {
        this.resolvers = Optional.ofNullable(resolvers).orElseGet(List::of)
            .stream()
            .distinct()
            // we keep the same order comparison as
            // org.thymeleaf.EngineConfiguration.TemplateResolverComparator
            .sorted(comparing(ITemplateResolver::getOrder, nullsLast(naturalOrder())))
            .toList();
    }

    @Override
    public String getName() {
        return resolvers.stream()
            .map(ITemplateResolver::getName)
            .collect(Collectors.joining(", "));
    }

    @Override
    public Integer getOrder() {
        // null order means to be the end of the resolvers.
        return null;
    }

    @Override
    public TemplateResolution resolveTemplate(IEngineConfiguration configuration,
        String ownerTemplate, String template, Map<String, Object> templateResolutionAttributes) {
        return resolvers.stream()
            .map(resolver -> resolver.resolveTemplate(
                configuration, ownerTemplate, template, templateResolutionAttributes)
            )
            .filter(Objects::nonNull)
            .findFirst()
            .orElseThrow(() -> new NotFoundException("Template " + template + " was not found."));
    }

}
