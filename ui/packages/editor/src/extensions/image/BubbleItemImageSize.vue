<script setup lang="ts">
import Input from "@/components/base/Input.vue";
import BubbleButton from "@/components/bubble/BubbleButton.vue";
import { i18n } from "@/locales";
import type { BubbleItemComponentProps } from "@/types";
import { VDropdown } from "@halo-dev/components";
import { computed } from "vue";
import MdiBackupRestore from "~icons/mdi/backup-restore";
import MdiImageSizeSelectActual from "~icons/mdi/image-size-select-actual";
import MdiImageSizeSelectLarge from "~icons/mdi/image-size-select-large";
import MdiImageSizeSelectSmall from "~icons/mdi/image-size-select-small";
import { ExtensionImage } from "./index";

const props = defineProps<BubbleItemComponentProps>();

const width = computed({
  get: () => {
    return props.editor.getAttributes(ExtensionImage.name).width;
  },
  set: (width: string) => {
    handleSetSize({ width, height: height.value });
  },
});

const height = computed({
  get: () => {
    return props.editor.getAttributes(ExtensionImage.name).height;
  },
  set: (height: string) => {
    handleSetSize({ width: width.value, height });
  },
});

function handleSetSize(size: { width?: string; height?: string }) {
  props.editor
    .chain()
    .updateAttributes(ExtensionImage.name, size)
    .setNodeSelection(props.editor.state.selection.from)
    .focus()
    .run();
}

const presetSizes = [
  {
    width: "25%",
    height: "auto",
    icon: MdiImageSizeSelectSmall,
    title: i18n.global.t("editor.extensions.image.small_size"),
  },
  {
    width: "50%",
    height: "auto",
    icon: MdiImageSizeSelectLarge,
    title: i18n.global.t("editor.extensions.image.medium_size"),
  },
  {
    width: "100%",
    height: "auto",
    icon: MdiImageSizeSelectActual,
    title: i18n.global.t("editor.extensions.image.large_size"),
  },
  {
    width: undefined,
    height: undefined,
    icon: MdiBackupRestore,
    title: i18n.global.t("editor.extensions.image.restore_size"),
  },
];
</script>

<template>
  <VDropdown
    v-if="visible?.({ editor })"
    class="inline-flex"
    :auto-hide="true"
    :distance="10"
  >
    <BubbleButton
      :title="i18n.global.t('editor.extensions.image.resize')"
      show-more-indicator
    >
      <template #icon>
        <MdiImageSizeSelectSmall />
      </template>
    </BubbleButton>
    <template #popper>
      <div class="flex w-56 flex-col gap-3">
        <div class="flex flex-col items-center gap-3">
          <Input
            v-model="width"
            :label="i18n.global.t('editor.common.width')"
            :tooltip="i18n.global.t('editor.common.tooltip.custom_width_input')"
          />
          <Input
            v-model="height"
            :label="i18n.global.t('editor.common.height')"
            :tooltip="
              i18n.global.t('editor.common.tooltip.custom_height_input')
            "
          />
        </div>

        <div class="flex items-center gap-1 rounded-md bg-gray-100 p-1">
          <button
            v-for="item in presetSizes"
            :key="item.width"
            v-tooltip="item.title"
            class="inline-flex flex-1 items-center justify-center rounded px-2 py-1.5 text-gray-600 transition-all hover:text-gray-900"
            :class="{
              'bg-white text-gray-900':
                item.width === width && item.height === height,
            }"
            @click="handleSetSize({ width: item.width, height: item.height })"
          >
            <component :is="item.icon" />
          </button>
        </div>
      </div>
    </template>
  </VDropdown>
</template>
