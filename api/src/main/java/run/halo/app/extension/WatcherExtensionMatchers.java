package run.halo.app.extension;

import java.util.Objects;
import lombok.Builder;

public class WatcherExtensionMatchers {
    private final GroupVersionKind gvk;
    private final ExtensionMatcher onAddMatcher;
    private final ExtensionMatcher onUpdateMatcher;
    private final ExtensionMatcher onDeleteMatcher;

    /**
     * Constructs a new {@link WatcherExtensionMatchers} with the given
     * {@link DefaultExtensionMatcher}.
     */
    @Builder(builderMethodName = "internalBuilder")
    public WatcherExtensionMatchers(GroupVersionKind gvk, ExtensionMatcher onAddMatcher,
        ExtensionMatcher onUpdateMatcher, ExtensionMatcher onDeleteMatcher) {
        this.gvk = gvk;
        this.onAddMatcher = Objects.requireNonNullElse(onAddMatcher, emptyMatcher(gvk));
        this.onUpdateMatcher = Objects.requireNonNullElse(onUpdateMatcher, emptyMatcher(gvk));
        this.onDeleteMatcher = Objects.requireNonNullElse(onDeleteMatcher, emptyMatcher(gvk));
    }

    public GroupVersionKind getGroupVersionKind() {
        return this.gvk;
    }

    public ExtensionMatcher onAddMatcher() {
        return this.onAddMatcher;
    }

    public ExtensionMatcher onUpdateMatcher() {
        return this.onUpdateMatcher;
    }

    public ExtensionMatcher onDeleteMatcher() {
        return this.onDeleteMatcher;
    }

    public static WatcherExtensionMatchersBuilder builder(GroupVersionKind gvk) {
        return internalBuilder().gvk(gvk);
    }

    static ExtensionMatcher emptyMatcher(GroupVersionKind gvk) {
        return DefaultExtensionMatcher.builder(gvk).build();
    }
}
