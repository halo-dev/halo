package run.halo.app.content.permalinks;

import run.halo.app.extension.GroupVersionKind;

public record ExtensionLocator(GroupVersionKind gvk, String name, String slug) {
}
