<script lang="ts" setup>
import LazyVideo from "@/components/video/LazyVideo.vue";
import { isImage } from "@/utils/image";
import { GetThumbnailByUriSizeEnum } from "@halo-dev/api-client";
import { utils } from "@halo-dev/ui-shared";
import mime from "mime/lite";
import { computed } from "vue";

const props = defineProps<{
  url: string;
}>();

const mediaType = computed(() => {
  return mime.getType(props.url);
});
</script>

<template>
  <img
    v-if="isImage(mediaType)"
    :src="utils.attachment.getThumbnailUrl(url, GetThumbnailByUriSizeEnum.S)"
    class="size-full object-cover"
  />
  <LazyVideo
    v-else-if="mediaType?.startsWith('video/')"
    classes="size-full object-cover"
    :src="url"
  />
  <AttachmentFileTypeIcon v-else :file-name="url" />
</template>
