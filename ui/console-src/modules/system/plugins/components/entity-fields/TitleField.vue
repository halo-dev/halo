<script lang="ts" setup>
import { pluginLabels } from "@/constants/labels";
import { type Plugin } from "@halo-dev/api-client";
import { VEntityField, VTag } from "@halo-dev/components";

withDefaults(
  defineProps<{
    plugin: Plugin;
  }>(),
  {}
);
</script>

<template>
  <VEntityField
    :title="plugin.spec.displayName"
    :description="plugin.spec.description"
    :route="{
      name: 'PluginDetail',
      params: { name: plugin.metadata.name },
    }"
  >
    <template
      v-if="plugin.metadata.labels?.[pluginLabels.SYSTEM_RESERVED] === 'true'"
      #extra
    >
      <VTag>
        {{ $t("core.plugin.list.fields.system_reserved") }}
      </VTag>
    </template>
  </VEntityField>
</template>
