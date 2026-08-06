import type { PluginModule } from "@halo-dev/ui-shared";
import { defineStore } from "pinia";
import { computed, shallowRef } from "vue";

export type UiPluginFailureStage =
  | "discovery"
  | "style"
  | "entry"
  | "export"
  | "registration"
  | "rollback"
  | "chunk";

export interface UiPluginDiagnostic {
  name: string;
  type: "plugin" | "theme";
  stage: UiPluginFailureStage;
  message: string;
  reloadRequired?: boolean;
  incompleteRollback?: string[];
}

export const usePluginModuleStore = defineStore("plugin", () => {
  const pluginModuleMap = shallowRef<Record<string, PluginModule>>({});
  const diagnostics = shallowRef<UiPluginDiagnostic[]>([]);

  function registerPluginModule(name: string, pluginModule: PluginModule) {
    pluginModuleMap.value = {
      ...pluginModuleMap.value,
      [name]: pluginModule,
    };
  }

  function unregisterPluginModule(name: string) {
    const nextPluginModuleMap = { ...pluginModuleMap.value };
    delete nextPluginModuleMap[name];
    pluginModuleMap.value = nextPluginModuleMap;
  }

  function recordDiagnostic(diagnostic: UiPluginDiagnostic) {
    const duplicate = diagnostics.value.some(
      (item) =>
        item.name === diagnostic.name &&
        item.stage === diagnostic.stage &&
        item.message === diagnostic.message
    );
    if (!duplicate) {
      diagnostics.value = [...diagnostics.value, diagnostic];
    }
  }

  function clearDiagnostics() {
    diagnostics.value = [];
  }

  const pluginModules = computed(() => {
    return Object.values(pluginModuleMap.value);
  });

  return {
    pluginModuleMap,
    pluginModules,
    diagnostics,
    registerPluginModule,
    unregisterPluginModule,
    recordDiagnostic,
    clearDiagnostics,
  };
});
