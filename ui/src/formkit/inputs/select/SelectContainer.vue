<script lang="ts" setup>
import {
  IconArrowDownLine,
  IconCloseCircle,
  VPopover,
} from "@halo-dev/components";
import { useEventListener } from "@vueuse/core";
import { computed, nextTick, onMounted, ref, watch, type PropType } from "vue";
import MultipleSelectSelector from "./MultipleSelectSelector.vue";
import SelectDropdownContainer from "./SelectDropdownContainer.vue";
import SelectSelector from "./SelectSelector.vue";
import type { SelectOption } from "./types";

const props = defineProps({
  multiple: {
    type: Boolean,
    default: false,
  },
  allowCreate: {
    type: Boolean,
    default: false,
  },
  maxCount: {
    type: Number,
    default: NaN,
  },
  sortable: {
    type: Boolean,
    default: true,
  },
  placeholder: {
    type: String,
    default: "",
  },
  options: {
    type: Array as PropType<SelectOption[]>,
    required: false,
    default: () => [],
  },
  loading: {
    type: Boolean,
    default: false,
  },
  nextLoading: {
    type: Boolean,
    default: false,
  },
  selected: {
    type: Array as PropType<SelectOption[]>,
    required: false,
    default: () => [],
  },
  remote: {
    type: Boolean,
    default: false,
  },
  clearable: {
    type: Boolean,
    default: false,
  },
  searchable: {
    type: Boolean,
    default: false,
  },
  autoSelect: {
    type: Boolean,
    default: true,
  },
});

const emit = defineEmits<{
  (event: "update", value: SelectOption[]): void;
  (event: "search", value: string, e?: Event): void;
  (event: "loadMore"): void;
}>();

const selectContainerRef = ref<HTMLElement>();
const inputRef = ref<HTMLInputElement | null>();
const searchKeyword = ref<string>("");
const isDropdownVisible = ref<boolean>(false);
const selectedOptions = computed({
  get: () => [...props.selected],
  set: (value) => {
    emit("update", value);
  },
});
const hasClearable = computed(
  () =>
    props.clearable && (selectedOptions.value.length > 0 || searchKeyword.value)
);

const getInputHTMLRef = () => {
  nextTick(() => {
    inputRef.value = selectContainerRef.value?.querySelector("input");
  });
};

onMounted(() => {
  getInputHTMLRef();
});

// The non-modal popover moves focus to its content when it mounts, so return
// the focus to the search input after the panel has rendered.
watch(isDropdownVisible, async (visible) => {
  if (!visible) {
    return;
  }
  await nextTick();
  await nextTick();
  inputRef.value?.focus();
});

const handleOptionSelect = (option: SelectOption) => {
  if (!props.multiple) {
    selectedOptions.value = [option];
    isDropdownVisible.value = false;
  } else {
    const index = selectedOptions.value.findIndex(
      (selected) => selected.value === option.value
    );
    if (index === -1) {
      selectedOptions.value.push(option);
    } else {
      selectedOptions.value.splice(index, 1);
    }
    selectedOptions.value = [...selectedOptions.value];
  }
  clearInputValue();
  // Clicking an option moves the focus away from the search input, so put it
  // back once the option has been selected.
  nextTick(() => inputRef.value?.focus());
};

const handleDeleteSelectItem = (index: number) => {
  selectedOptions.value.splice(index, 1);
  selectedOptions.value = [...selectedOptions.value];
};

const handleSearch = (value: string, event?: Event) => {
  searchKeyword.value = value;
  if (!isDropdownVisible.value && !!value) {
    isDropdownVisible.value = true;
  }
  emit("search", value, event);
};

const clearInputValue = () => {
  if (!inputRef.value) {
    return;
  }
  inputRef.value.value = "";
  // Manually trigger input event
  const event = new Event("input", { bubbles: true });
  inputRef.value.dispatchEvent(event);
};

let blockEscapeKeyup = false;

// Escape closes the panel through the popover itself. The surrounding modal
// also listens for Escape on keyup, so swallow that keyup once the panel has
// handled the key.
useEventListener(
  window,
  "keydown",
  (event: KeyboardEvent) => {
    if (event.key === "Escape" && isDropdownVisible.value) {
      blockEscapeKeyup = true;
    }
  },
  { capture: true }
);

useEventListener(
  window,
  "keyup",
  (event: KeyboardEvent) => {
    if (event.key !== "Escape" || !blockEscapeKeyup) {
      return;
    }
    blockEscapeKeyup = false;
    event.stopPropagation();
  },
  { capture: true }
);

const handleKeyDown = (event: KeyboardEvent) => {
  const key = event.key;
  if (key === "Escape") {
    if (isDropdownVisible.value) {
      clearInputValue();
    }
    return;
  }

  if (key === "Enter" || key.startsWith("Arrow")) {
    if (!isDropdownVisible.value) {
      isDropdownVisible.value = true;
      event.stopPropagation();
    }
  }
};

const clearAllSelectedOptions = () => {
  if (!hasClearable.value) {
    return;
  }

  selectedOptions.value = [];
  clearInputValue();
};

const handleSortSelectedOptions = (options: SelectOption[]) => {
  emit("update", options);
};
</script>

<template>
  <VPopover
    v-model:open="isDropdownVisible"
    :arrow="false"
    match-trigger-width
    :offset="10"
    content-class="!p-0 overflow-hidden"
  >
    <div
      ref="selectContainerRef"
      class="select-container relative w-full items-center"
    >
      <div class="relative h-full items-center rounded-md pe-7 ps-3 text-sm">
        <component
          :is="multiple ? MultipleSelectSelector : SelectSelector"
          v-bind="{
            placeholder: placeholder,
            isDropdownVisible,
            selectedOptions,
            sortable,
            searchable,
          }"
          @search="handleSearch"
          @delete-item="handleDeleteSelectItem"
          @keydown="handleKeyDown"
          @sort="handleSortSelectedOptions"
        ></component>
      </div>
      <span
        class="absolute inset-y-0 right-2.5 flex items-center text-gray-500 hover:text-gray-700"
      >
        <IconArrowDownLine
          class="pointer-events-none"
          :class="{
            'group-hover/select:hidden': hasClearable,
          }"
        />
        <IconCloseCircle
          class="hidden cursor-pointer"
          :class="{
            'group-hover/select:block': hasClearable,
          }"
          @click.stop="clearAllSelectedOptions"
        />
      </span>
    </div>
    <template #content>
      <SelectDropdownContainer
        :loading="loading"
        :next-loading="nextLoading"
        :options="options"
        :remote="remote"
        :keyword="searchKeyword"
        :multiple="multiple || false"
        :selected-options="selectedOptions"
        :allow-create="allowCreate"
        :max-count="maxCount"
        @selected="handleOptionSelect"
        @load-more="emit('loadMore')"
      />
    </template>
  </VPopover>
</template>
