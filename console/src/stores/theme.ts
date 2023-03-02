import { apiClient } from "@/utils/api-client";
import type { Theme } from "@halo-dev/api-client";
import { defineStore } from "pinia";
import { ref } from "vue";
import { usePermission } from "@/utils/permission";

export const useThemeStore = defineStore("theme", () => {
  const activatedTheme = ref<Theme>();

  const { currentUserHasPermission } = usePermission();

  async function fetchActivatedTheme() {
    if (!currentUserHasPermission(["system:themes:view"])) {
      return;
    }

    try {
      const { data } = await apiClient.theme.fetchActivatedTheme({
        mute: true,
      });

      if (data) {
        activatedTheme.value = data;
      }
    } catch (e) {
      console.error("Failed to fetch active theme", e);
    }
  }

  return { activatedTheme, fetchActivatedTheme };
});
