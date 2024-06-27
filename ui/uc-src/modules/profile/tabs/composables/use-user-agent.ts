import { UAParser } from "ua-parser-js";
import { computed } from "vue";

export function useUserAgent(userAgent?: string) {
  const ua = computed(() => new UAParser(userAgent));

  const os = computed(() =>
    [ua.value.getOS().name, ua.value.getOS().version].filter(Boolean).join(" ")
  );

  const browser = computed(() =>
    [ua.value.getBrowser().name, ua.value.getBrowser().version]
      .filter(Boolean)
      .join(" ")
  );

  return {
    ua,
    os,
    browser,
  };
}
