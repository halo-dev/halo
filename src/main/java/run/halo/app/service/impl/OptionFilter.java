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

    private final Set<String> defaultPrivateOptionKeys;

    private final OptionService optionService;

    public OptionFilter(OptionService optionService) {
        this.optionService = optionService;
        this.defaultPrivateOptionKeys = getDefaultPrivateOptionKeys();
    }

    private Set<String> getDefaultPrivateOptionKeys() {
        return Set.of(
            AliOssProperties.OSS_ACCESS_KEY.getValue(),
            AliOssProperties.OSS_ACCESS_SECRET.getValue(),
            ApiProperties.API_ACCESS_KEY.getValue()
        );
    }

    private Set<String> getConfiguredPrivateOptionKeys() {
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
     * Filter option keys to prevent outsider from accessing private options.
     *
     * @param optionKeys option key collection
     * @return filtered option keys
     */
    public Set<String> filter(Collection<String> optionKeys) {
        if (CollectionUtils.isEmpty(optionKeys)) {
            return Collections.emptySet();
        }

        return optionKeys.stream()
            .filter(Objects::nonNull)
            .filter(optionKey -> !optionKey.isBlank())
            .filter(optionKey -> !defaultPrivateOptionKeys.contains(optionKey))
            .filter(optionKey -> !getConfiguredPrivateOptionKeys().contains(optionKey))
            .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Filter option key to prevent outsider from accessing private option.
     *
     * @param optionKey option key
     * @return an optional of option key
     */
    public Optional<String> filter(String optionKey) {
        if (!StringUtils.hasText(optionKey)) {
            return Optional.empty();
        }
        if (defaultPrivateOptionKeys.contains(optionKey)) {
            return Optional.empty();
        }
        if (getConfiguredPrivateOptionKeys().contains(optionKey)) {
            return Optional.empty();
        }
        return Optional.of(optionKey);
    }
}
