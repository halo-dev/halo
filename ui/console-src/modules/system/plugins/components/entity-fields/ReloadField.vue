<script setup lang="ts">
import { PluginStatusPhaseEnum, type Plugin } from "@halo-dev/api-client";
import { IconInformation, VButton } from "@halo-dev/components";
import { computed } from "vue";

const props = withDefaults(
  defineProps<{
    plugin: Plugin;
  }>(),
  {}
);

const enabledJsModulesInfo =
  (window["enabledPlugins"] as { name: string; version: string }[]) || [];

const currentJsModuleInfo = enabledJsModulesInfo.find((jsModuleInfo) => {
  return jsModuleInfo.name === props.plugin.metadata.name;
});

const needsReloadWindow = computed(() => {
  if (!currentJsModuleInfo) {
    return false;
  }

  const { version } = props.plugin.spec;
  const { phase } = props.plugin.status || {};

  const isStarted = PluginStatusPhaseEnum.Started === phase;

  return isStarted && version !== currentJsModuleInfo?.version;
});

function handleReloadWindow() {
  window.location.reload();
}
</script>

<template>
  <VButton v-if="needsReloadWindow" size="xs" @click="handleReloadWindow">
    <template #icon>
      <IconInformation class="h-full w-full" />
    </template>
    {{ $t("core.plugin.operations.reload_window.button") }}
  </VButton>
</template>

<style lang="scss" scoped></style>
