<script lang="ts" setup>
import { nextTick, ref, watch } from "vue";

const modelValue = defineModel<string>({
  default: "",
});

const props = withDefaults(
  defineProps<{
    placeholder?: string;
    maxLength: number;
    confirmText: string;
    cancelText: string;
    autoFocus?: boolean;
  }>(),
  {
    placeholder: "",
    autoFocus: true,
  }
);

const emit = defineEmits<{
  (event: "confirm"): void;
  (event: "cancel"): void;
}>();

const inputRef = ref<HTMLInputElement | null>(null);

watch(
  () => modelValue.value,
  (newValue) => {
    if (typeof newValue === "string" && newValue.length > props.maxLength) {
      modelValue.value = newValue.slice(0, props.maxLength);
    }
  }
);

async function focus() {
  if (!props.autoFocus) {
    return;
  }
  await nextTick();
  inputRef.value?.focus();
}

watch(
  () => props.autoFocus,
  () => {
    focus();
  },
  { immediate: true }
);
</script>

<template>
  <div class="flex items-center gap-2">
    <input
      ref="inputRef"
      v-model.trim="modelValue"
      class="h-9 w-full rounded-md bg-white px-3 text-sm text-gray-900 ring-1 ring-gray-100 focus:!ring-1 focus:!ring-primary"
      :placeholder="placeholder"
      :maxlength="maxLength"
      @keydown.enter.prevent="emit('confirm')"
      @keydown.esc.prevent="emit('cancel')"
    />
    <button
      class="h-9 shrink-0 rounded-md bg-primary px-3 text-sm text-white hover:bg-primary/90"
      type="button"
      @click="emit('confirm')"
    >
      {{ confirmText }}
    </button>
    <button
      class="h-9 shrink-0 rounded-md bg-gray-100 px-3 text-sm text-gray-900 hover:bg-gray-200"
      type="button"
      @click="emit('cancel')"
    >
      {{ cancelText }}
    </button>
  </div>
</template>
