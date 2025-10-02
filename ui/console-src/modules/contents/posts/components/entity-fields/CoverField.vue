<script lang="ts" setup>
import { generateThumbnailUrl } from "@/utils/thumbnail";
import {
  GetThumbnailByUriSizeEnum,
  type ListedPost,
} from "@halo-dev/api-client";
import { VEntityField } from "@halo-dev/components";

withDefaults(
  defineProps<{
    post: ListedPost;
  }>(),
  {}
);
</script>

<template>
  <VEntityField v-if="post.post.spec.cover">
    <template #description>
      <div class="aspect-h-2 aspect-w-3 w-20 overflow-hidden rounded-md">
        <img
          class="h-full w-full object-cover"
          :src="
            generateThumbnailUrl(
              post.post.spec.cover,
              GetThumbnailByUriSizeEnum.S
            )
          "
        />
      </div>
    </template>
  </VEntityField>
</template>
