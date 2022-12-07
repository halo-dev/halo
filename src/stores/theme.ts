import { apiClient } from "@/utils/api-client";
import type { Theme } from "@halo-dev/api-client";
import { defineStore } from "pinia";
import { ref } from "vue";

export const useThemeStore = defineStore("theme", () => {
  const activatedTheme = ref<Theme>();

  async function fetchActivatedTheme() {
    try {
      const { data } = await apiClient.extension.configMap.getv1alpha1ConfigMap(
        {
          name: "system",
        },
        { mute: true }
      );

      if (!data.data?.theme) {
        return;
      }

      const themeConfig = JSON.parse(data.data.theme);

      const { data: themeData } =
        await apiClient.extension.theme.getthemeHaloRunV1alpha1Theme(
          {
            name: themeConfig.active,
          },
          {
            mute: true,
          }
        );

      activatedTheme.value = themeData;
    } catch (e) {
      console.error("Failed to fetch active theme", e);
    }
  }

  return { activatedTheme, fetchActivatedTheme };
});
