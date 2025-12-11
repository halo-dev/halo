import type { PluginModule } from "@halo-dev/ui-shared";
import { defineStore } from "pinia";
import { computed, shallowRef } from "vue";

export const usePluginModuleStore = defineStore("plugin", () => {
  const pluginModuleMap = shallowRef<Record<string, PluginModule>>({});

  function registerPluginModule(name: string, pluginModule: PluginModule) {
    pluginModuleMap.value = {
      ...pluginModuleMap.value,
      [name]: pluginModule,
    };
  }

  const pluginModules = computed(() => {
    return Object.values(pluginModuleMap.value);
  });

  return { pluginModuleMap, pluginModules, registerPluginModule };
});
