<script lang="ts" setup>
import { RouterView, useRoute } from "vue-router";
import { VDialogProvider } from "@halo-dev/components";
import { watch } from "vue";
import { useTitle } from "@vueuse/core";

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
</script>

<template>
  <VDialogProvider>
    <RouterView />
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
