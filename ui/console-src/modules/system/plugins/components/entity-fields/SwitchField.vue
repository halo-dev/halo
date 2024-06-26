<script lang="ts" setup>
import type { Plugin } from "@halo-dev/api-client";
import { VEntityField, VSwitch } from "@halo-dev/components";
import { toRefs } from "vue";
import { usePluginLifeCycle } from "../../composables/use-plugin";
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
          :loading="changingStatus"
          @click="changeStatus"
        />
      </div>
    </template>
  </VEntityField>
</template>
