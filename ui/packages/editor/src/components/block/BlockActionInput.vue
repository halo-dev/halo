<script lang="ts" setup>
import { vTooltip } from "floating-vue";
import { computed } from "vue";

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
  @apply block w-32 rounded-md border border-gray-300 bg-gray-50 px-2 py-1 text-sm text-gray-900 hover:bg-gray-100 focus:border-blue-500 focus:ring-blue-500;
}
</style>
