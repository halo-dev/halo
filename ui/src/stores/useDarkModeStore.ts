import { useMediaQuery, useStorage, useToggle } from "@vueuse/core";
import { defineStore } from "pinia";
import { computed, watch } from "vue";

export type DarkModePreference = "light" | "dark" | "system";

const STORAGE_KEY = "halo-dark-mode-preference";

export const useDarkModeStore = defineStore("darkMode", () => {
  const preference = useStorage<DarkModePreference>(STORAGE_KEY, "system");

  // Only READ system preference — don't let vueuse manage the DOM class
  const systemPrefersDark = useMediaQuery("(prefers-color-scheme: dark)");

  const isDark = computed(() => {
    if (preference.value === "system") {
      return systemPrefersDark.value;
    }
    return preference.value === "dark";
  });

  const toggleDark = useToggle(isDark);

  // Manage .dark class ourselves — no conflict with vueuse auto-management
  watch(
    isDark,
    (dark) => {
      document.documentElement.classList.toggle("dark", dark);
    },
    { immediate: true }
  );

  function setPreference(value: DarkModePreference) {
    preference.value = value;
  }

  function cyclePreference() {
    const order: DarkModePreference[] = ["light", "dark", "system"];
    const currentIndex = order.indexOf(preference.value);
    preference.value = order[(currentIndex + 1) % order.length];
  }

  return {
    preference,
    isDark,
    toggleDark,
    setPreference,
    cyclePreference,
  };
});
