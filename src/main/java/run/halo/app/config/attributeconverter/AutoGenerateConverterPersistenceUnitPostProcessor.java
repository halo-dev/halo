package run.halo.app.config.attributeconverter;

import static java.util.stream.Collectors.toUnmodifiableSet;

import java.util.Set;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitPostProcessor;
import org.springframework.util.ClassUtils;
import run.halo.app.model.enums.ValueEnum;

/**
 * Attribute converter persistence unit post processor.
 *
 * @author johnniang
 */
class AutoGenerateConverterPersistenceUnitPostProcessor implements PersistenceUnitPostProcessor {

    private static final String PACKAGE_TO_SCAN = "run.halo.app";

    @Override
    public void postProcessPersistenceUnitInfo(MutablePersistenceUnitInfo pui) {
        var generator = new AttributeConverterAutoGenerator(ClassUtils.getDefaultClassLoader());

        findValueEnumClasses()
            .stream()
            .map(generator::generate)
            .map(Class::getName)
            .forEach(pui::addManagedClassName);
    }

    private Set<Class<?>> findValueEnumClasses() {
        var scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(ValueEnum.class));

        return scanner.findCandidateComponents(PACKAGE_TO_SCAN)
            .stream()
            .filter(bd -> bd.getBeanClassName() != null)
            .map(bd -> ClassUtils.resolveClassName(bd.getBeanClassName(), null))
            .collect(toUnmodifiableSet());
    }
}
