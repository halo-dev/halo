<script setup lang="ts">
import { PluginStatusPhaseEnum, type Plugin } from "@halo-dev/api-client";
import { IconInformation, VButton } from "@halo-dev/components";
import { stores } from "@halo-dev/ui-shared";
import { computed } from "vue";

const props = withDefaults(
  defineProps<{
    plugin: Plugin;
  }>(),
  {}
);

const uiPluginsStore = stores.uiPlugins();
const currentJsModuleInfo = computed(() =>
  uiPluginsStore.get(props.plugin.metadata.name)
);

const needsReloadWindow = computed(() => {
  if (!currentJsModuleInfo.value) {
    return false;
  }

  const { version } = props.plugin.spec;
  const { phase } = props.plugin.status || {};

  const isStarted = PluginStatusPhaseEnum.Started === phase;

  return isStarted && version !== currentJsModuleInfo.value.version;
});

function handleReloadWindow() {
  window.location.reload();
}
</script>

<template>
  <VButton v-if="needsReloadWindow" size="xs" @click="handleReloadWindow">
    <template #icon>
      <IconInformation />
    </template>
    {{ $t("core.plugin.operations.reload_window.button") }}
  </VButton>
</template>

<style lang="scss" scoped></style>
