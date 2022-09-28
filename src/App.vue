<script lang="ts" setup>
import { RouterView, useRoute } from "vue-router";
import { VDialogProvider } from "@halo-dev/components";
import { onMounted, provide, ref, watch, type Ref } from "vue";
import { useTitle } from "@vueuse/core";
import GlobalSearchModal from "@/components/global-search/GlobalSearchModal.vue";

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

const globalSearchVisible = ref(false);

provide<Ref<boolean>>("globalSearchVisible", globalSearchVisible);

const isMac = /macintosh|mac os x/i.test(navigator.userAgent);

const handleKeybinding = (e: KeyboardEvent) => {
  const { key, ctrlKey, metaKey } = e;
  if (key === "k" && ((ctrlKey && !isMac) || metaKey)) {
    globalSearchVisible.value = true;
    e.preventDefault();
  }
};

onMounted(() => {
  document.addEventListener("keydown", handleKeybinding);
});
</script>

<template>
  <VDialogProvider>
    <RouterView />
    <GlobalSearchModal v-model:visible="globalSearchVisible" />
  </VDialogProvider>
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
</style>
