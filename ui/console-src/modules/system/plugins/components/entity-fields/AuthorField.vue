<script lang="ts" setup>
import type { Plugin } from "@halo-dev/api-client";
import { VEntityField } from "@halo-dev/components";
withDefaults(
  defineProps<{
    plugin: Plugin;
  }>(),
  {}
);
</script>

<template>
  <VEntityField v-if="plugin.spec.author">
    <template #description>
      <a
        :href="plugin.spec.author.website"
        class="hidden text-sm text-gray-500 hover:text-gray-900 sm:block"
        target="_blank"
      >
        @{{ plugin.spec.author.name }}
      </a>
    </template>
  </VEntityField>
</template>
