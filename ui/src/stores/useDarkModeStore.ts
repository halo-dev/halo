import { useDark, useToggle } from "@vueuse/core";
import { defineStore } from "pinia";
import { computed, ref, watch } from "vue";

export type DarkModePreference = "light" | "dark" | "system";

const STORAGE_KEY = "halo-dark-mode-preference";

export const useDarkModeStore = defineStore("darkMode", () => {
  const preference = ref<DarkModePreference>(
    (localStorage.getItem(STORAGE_KEY) as DarkModePreference) || "system"
  );

  const systemIsDark = useDark({
    selector: "html",
    attribute: "class",
    valueDark: "dark",
    valueLight: "",
  });

  const isDark = computed(() => {
    if (preference.value === "system") {
      return systemIsDark.value;
    }
    return preference.value === "dark";
  });

  const toggleDark = useToggle(isDark);

  // Sync .dark class on <html> whenever isDark changes
  watch(
    isDark,
    (dark) => {
      const html = document.documentElement;
      if (dark) {
        html.classList.add("dark");
      } else {
        html.classList.remove("dark");
      }
    },
    { immediate: true }
  );

  // Persist preference
  watch(preference, (val) => {
    localStorage.setItem(STORAGE_KEY, val);
  });

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
