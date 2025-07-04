import { useLocalStorage } from "@vueuse/core";
import { computed, watch } from "vue";

export type ColorScheme = "light" | "dark" | "system";

export function useColorScheme() {
  const preferredScheme = useLocalStorage<ColorScheme>(
    "halo.color-scheme",
    "system"
  );

  const systemScheme = computed(() => {
    if (typeof window === "undefined") return "light";
    return window.matchMedia("(prefers-color-scheme: dark)").matches
      ? "dark"
      : "light";
  });

  const currentScheme = computed(() => {
    if (preferredScheme.value === "system") {
      return systemScheme.value;
    }
    return preferredScheme.value;
  });

  const applyScheme = (scheme: "light" | "dark") => {
    const html = document.documentElement;

    if (scheme === "dark") {
      html.classList.add("theme-dark");
      html.classList.remove("theme-light");
    } else {
      html.classList.add("theme-light");
      html.classList.remove("theme-dark");
    }
  };

  // Watch for scheme changes and apply them
  watch(
    currentScheme,
    (newScheme) => {
      applyScheme(newScheme);
    },
    { immediate: true }
  );

  // Watch for system scheme changes when in system mode
  if (typeof window !== "undefined") {
    const mediaQuery = window.matchMedia("(prefers-color-scheme: dark)");
    mediaQuery.addEventListener("change", () => {
      if (preferredScheme.value === "system") {
        applyScheme(systemScheme.value);
      }
    });
  }

  const toggleScheme = () => {
    if (preferredScheme.value === "light") {
      preferredScheme.value = "dark";
    } else if (preferredScheme.value === "dark") {
      preferredScheme.value = "system";
    } else {
      preferredScheme.value = "light";
    }
  };

  const setScheme = (scheme: ColorScheme) => {
    preferredScheme.value = scheme;
  };

  return {
    preferredScheme,
    currentScheme,
    systemScheme,
    toggleScheme,
    setScheme,
  };
}
