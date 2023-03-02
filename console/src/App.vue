<script lang="ts" setup>
import { RouterView, useRoute } from "vue-router";
import { computed, watch } from "vue";
import { useTitle } from "@vueuse/core";
import { useFavicon } from "@vueuse/core";
import { useSystemConfigMapStore } from "./stores/system-configmap";
import { storeToRefs } from "pinia";

const { configMap } = storeToRefs(useSystemConfigMapStore());

const AppName = "Halo";
const route = useRoute();
const title = useTitle();

watch(
  () => route.name,
  () => {
    const { title: routeTitle } = route.meta;
    if (routeTitle) {
      title.value = `${routeTitle} - ${AppName}`;
      return;
    }
    title.value = AppName;
  }
);

// Favicon
const defaultFavicon = "/console/favicon.ico";

const favicon = computed(() => {
  if (!configMap?.value?.data?.["basic"]) {
    return defaultFavicon;
  }

  const basic = JSON.parse(configMap.value.data["basic"]);

  if (basic.favicon) {
    return basic.favicon;
  }

  return defaultFavicon;
});

useFavicon(favicon);
</script>

<template>
  <RouterView />
</template>

<style lang="scss">
body {
  overflow-y: overlay;
  background: #eff4f9;
}

*,
*::before,
*::after {
  box-sizing: border-box;
}

*::-webkit-scrollbar-track-piece {
  background-color: #f8f8f8;
  -webkit-border-radius: 2em;
  -moz-border-radius: 2em;
  border-radius: 2em;
}

*::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

*::-webkit-scrollbar-thumb {
  background-color: #ddd;
  background-clip: padding-box;
  -webkit-border-radius: 2em;
  -moz-border-radius: 2em;
  border-radius: 2em;
}

*::-webkit-scrollbar-thumb:hover {
  background-color: #bbb;
}

.v-popper__popper {
  outline: none;
}

.v-popper--theme-tooltip {
  pointer-events: none;
}
</style>
