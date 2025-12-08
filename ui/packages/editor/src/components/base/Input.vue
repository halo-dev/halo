<script lang="ts" setup>
import { nextTick, onMounted, ref } from "vue";
import MingcuteInformationLine from "~icons/mingcute/information-line";

const modelValue = defineModel<string | number | undefined>({
  default: "",
});

const props = withDefaults(
  defineProps<{
    type?: string;
    label?: string;
    help?: boolean;
    placeholder?: string;
    tooltip?: string;
    autoFocus?: boolean;
  }>(),
  {
    type: "text",
    label: undefined,
    help: undefined,
    placeholder: undefined,
    tooltip: undefined,
    autoFocus: false,
  }
);

const emit = defineEmits<{
  (event: "focus"): void;
}>();

const inputRef = ref<HTMLInputElement>();

onMounted(() => {
  if (props.autoFocus) {
    nextTick(() => {
      inputRef.value?.focus();
    });
  }
});
</script>

<template>
  <div class="group relative h-11 w-full" :class="{ 'mt-2': !!label }">
    <label
      v-if="label"
      class="absolute -top-2 left-2 inline-block rounded-md bg-white px-1 text-xs text-gray-500 group-focus-within:text-gray-900"
    >
      {{ label }}
    </label>
    <input
      ref="inputRef"
      v-model.lazy.trim="modelValue"
      :type="type"
      class="block size-full rounded-md bg-white px-3 text-sm text-gray-900 ring-1 ring-gray-100 transition-all placeholder:text-gray-400 focus:!ring-1 focus:!ring-primary"
      :placeholder="placeholder"
      @focus="emit('focus')"
    />
    <MingcuteInformationLine
      v-if="tooltip"
      v-tooltip="tooltip"
      class="absolute right-2 top-1/2 size-4 -translate-y-1/2 text-gray-500 transition-colors hover:text-primary"
    />
    <span v-if="help" class="line-clamp-1 text-xs text-gray-500">
      {{ help }}
    </span>
  </div>
</template>
