<script lang="ts" setup>
import { RouterView, useRoute } from "vue-router";
import { computed, watch, ref, reactive, onMounted, inject } from "vue";
import { useTitle } from "@vueuse/core";
import { useFavicon } from "@vueuse/core";
import { useSystemConfigMapStore } from "./stores/system-configmap";
import { storeToRefs } from "pinia";
import axios from "axios";
import { useI18n } from "vue-i18n";
import {
  useOverlayScrollbars,
  type UseOverlayScrollbarsParams,
} from "overlayscrollbars-vue";
import type { FormKitConfig } from "@formkit/core";
import { i18n } from "./locales";
import { AppName } from "./constants/app";

const { t } = useI18n();

const { configMap } = storeToRefs(useSystemConfigMapStore());

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
const globalInfoFavicon = ref("");

(async () => {
  const { data } = await axios.get(
    `${import.meta.env.VITE_API_URL}/actuator/globalinfo`,
    {
      withCredentials: true,
    }
  );
  if (data?.favicon) {
    globalInfoFavicon.value = data.favicon;
  }
})();

const favicon = computed(() => {
  if (configMap?.value?.data?.["basic"]) {
    const basic = JSON.parse(configMap.value.data["basic"]);

    if (basic.favicon) {
      return basic.favicon;
    }
  }

  if (globalInfoFavicon.value) {
    return globalInfoFavicon.value;
  }

  return defaultFavicon;
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
