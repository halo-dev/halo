package run.halo.app.theme.router;

import run.halo.app.extension.GroupVersionKind;

/**
 * A record class for holding gvk and name.
 *
 * @author guqing
 * @since 2.0.0
 */
public record GvkName(GroupVersionKind gvk, String name) {
}
