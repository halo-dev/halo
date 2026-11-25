<script lang="ts" setup>
import type { Payload } from "@ckpack/vue-color";
import { Sketch } from "@ckpack/vue-color";
import { VPopover } from "@halo-dev/components";
import tailwindcssColors from "tailwindcss/colors";
import MingcutePaletteLine from "~icons/mingcute/palette-line";
import MingcuteRightLine from "~icons/mingcute/right-line";
import { i18n } from "@/locales";

interface Color {
  color: string;
  name: string;
}

const modelValue = defineModel<string | undefined>({ default: undefined });
const shown = defineModel<boolean>("shown", { default: false });

function getColors(): Color[] {
  const result: Color[] = [];

  const colors: { [key: string]: { [key: string]: string } } = Object.keys(
    tailwindcssColors
  ).reduce((acc, key) => {
    if (
      [
        "gray",
        "red",
        "orange",
        "yellow",
        "green",
        "blue",
        "purple",
        "pink",
      ].includes(key)
    ) {
      // @ts-ignore
      acc[key] = tailwindcssColors[key];
    }
    return acc;
  }, {});

  for (const color in colors) {
    const colorShades = colors[color];
    const colorShadesArr = Object.entries(colorShades);

    const sortedShades = colorShadesArr
      .filter(([shade]) => parseInt(shade) >= 100 && parseInt(shade) <= 900)
      .sort((a, b) => parseInt(b[0]) - parseInt(a[0]));

    const formattedShades = sortedShades.map(([shade, value]) => ({
      color: value,
      name: `${color} ${shade}`,
    }));

    result.push(...formattedShades);
  }

  return result;
}

function handleSetColor(color: string) {
  modelValue.value = color;
}

function onColorChange(color: Payload) {
  handleSetColor(color.hex);
}
</script>

<template>
  <VPopover v-model:open="shown" :offset="4" :close-on-focus-outside="false">
    <slot />
    <template #content>
      <slot name="prefix" />
      <div class="grid grid-cols-9 gap-1.5 p-2 pt-1">
        <div
          v-for="item in getColors()"
          :key="item.color"
          :style="{ backgroundColor: item.color }"
          class="size-5 cursor-pointer rounded-sm ring-gray-300 ring-offset-1 hover:ring-1"
          :title="item.name"
          @click="handleSetColor(item.color)"
        ></div>
      </div>

      <VPopover
        placement="right"
        :offset="4"
        content-class="!p-0"
        :close-on-focus-outside="false"
      >
        <div class="p-1">
          <div
            class="flex cursor-pointer items-center justify-between rounded p-1 text-xs text-gray-600 hover:bg-gray-100 hover:text-gray-900"
          >
            <div class="inline-flex items-center gap-2">
              <MingcutePaletteLine />
              <span>
                {{ i18n.global.t("editor.components.color_picker.more_color") }}
              </span>
            </div>
            <div>
              <MingcuteRightLine />
            </div>
          </div>
        </div>
        <template #content>
          <Sketch model-value="#000" @update:model-value="onColorChange" />
        </template>
      </VPopover>
    </template>
  </VPopover>
</template>
