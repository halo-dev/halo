<script setup lang="ts">
import { computed, ref } from "vue";

import MultipleOverflow from "./MultipleOverflow.vue";
import MultipleOverflowItem from "./MultipleOverflowItem.vue";
import MultipleSelect from "./MultipleSelect.vue";
import MultipleSelectSearchInput from "./MultipleSelectSearchInput.vue";

const props = withDefaults(
  defineProps<{
    sortable: boolean;
    placeholder?: string;
    selectedOptions: Array<{
      label: string;
      value: string;
    }>;
    searchable: boolean;
  }>(),
  {
    placeholder: "Select...",
  }
);

const emit = defineEmits<{
  (event: "search", value: string): void;
  (event: "blur", value: FocusEvent): void;
  (
    event: "deleteItem",
    index: number,
    option?: {
      label: string;
      value: string;
    }
  ): void;
  (event: "sort", value: Array<{ label: string; value: string }>): void;
}>();

const inputRef = ref();
const inputValue = ref("");
const isCombinationInput = ref(false);

const handleSearch = (value: string, event?: Event) => {
  inputValue.value = value;
  if (event && event instanceof InputEvent) {
    isCombinationInput.value = event.isComposing;
  } else {
    isCombinationInput.value = false;
  }
  emit("search", value);
};

const handleSearchInputBackspace = () => {
  // If the input is in composition mode, do not delete the selected item
  if (isCombinationInput.value) {
    return;
  }
  if (!inputValue.value && props.selectedOptions.length > 0) {
    emit("deleteItem", props.selectedOptions.length - 1);
  }
};

const showPlaceholder = computed(() => {
  return (
    !props.selectedOptions.length &&
    !inputValue.value &&
    !isCombinationInput.value
  );
});

const handleFocusout = (event: FocusEvent) => {
  if (event.relatedTarget) {
    const target = event.relatedTarget as HTMLElement;
    if (target && target.closest(".select-item")) {
      event.stopPropagation();
    }
  }
};
</script>
<template>
  <MultipleOverflow
    class="cursor-text"
    :class="{
      '!cursor-pointer': !searchable,
    }"
  >
    <MultipleSelect
      :selected-options="selectedOptions"
      :sortable="sortable"
      @delete-item="(index, option) => emit('deleteItem', index, option)"
      @sort="(options) => emit('sort', options)"
    />
    <template #input>
      <MultipleOverflowItem>
        <MultipleSelectSearchInput
          ref="inputRef"
          :searchable="searchable"
          @search="handleSearch"
          @keydown.backspace="handleSearchInputBackspace"
          @focusout="handleFocusout"
        ></MultipleSelectSearchInput>

        <span
          v-if="showPlaceholder"
          class="pointer-events-none absolute inset-y-0 left-0 w-full truncate text-sm"
        >
          <span class="w-full text-gray-400">
            {{ placeholder }}
          </span>
        </span>
      </MultipleOverflowItem>
    </template>
  </MultipleOverflow>
</template>
