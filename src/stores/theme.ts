import { apiClient } from "@/utils/api-client";
import type { Theme } from "@halo-dev/api-client";
import { defineStore } from "pinia";

interface ThemeStoreState {
  activatedTheme?: Theme;
}

export const useThemeStore = defineStore("theme", {
  state: (): ThemeStoreState => ({
    activatedTheme: undefined,
  }),
  actions: {
    async fetchActivatedTheme() {
      try {
        const { data } =
          await apiClient.extension.configMap.getv1alpha1ConfigMap({
            name: "system",
          });

        if (!data.data?.theme) {
          // Todo: show error
          return;
        }

        const themeConfig = JSON.parse(data.data.theme);

        const { data: themeData } =
          await apiClient.extension.theme.getthemeHaloRunV1alpha1Theme({
            name: themeConfig.active,
          });

        this.activatedTheme = themeData;
      } catch (e) {
        console.error("Failed to fetch active theme", e);
      }
    },
  },
});
