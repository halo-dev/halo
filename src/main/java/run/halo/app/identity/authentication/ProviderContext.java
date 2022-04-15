package run.halo.app.identity.authentication;

import java.util.function.Supplier;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * A context that holds information of the Provider.
 *
 * @author guqing
 * @date 2022-04-14
 */
public record ProviderContext(ProviderSettings providerSettings,
                              @Nullable Supplier<String> issuerSupplier) {
    /**
     * Constructs a {@code ProviderContext} using the provided parameters.
     *
     * @param providerSettings the provider settings
     * @param issuerSupplier a {@code Supplier} for the {@code URL} of the Provider's issuer
     * identifier
     */
    public ProviderContext {
        Assert.notNull(providerSettings, "providerSettings cannot be null");
    }

    /**
     * Returns the {@link ProviderSettings}.
     *
     * @return the {@link ProviderSettings}
     */
    @Override
    public ProviderSettings providerSettings() {
        return this.providerSettings;
    }

    /**
     * Returns the {@code URL} of the Provider's issuer identifier.
     * The issuer identifier is resolved from the constructor parameter {@code Supplier<String>}
     * or if not provided then defaults to {@link ProviderSettings#getIssuer()}.
     *
     * @return the {@code URL} of the Provider's issuer identifier
     */
    public String getIssuer() {
        return this.issuerSupplier != null
            ? this.issuerSupplier.get() :
            providerSettings().getIssuer();
    }
}

