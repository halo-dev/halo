<script lang="ts" setup>
import {
  GetThumbnailByUriSizeEnum,
  type ListedSinglePage,
} from "@halo-dev/api-client";
import { VEntityField } from "@halo-dev/components";
import { utils } from "@halo-dev/console-shared";

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
          :src="
            utils.attachment.getThumbnailUrl(
              singlePage.page.spec.cover,
              GetThumbnailByUriSizeEnum.S
            )
          "
        />
      </div>
    </template>
  </VEntityField>
</template>
