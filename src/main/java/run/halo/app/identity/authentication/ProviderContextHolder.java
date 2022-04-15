package run.halo.app.identity.authentication;

/**
 * A holder of {@link ProviderContext} that associates it with the current thread using a {@code
 * ThreadLocal}.
 *
 * @author guqing
 * @see ProviderContext
 * @see ProviderContextFilter
 * @since 2.0.0
 */
public final class ProviderContextHolder {
    private static final ThreadLocal<ProviderContext> holder = new ThreadLocal<>();

    private ProviderContextHolder() {
    }

    /**
     * Returns the {@link ProviderContext} bound to the current thread.
     *
     * @return the {@link ProviderContext}
     */
    public static ProviderContext getProviderContext() {
        return holder.get();
    }

    /**
     * Bind the given {@link ProviderContext} to the current thread.
     *
     * @param providerContext the {@link ProviderContext}
     */
    public static void setProviderContext(ProviderContext providerContext) {
        if (providerContext == null) {
            resetProviderContext();
        } else {
            holder.set(providerContext);
        }
    }

    /**
     * Reset the {@link ProviderContext} bound to the current thread.
     */
    public static void resetProviderContext() {
        holder.remove();
    }

}