<script lang="ts" setup>
import { nextTick, ref, watch } from "vue";

defineProps<{
  searchable: boolean;
}>();

const emit = defineEmits<{
  (event: "search", value: string, e?: Event): void;
  (event: "enter", value: string): void;
}>();

const inputHTMLRef = ref<HTMLInputElement | null>(null);
const inputMirrorRef = ref<HTMLSpanElement | null>(null);
const searchInputContainerRef = ref<HTMLSpanElement | null>(null);
const inputValue = ref("");
const inputMirrorValue = ref("");

watch(
  inputMirrorValue,
  () => {
    nextTick(() => {
      if (searchInputContainerRef.value && inputMirrorRef.value) {
        searchInputContainerRef.value.style.width =
          inputMirrorRef.value.offsetWidth + 10 + "px";
      }
    });
  },
  {
    immediate: true,
  }
);

const handleInputSearch = (event: Event) => {
  if (event instanceof InputEvent) {
    const target = event.target as HTMLInputElement;
    inputMirrorValue.value = target.value;
  }
  emit("search", inputValue.value, event);
};

defineExpose({
  inputHTML: inputHTMLRef,
});
</script>

<template>
  <div class="relative max-w-full cursor-text">
    <span ref="searchInputContainerRef" class="relative flex max-w-full">
      <input
        ref="inputHTMLRef"
        v-model="inputValue"
        :readonly="!searchable"
        type="text"
        autocomplete="off"
        class="m-0 h-full w-full cursor-auto !appearance-none border-none bg-transparent p-0 pe-0 ps-0 text-base outline-none"
        @input="handleInputSearch"
      />
      <span
        ref="inputMirrorRef"
        class="invisible absolute end-auto start-0 m-0 whitespace-pre border-none p-0 text-base outline-none"
        aria-hidden="true"
      >
        {{ inputMirrorValue }}
        &nbsp;
      </span>
    </span>
  </div>
</template>
