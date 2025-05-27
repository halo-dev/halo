package run.halo.app.extension;

import java.util.Objects;
import lombok.Builder;
import lombok.Getter;
import org.springframework.util.Assert;

public class WatcherExtensionMatchers {
    @Getter
    private final ExtensionClient client;
    private final GroupVersionKind gvk;
    private final ExtensionMatcher onAddMatcher;
    private final ExtensionMatcher onUpdateMatcher;
    private final ExtensionMatcher onDeleteMatcher;

    /**
     * Constructs a new {@link WatcherExtensionMatchers} with the given
     * {@link DefaultExtensionMatcher}.
     */
    @Builder(builderMethodName = "internalBuilder")
    public WatcherExtensionMatchers(ExtensionClient client,
        GroupVersionKind gvk, ExtensionMatcher onAddMatcher,
        ExtensionMatcher onUpdateMatcher, ExtensionMatcher onDeleteMatcher) {
        Assert.notNull(client, "The client must not be null.");
        Assert.notNull(gvk, "The gvk must not be null.");
        this.client = client;
        this.gvk = gvk;
        this.onAddMatcher =
            Objects.requireNonNullElseGet(onAddMatcher, () -> emptyMatcher(client, gvk));
        this.onUpdateMatcher =
            Objects.requireNonNullElseGet(onUpdateMatcher, () -> emptyMatcher(client, gvk));
        this.onDeleteMatcher =
            Objects.requireNonNullElseGet(onDeleteMatcher, () -> emptyMatcher(client, gvk));
    }

    public GroupVersionKind getGroupVersionKind() {
        return this.gvk;
    }

    public ExtensionMatcher onAddMatcher() {
        return delegateExtensionMatcher(this.onAddMatcher);
    }

    public ExtensionMatcher onUpdateMatcher() {
        return delegateExtensionMatcher(this.onUpdateMatcher);
    }

    public ExtensionMatcher onDeleteMatcher() {
        return delegateExtensionMatcher(this.onDeleteMatcher);
    }

    public static WatcherExtensionMatchersBuilder builder(ExtensionClient client,
        GroupVersionKind gvk) {
        return internalBuilder().gvk(gvk).client(client);
    }

    static ExtensionMatcher emptyMatcher(ExtensionClient client,
        GroupVersionKind gvk) {
        return DefaultExtensionMatcher.builder(client, gvk).build();
    }

    ExtensionMatcher delegateExtensionMatcher(ExtensionMatcher matcher) {
        return extension -> extension.groupVersionKind().equals(gvk) && matcher.match(extension);
    }
}
