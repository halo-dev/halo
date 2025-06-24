<script setup lang="ts">
import type { OwnerInfo } from "@halo-dev/api-client";
import { VAvatar } from "@halo-dev/components";

defineProps<{
  owner: OwnerInfo;
}>();
</script>
<template>
  <div
    class="-m-1 p-1 inline-flex items-center gap-1.5 hover:bg-gray-100 rounded-lg cursor-pointer transition-colors"
  >
    <VAvatar circle :src="owner?.avatar" :alt="owner?.displayName" size="xs" />
    <span class="text-sm font-medium text-gray-900">
      {{ owner?.displayName }}
    </span>
  </div>
</template>
