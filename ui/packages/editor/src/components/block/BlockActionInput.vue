<script lang="ts" setup>
import { computed } from "vue";
import { vTooltip } from "floating-vue";

const props = withDefaults(
  defineProps<{
    tooltip?: string;
    modelValue?: string;
  }>(),
  {
    modelValue: "",
    tooltip: undefined,
  }
);

const emit = defineEmits<{
  (emit: "update:modelValue", value: string): void;
}>();

const value = computed({
  get: () => props.modelValue || "",
  set: (value: string) => emit("update:modelValue", value),
});
</script>

<template>
  <input
    v-model.lazy.trim="value"
    v-tooltip="tooltip"
    class="editor-block__actions-input"
  />
</template>

<style lang="scss">
.editor-block__actions-input {
  @apply bg-gray-50 rounded-md hover:bg-gray-100 block px-2 w-32 py-1 text-sm text-gray-900 border border-gray-300  focus:ring-blue-500 focus:border-blue-500;
}
</style>
