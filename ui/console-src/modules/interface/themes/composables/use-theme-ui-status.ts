import type { Theme, UiPluginResources } from "@halo-dev/api-client";
import { stores } from "@halo-dev/ui-shared";
import { computed, type ComputedRef, type Ref } from "vue";
import { usePluginModuleStore, type UiPluginDiagnostic } from "@/stores/plugin";
import {
  summarizePluginModule,
  type UiModuleSummary,
} from "../utils/ui-capabilities";

export type ThemeUiLoadState =
  | "none"
  | "invalid"
  | "not-loaded"
  | "pending"
  | "failed"
  | "registered";

interface UseThemeUiStatusReturn {
  providerName: ComputedRef<string | undefined>;
  loadState: ComputedRef<ThemeUiLoadState>;
  versionMismatch: ComputedRef<boolean>;
  diagnostics: ComputedRef<UiPluginDiagnostic[]>;
  moduleSummary: ComputedRef<UiModuleSummary | undefined>;
}

/**
 * Resolves the browser-side loading state of a theme's UI resources. It only
 * reads the registration and module stores; it never loads the theme's bundle
 * nor executes any extension callback.
 */
export function useThemeUiStatus(
  theme: Ref<Theme | undefined>,
  ui: Ref<UiPluginResources | undefined>
): UseThemeUiStatusReturn {
  const pluginModuleStore = usePluginModuleStore();
  const uiPluginsStore = stores.uiPlugins();

  const providerName = computed(() =>
    theme.value ? `theme:${theme.value.metadata.name}` : undefined
  );

  const registration = computed(() =>
    providerName.value ? uiPluginsStore.get(providerName.value) : undefined
  );

  const versionMismatch = computed(
    () =>
      !!registration.value &&
      !!theme.value?.spec.version &&
      registration.value.version !== theme.value.spec.version
  );

  const loadState = computed<ThemeUiLoadState>(() => {
    const kind = ui.value?.kind;
    if (!kind || kind === "none") {
      return "none";
    }
    if (kind === "invalid") {
      return "invalid";
    }
    if (!registration.value) {
      // Only the activated theme is loaded by the browser; anything else is
      // present on disk but not loaded.
      return "not-loaded";
    }
    return registration.value.status;
  });

  const diagnostics = computed(() =>
    pluginModuleStore.diagnostics.filter(
      (diagnostic) => diagnostic.name === providerName.value
    )
  );

  // Module declarations are only trustworthy for the currently registered
  // module of the exact selected version.
  const moduleSummary = computed(() => {
    if (
      loadState.value !== "registered" ||
      versionMismatch.value ||
      !providerName.value
    ) {
      return undefined;
    }
    const module = pluginModuleStore.pluginModuleMap[providerName.value];
    if (!module) {
      return undefined;
    }
    return summarizePluginModule(module);
  });

  return {
    providerName,
    loadState,
    versionMismatch,
    diagnostics,
    moduleSummary,
  };
}
