<script lang="ts" setup>
import { Sketch, type Payload } from "@ckpack/vue-color";
import { TinyColor } from "@ctrl/tinycolor";
import type { FormKitFrameworkContext } from "@formkit/core";
import { IconClose, VButton, VDropdown } from "@halo-dev/components";
import Color from "colorjs.io";
import { computed, useTemplateRef, type PropType } from "vue";
import RiCodeSSlashLine from "~icons/ri/code-s-slash-line";
import type { ColorFormat } from "./types";

const props = defineProps({
  context: {
    type: Object as PropType<FormKitFrameworkContext>,
    required: true,
  },
});

const format = computed(() => props.context.format as ColorFormat);

function onColorChange(color: Payload) {
  props.context.node.input(formatPayload(color));
}

function formatPayload(color: Payload) {
  switch (format.value) {
    case "rgb":
      return new TinyColor(color.rgba).toRgbString();
    case "hex8":
      return color.hex8;
    case "hsl":
      return new TinyColor(color.hsl).toHslString();
    default:
      return color.hex;
  }
}

function formatColorByUnpredictableValue(value: string) {
  const color = new TinyColor(value);
  switch (format.value) {
    case "rgb":
      return color.toRgbString();
    case "hex8":
      return color.toHex8();
    case "hsl":
      return color.toHslString();
    default:
      return color.toHex();
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

const editFormDropdown =
  useTemplateRef<InstanceType<typeof VDropdown>>("editFormDropdown");

function onEditFormSubmit({ value }: { value: string }) {
  props.context.node.input(formatColorByUnpredictableValue(value));
  editFormDropdown.value?.hide();
}
</script>

<template>
  <div class="group/color-input inline-flex items-center gap-2">
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
        <span class="text-sm">
          {{ context._value || $t("core.formkit.color.placeholder") }}
        </span>
      </button>
      <template #popper>
        <Sketch
          :model-value="context._value || '#000'"
          @update:model-value="onColorChange"
        />
      </template>
    </VDropdown>

    <div class="inline-flex items-center gap-1.5">
      <VDropdown ref="editFormDropdown" class="inline-flex">
        <template #default="{ shown }">
          <button
            v-tooltip="$t('core.formkit.color.operations.edit')"
            type="button"
            :aria-label="$t('core.formkit.color.operations.edit')"
            class="text-gray-500 opacity-0 transition-all hover:text-gray-900 group-hover/color-input:opacity-100"
            :class="{ '!text-gray-900 !opacity-100': shown }"
          >
            <RiCodeSSlashLine />
          </button>
        </template>
        <template #popper>
          <div class="w-96">
            <FormKit
              id="color-edit-form"
              type="form"
              ignore
              name="color-edit-form"
              @submit="onEditFormSubmit"
            >
              <FormKit type="text" :model-value="context._value" name="value" />
            </FormKit>
            <div class="mt-4">
              <VButton
                type="secondary"
                @click="$formkit.submit('color-edit-form')"
              >
                {{ $t("core.common.buttons.save") }}
              </VButton>
            </div>
          </div>
        </template>
      </VDropdown>
      <button
        v-if="context._value"
        v-tooltip="$t('core.common.buttons.delete')"
        type="button"
        :aria-label="$t('core.common.buttons.delete')"
        class="text-gray-500 opacity-0 transition-all hover:text-gray-900 group-hover/color-input:opacity-100"
        @click="context.node.input(undefined)"
      >
        <IconClose />
      </button>
    </div>
  </div>
</template>
