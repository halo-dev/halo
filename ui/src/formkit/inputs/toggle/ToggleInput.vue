<script lang="ts" setup>
import type { FormKitFrameworkContext } from "@formkit/core";
import { computed } from "vue";
import type { ToggleOption, ToggleValue } from ".";

const { context } = defineProps<{
  context: FormKitFrameworkContext;
}>();

const options = computed<ToggleOption[]>(
  () => context.node.props.options ?? []
);

const multiple = computed<boolean>(() => context.node.props.multiple ?? false);
const renderType = computed<"text" | "image" | "color">(
  () => context.node.props.renderType ?? "text"
);

const currentValue = computed<ToggleValue | ToggleValue[]>(
  () => context._value
);

function isSelected(value: ToggleValue) {
  if (multiple.value) {
    const valueArray = (currentValue.value as ToggleValue[]) || [];
    return valueArray.includes(value);
  }

  return currentValue.value === value;
}

function handleSelect(value: ToggleValue) {
  if (multiple.value) {
    const valueArray = (currentValue.value as ToggleValue[]) || [];
    if (valueArray.includes(value)) {
      context.node.input(valueArray.filter((v) => v !== value));
    } else {
      context.node.input([...valueArray, value]);
    }
    return;
  }

  context.node.input(value);
}

const defaultSize = () => {
  switch (renderType.value) {
    case "image":
      return "100px";
    case "text":
      return "100px";
    case "color":
      return "40px";
    default:
      return "100px";
  }
};

const size = computed(() => {
  const size = context.node.props.size;
  if (!size) {
    return defaultSize();
  }

  const sizeValue = Number(size);
  if (Number.isNaN(sizeValue)) {
    return defaultSize();
  }
  return `${sizeValue}px`;
});

const gap = computed(() => {
  const gap = context.node.props.gap;
  if (!gap) {
    return "12px";
  }

  const gapValue = Number(gap);
  if (Number.isNaN(gapValue)) {
    return "12px";
  }
  return `${gap}px`;
});
</script>
<template>
  <div class="flex flex-wrap" :style="{ gap: gap }">
    <div
      v-for="option in options"
      :key="String(option.value)"
      class="group flex cursor-pointer flex-col items-center justify-center gap-2"
      @click="handleSelect(option.value)"
    >
      <div
        class="border-2 p-0.5 transition-all duration-200"
        :class="[
          isSelected(option.value)
            ? 'border-primary shadow-md'
            : 'border-transparent group-hover:border-gray-300 group-hover:shadow-sm',
          renderType === 'color' ? 'rounded-full' : 'rounded-lg',
        ]"
      >
        <img
          v-if="renderType === 'image'"
          :src="option.render"
          :alt="option.label"
          :style="{ height: size }"
          class="rounded-md object-cover"
        />
        <div
          v-if="renderType === 'color'"
          :style="{ backgroundColor: option.render, height: size, width: size }"
          class="rounded-full"
        ></div>
        <span
          v-if="renderType === 'text'"
          class="flex items-center justify-center rounded-md p-1 text-base font-medium"
          :class="
            isSelected(option.value)
              ? 'text-primary'
              : 'text-gray-600 group-hover:text-gray-900'
          "
        >
          {{ option.render || option.label || option.value }}
        </span>
      </div>
      <label
        v-if="option.label && renderType !== 'text'"
        class="cursor-pointer select-none text-sm transition-colors duration-200"
        :class="
          isSelected(option.value)
            ? 'font-semibold text-gray-900'
            : 'text-gray-500 group-hover:text-gray-700'
        "
      >
        {{ option.label }}
      </label>
    </div>
  </div>
</template>
