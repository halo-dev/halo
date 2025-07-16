<script lang="ts" setup>
import { generateThumbnailUrl } from "@/utils/thumbnail";
import type { ListedSinglePage } from "@halo-dev/api-client";
import { VEntityField } from "@halo-dev/components";

withDefaults(
  defineProps<{
    singlePage: ListedSinglePage;
  }>(),
  {}
);
</script>

<template>
  <VEntityField v-if="singlePage.page.spec.cover">
    <template #description>
      <div class="aspect-h-2 aspect-w-3 w-20 overflow-hidden rounded-md">
        <img
          class="h-full w-full object-cover"
          :src="generateThumbnailUrl(singlePage.page.spec.cover, 's')"
        />
      </div>
    </template>
  </VEntityField>
</template>
