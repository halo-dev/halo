<script lang="ts" setup>
import {
  GetThumbnailByUriSizeEnum,
  type Attachment,
} from "@halo-dev/api-client";
import { computed, toRefs } from "vue";
import AttachmentSingleThumbnailItem from "./AttachmentSingleThumbnailItem.vue";

const props = withDefaults(
  defineProps<{
    attachment?: Attachment;
  }>(),
  {
    attachment: undefined,
  }
);

const { attachment } = toRefs(props);

const sizeOrder: Record<GetThumbnailByUriSizeEnum, number> = {
  XL: 4,
  L: 3,
  M: 2,
  S: 1,
};

const thumbnails = computed(() => {
  return Object.entries(attachment.value?.status?.thumbnails || {})
    .sort(
      ([sizeA], [sizeB]) =>
        (sizeOrder[sizeB as GetThumbnailByUriSizeEnum] || 0) -
        (sizeOrder[sizeA as GetThumbnailByUriSizeEnum] || 0)
    )
    .map(([size, permalink]) => ({
      size: size as GetThumbnailByUriSizeEnum,
      permalink,
    }));
});
</script>
<template>
  <ul v-if="thumbnails.length" class="flex flex-col space-y-2">
    <AttachmentSingleThumbnailItem
      v-for="thumbnail in thumbnails"
      :key="thumbnail.size"
      :size="thumbnail.size"
      :permalink="thumbnail.permalink"
    />
  </ul>
  <span v-else>
    {{ $t("core.attachment.detail_modal.preview.not_support_thumbnail") }}
  </span>
</template>
