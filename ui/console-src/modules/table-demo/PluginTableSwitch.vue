<script lang="ts" setup>
import { usePluginLifeCycle } from "@console/modules/system/plugins/composables/use-plugin";
import type { Plugin } from "@halo-dev/api-client";
import { VSwitch } from "@halo-dev/components";
import { toRefs } from "vue";

const props = defineProps<{ plugin: Plugin }>();

const { plugin } = toRefs(props);

const { changingStatus, changeStatus } = usePluginLifeCycle(plugin);
</script>

<template>
  <VSwitch
    v-permission="['system:plugins:manage']"
    :model-value="plugin.spec.enabled"
    :loading="changingStatus"
    @click="changeStatus"
  />
</template>
