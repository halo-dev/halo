package run.halo.app.service.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import run.halo.app.model.properties.AliOssProperties;
import run.halo.app.model.properties.ApiProperties;
import run.halo.app.model.support.HaloConst;
import run.halo.app.service.OptionService;

/**
 * Option filter for private options.
 *
 * @author johnniang
 * @date 2021-04-08
 */
public class OptionFilter {

    private final Set<String> defaultPrivateOptionNames;

    private final OptionService optionService;

    public OptionFilter(OptionService optionService) {
        this.optionService = optionService;
        this.defaultPrivateOptionNames = getDefaultPrivateOptionNames();
    }

    private Set<String> getDefaultPrivateOptionNames() {
        return Set.of(
            AliOssProperties.OSS_ACCESS_KEY.getValue(),
            AliOssProperties.OSS_ACCESS_SECRET.getValue(),
            ApiProperties.API_ACCESS_KEY.getValue()
        );
    }

    private Set<String> getConfiguredPrivateOptionNames() {
        // resolve configured private option names
        return optionService.getByKey(HaloConst.PRIVATE_OPTION_KEY, String.class)
            .map(privateOptions -> privateOptions.split(","))
            .map(Set::of)
            .orElse(Collections.emptySet())
            .stream()
            .map(String::trim)
            .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Filter option names to prevent outsider from accessing private options.
     *
     * @param optionNames option name collection
     * @return filtered option names
     */
    public Set<String> filter(Collection<String> optionNames) {
        if (CollectionUtils.isEmpty(optionNames)) {
            return Collections.emptySet();
        }

        return optionNames.stream()
            .filter(Objects::nonNull)
            .filter(optionName -> !optionName.isBlank())
            .filter(optionName -> !defaultPrivateOptionNames.contains(optionName))
            .filter(optionName -> !getConfiguredPrivateOptionNames().contains(optionName))
            .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Filter option name to prevent outsider from accessing private option.
     *
     * @param optionName option name
     * @return an optional of option name
     */
    public Optional<String> filter(String optionName) {
        if (!StringUtils.hasText(optionName)) {
            return Optional.empty();
        }
        if (defaultPrivateOptionNames.contains(optionName)) {
            return Optional.empty();
        }
        if (getConfiguredPrivateOptionNames().contains(optionName)) {
            return Optional.empty();
        }
        return Optional.of(optionName);
    }
}
