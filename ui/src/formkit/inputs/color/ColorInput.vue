<script lang="ts" setup>
import { Sketch, type Payload } from "@ckpack/vue-color";
import type { FormKitFrameworkContext } from "@formkit/core";
import { VDropdown } from "@halo-dev/components";
import Color from "colorjs.io";
import { computed, type PropType } from "vue";
import type { ColorFormat } from "./types";

const props = defineProps({
  context: {
    type: Object as PropType<FormKitFrameworkContext>,
    required: true,
  },
});

const format = computed(() => props.context.format as ColorFormat);

function onColorChange(color: Payload) {
  props.context.node.input(formatColor(color));
}

function formatColor(color: Payload) {
  switch (format.value) {
    case "rgb":
      return `rgb(${color.rgba.r} ${color.rgba.g} ${color.rgba.b} / ${color.rgba.a})`;
    case "hex8":
      return color.hex8;
    default:
      return color.hex;
  }
}

const isHighContrast = computed(() => {
  const color = props.context._value;
  if (!color) {
    return false;
  }
  try {
    const onWhite = Math.abs(Color.contrast(color, "white", "APCA"));
    const onBlack = Math.abs(Color.contrast(color, "black", "APCA"));
    return onWhite > onBlack;
  } catch {
    return false; // Default to low contrast on error
  }
});
</script>

<template>
  <VDropdown class="inline-flex" popper-class="[&_.v-popper\_\_inner]:!p-0">
    <button
      type="button"
      aria-label="Choose color"
      class="inline-flex h-8 items-center justify-center rounded-lg bg-white px-2 transition-all hover:opacity-80 hover:shadow active:opacity-70"
      :style="{
        backgroundColor: context._value,
      }"
      :class="[
        { 'text-white': isHighContrast },
        { 'text-gray-900 ring-1 ring-gray-200': !isHighContrast },
      ]"
    >
      <span class="text-sm">{{ context._value }}</span>
    </button>
    <template #popper>
      <Sketch
        :model-value="context._value"
        @update:model-value="onColorChange"
      />
    </template>
  </VDropdown>
</template>
