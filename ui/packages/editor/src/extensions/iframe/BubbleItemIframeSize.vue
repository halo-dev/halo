<script setup lang="ts">
import Input from "@/components/base/Input.vue";
import BubbleButton from "@/components/bubble/BubbleButton.vue";
import { i18n } from "@/locales";
import type { BubbleItemComponentProps } from "@/types";
import { VDropdown } from "@halo-dev/components";
import { computed } from "vue";
import MdiImageSizeSelectSmall from "~icons/mdi/image-size-select-small";
import MingcuteCellphoneLine from "~icons/mingcute/cellphone-line";
import MingcuteImacLine from "~icons/mingcute/imac-line";
import MingcutePadLine from "~icons/mingcute/pad-line";
import { ExtensionIframe } from "./index";

const props = defineProps<BubbleItemComponentProps>();

const width = computed({
  get: () => {
    return props.editor.getAttributes(ExtensionIframe.name).width;
  },
  set: (value: string) => {
    handleSetSize(value, height.value);
  },
});

const height = computed({
  get: () => {
    return props.editor.getAttributes(ExtensionIframe.name).height;
  },
  set: (value: string) => {
    handleSetSize(width.value, value);
  },
});

const handleSetSize = (width: string, height: string) => {
  props.editor
    .chain()
    .updateAttributes(ExtensionIframe.name, { width, height })
    .focus()
    .setNodeSelection(props.editor.state.selection.from)
    .run();
};

const presetSizes = [
  {
    width: "390px",
    height: "844px",
    icon: MingcuteCellphoneLine,
    title: i18n.global.t("editor.extensions.iframe.phone_size"),
  },
  {
    width: "834px",
    height: "1194px",
    icon: MingcutePadLine,
    title: i18n.global.t("editor.extensions.iframe.tablet_vertical_size"),
  },
  {
    width: "1194px",
    height: "834px",
    icon: MingcutePadLine,
    iconStyle: "transform: rotate(90deg)",
    title: i18n.global.t("editor.extensions.iframe.tablet_horizontal_size"),
  },
  {
    width: "100%",
    height: "834px",
    icon: MingcuteImacLine,
    title: i18n.global.t("editor.extensions.iframe.desktop_size"),
  },
];
</script>
<template>
  <VDropdown class="inline-flex" :auto-hide="true" :distance="10">
    <BubbleButton
      :title="i18n.global.t('editor.extensions.iframe.resize')"
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
            @click="handleSetSize(item.width, item.height)"
          >
            <component :is="item.icon" :style="item.iconStyle" />
          </button>
        </div>
      </div>
    </template>
  </VDropdown>
</template>
