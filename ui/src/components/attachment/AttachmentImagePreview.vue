<script lang="ts" setup>
import {
  GetThumbnailByUriSizeEnum,
  type Attachment,
} from "@halo-dev/api-client";
import { VTabbar } from "@halo-dev/components";
import { THUMBNAIL_WIDTH_MAP } from "@halo-dev/ui-shared";
import { UseImage } from "@vueuse/components";
import { computed, ref, toRefs } from "vue";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

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

const items = computed(
  (): { label: string; id: string; permalink?: string }[] => {
    return [
      {
        label: t("core.attachment.common.text.original"),
        id: "original",
        permalink: attachment.value?.status?.permalink,
      },
      ...Object.entries(attachment.value?.status?.thumbnails || {})
        .sort(
          ([sizeA], [sizeB]) =>
            (sizeOrder[sizeB as GetThumbnailByUriSizeEnum] || 0) -
            (sizeOrder[sizeA as GetThumbnailByUriSizeEnum] || 0)
        )
        .map(([size, permalink]) => ({
          label: `${THUMBNAIL_WIDTH_MAP[size as GetThumbnailByUriSizeEnum]}w`,
          id: size,
          permalink,
        })),
    ];
  }
);

const activeId = ref(items.value?.[0]?.id);
</script>
<template>
  <VTabbar
    v-model:active-id="activeId"
    :items="
      items.map((item) => ({
        id: item.id,
        label: item.label,
      }))
    "
    type="outline"
  ></VTabbar>
  <div class="mt-3">
    <a
      v-for="item in items"
      :key="item.permalink"
      class="block"
      target="_blank"
      :href="item.permalink"
    >
      <span v-if="!item.permalink" class="text-red-400">
        {{ $t("core.common.status.loading_error") }}
      </span>
      <UseImage
        v-else-if="item.id === activeId"
        v-tooltip="{
          content: item.permalink,
          placement: 'bottom',
        }"
        :src="item.permalink"
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
        <template #default>
          <Transition appear name="fade">
            <img :src="item.permalink" class="max-w-full rounded" />
          </Transition>
        </template>
      </UseImage>
    </a>
  </div>
</template>
