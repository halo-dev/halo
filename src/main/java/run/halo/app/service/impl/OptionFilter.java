package run.halo.app.service.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.util.CollectionUtils;
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

    /**
     * Filter option names to prevent outside from accessing private options.
     *
     * @param optionNames option name collection
     * @return filtered option names
     */
    public Set<String> filter(Collection<String> optionNames) {
        if (CollectionUtils.isEmpty(optionNames)) {
            return Collections.emptySet();
        }
        // resolve configured private option names
        Set<String> configuredPrivateOptionNames =
            optionService.getByKey(HaloConst.PRIVATE_OPTION_KEY, String.class)
                .map(privateOptions -> privateOptions.split(","))
                .map(Set::of)
                .orElse(Collections.emptySet())
                .stream().map(String::trim).collect(Collectors.toUnmodifiableSet());

        return optionNames.stream()
            .filter(optionName -> !optionName.isBlank())
            .filter(optionName -> !defaultPrivateOptionNames.contains(optionName))
            .filter(optionName -> !configuredPrivateOptionNames.contains(optionName))
            .collect(Collectors.toUnmodifiableSet());
    }
}
