<script lang="ts" setup>
import { VEntityField, VSwitch } from "@halo-dev/components";
import type { Plugin } from "@halo-dev/api-client";
import { usePluginLifeCycle } from "../../composables/use-plugin";
import { toRefs } from "vue";
const props = withDefaults(
  defineProps<{
    plugin: Plugin;
  }>(),
  {}
);

const { plugin } = toRefs(props);

const { changingStatus, changeStatus } = usePluginLifeCycle(plugin);
</script>

<template>
  <VEntityField v-permission="['system:plugins:manage']">
    <template #description>
      <div class="flex items-center">
        <VSwitch
          :model-value="plugin.spec.enabled"
          :disabled="changingStatus"
          @click="changeStatus"
        />
      </div>
    </template>
  </VEntityField>
</template>
