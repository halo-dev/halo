import { apiClient } from "@/utils/api-client";
import type { ConfigMap } from "@halo-dev/api-client";
import { defineStore } from "pinia";

interface SystemConfigMapState {
  configMap?: ConfigMap;
}

export const useSystemConfigMapStore = defineStore({
  id: "system-configmap",
  state: (): SystemConfigMapState => ({
    configMap: undefined,
  }),
  actions: {
    async fetchSystemConfigMap() {
      try {
        const { data } =
          await apiClient.extension.configMap.getv1alpha1ConfigMap(
            {
              name: "system",
            },
            { mute: true }
          );
        this.configMap = data;
      } catch (error) {
        console.error("Failed to fetch system configMap", error);
      }
    },
  },
});
