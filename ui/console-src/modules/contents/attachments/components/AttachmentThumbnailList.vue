<script lang="ts" setup>
import LazyImage from "@/components/image/LazyImage.vue";
import {
  GetThumbnailByUriSizeEnum,
  type Attachment,
} from "@halo-dev/api-client";
import { VTabbar } from "@halo-dev/components";
import { THUMBNAIL_WIDTH_MAP } from "@halo-dev/console-shared";
import { computed, ref, toRefs } from "vue";

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

const activeId = ref(thumbnails.value?.[0]?.size);
</script>
<template>
  <div v-if="thumbnails.length">
    <VTabbar
      v-model:active-id="activeId"
      :items="
        thumbnails.map((thumbnail) => ({
          id: thumbnail.size,
          label: `${THUMBNAIL_WIDTH_MAP[thumbnail.size]}w`,
        }))
      "
      type="outline"
    ></VTabbar>
    <div class="mt-3">
      <a
        v-for="thumbnail in thumbnails"
        :key="thumbnail.permalink"
        class="block"
        target="_blank"
        :href="thumbnail.permalink"
      >
        <LazyImage
          v-if="thumbnail.size === activeId"
          v-tooltip="{
            content: thumbnail.permalink,
            placement: 'bottom',
          }"
          :src="thumbnail.permalink"
          classes="max-w-full rounded"
        >
          <template #loading>
            <span class="text-gray-400">
              {{ $t("core.common.status.loading") }}...
            </span>
          </template>
          <template #error>
            <span class="text-red-400">
              {{ $t("core.common.status.loading_error") }}
            </span>
          </template>
        </LazyImage>
      </a>
    </div>
  </div>

  <span v-else>
    {{ $t("core.attachment.detail_modal.preview.not_support_thumbnail") }}
  </span>
</template>
