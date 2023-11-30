<script lang="ts" setup>
import { RouterView, useRoute } from "vue-router";
import { computed, watch, reactive, onMounted, inject } from "vue";
import { useTitle } from "@vueuse/core";
import { useFavicon } from "@vueuse/core";
import { useI18n } from "vue-i18n";
import {
  useOverlayScrollbars,
  type UseOverlayScrollbarsParams,
} from "overlayscrollbars-vue";
import type { FormKitConfig } from "@formkit/core";
import { i18n } from "@/locales";
import { AppName } from "@/constants/app";
import { useGlobalInfoStore } from "@/stores/global-info";
import { useQuery } from "@tanstack/vue-query";
import { useUserStore } from "@/stores/user";

const { t } = useI18n();

const globalInfoStore = useGlobalInfoStore();
const { isAnonymous } = useUserStore();

const route = useRoute();
const title = useTitle();

watch(
  () => route.name,
  () => {
    const { title: routeTitle } = route.meta;
    if (routeTitle) {
      title.value = `${t(routeTitle)} - ${AppName}`;
      return;
    }
    title.value = AppName;
  }
);

// Favicon
const defaultFavicon = "/console/favicon.ico";
const favicon = computed(() => {
  return globalInfoStore.globalInfo?.favicon || defaultFavicon;
});

useFavicon(favicon);

// body scroll
const body = document.querySelector("body");
const reactiveParams = reactive<UseOverlayScrollbarsParams>({
  options: {
    scrollbars: {
      autoHide: "scroll",
      autoHideDelay: 600,
    },
  },
  defer: true,
});
const [initialize] = useOverlayScrollbars(reactiveParams);
onMounted(() => {
  if (body) initialize({ target: body });
});

// setup formkit locale
// see https://formkit.com/essentials/internationalization
const formkitLocales = {
  en: "en",
  zh: "zh",
  "en-US": "en",
  "zh-CN": "zh",
};
const formkitConfig = inject(Symbol.for("FormKitConfig")) as FormKitConfig;
formkitConfig.locale = formkitLocales[i18n.global.locale.value] || "zh";

// Fix 100vh issue in ios devices
function setViewportProperty(doc: HTMLElement) {
  let prevClientHeight: number;
  const customVar = "--vh";
  function handleResize() {
    const clientHeight = doc.clientHeight;
    if (clientHeight === prevClientHeight) return;
    requestAnimationFrame(function updateViewportHeight() {
      doc.style.setProperty(customVar, clientHeight * 0.01 + "px");
      prevClientHeight = clientHeight;
    });
  }
  handleResize();
  return handleResize;
}
window.addEventListener(
  "resize",
  setViewportProperty(document.documentElement)
);

// keep session alive
useQuery({
  queryKey: ["health", "keep-session-alive"],
  queryFn: () => fetch("/actuator/health"),
  refetchInterval: 1000 * 60 * 5, // 5 minutes
  refetchOnWindowFocus: true,
  enabled: computed(() => !isAnonymous),
});
</script>

<template>
  <RouterView />
</template>

<style lang="scss">
body {
  background: #eff4f9;
}

*,
*::before,
*::after {
  box-sizing: border-box;
}

.v-popper__popper {
  outline: none;
}

.v-popper--theme-tooltip {
  pointer-events: none;
}
</style>
