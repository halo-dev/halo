import type { Theme } from "@halo-dev/api-client";
import { consoleApiClient } from "@halo-dev/api-client";
import { utils } from "@halo-dev/console-shared";
import { defineStore } from "pinia";
import { ref } from "vue";

export const useThemeStore = defineStore("theme", () => {
  const activatedTheme = ref<Theme>();

  async function fetchActivatedTheme() {
    if (!utils.permission.has(["system:themes:view"])) {
      return;
    }

    try {
      const { data } = await consoleApiClient.theme.theme.fetchActivatedTheme({
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
