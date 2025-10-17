<script lang="ts" setup>
import LazyImage from "@/components/image/LazyImage.vue";
import { THUMBNAIL_WIDTH_MAP } from "@/utils/thumbnail";
import {
  GetThumbnailByUriSizeEnum,
  type Attachment,
} from "@halo-dev/api-client";
import { VTabbar } from "@halo-dev/components";
import { useElementVisibility } from "@vueuse/core";
import { computed, ref, toRefs, useTemplateRef } from "vue";

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

const wrapper = useTemplateRef<HTMLDivElement | null>("wrapper");
const isWrapperVisible = useElementVisibility(wrapper, {
  once: true,
});
</script>
<template>
  <div v-if="thumbnails.length" ref="wrapper">
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
    <div v-if="isWrapperVisible" class="mt-3">
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
