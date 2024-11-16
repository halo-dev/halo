<script lang="ts" setup>
import { IconArrowDownLine, IconCloseCircle } from "@halo-dev/components";
import {
  computed,
  defineEmits,
  nextTick,
  onMounted,
  onUnmounted,
  ref,
  type PropType,
} from "vue";

import { Dropdown } from "floating-vue";
import MultipleSelectSelector from "./MultipleSelectSelector.vue";
import SelectDropdownContainer from "./SelectDropdownContainer.vue";
import SelectSelector from "./SelectSelector.vue";

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
    type: Array as PropType<
      Array<
        Record<string, unknown> & {
          label: string;
          value: string;
        }
      >
    >,
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
    type: Array as PropType<
      Array<{
        label: string;
        value: string;
      }>
    >,
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
  (event: "update", value: Array<{ label: string; value: string }>): void;
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
  if (selectContainerRef.value) {
    observer.observe(selectContainerRef.value);
  }
});

onUnmounted(() => {
  observer.disconnect();
});

// resolve the issue of the dropdown position when the container size changes
// https://github.com/Akryum/floating-vue/issues/977#issuecomment-1651898070
const observer = new ResizeObserver(() => {
  window.dispatchEvent(new Event("resize"));
});

const handleOptionSelect = (
  option: Record<string, unknown> & {
    label: string;
    value: string;
  }
) => {
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
};

/**
 * When the search box loses focus due to option selection, check if the focus is on an option, and keep the focus if so.
 *
 * @param event FocusEvent
 */
const handleSearchFocusout = (event: FocusEvent) => {
  const target = event.relatedTarget as HTMLElement;
  if (props.multiple && inputRef.value) {
    if (
      target &&
      (target.closest("#select-option") || target.closest(".select-container"))
    ) {
      inputRef.value.focus();
      return;
    }
  }
  clearInputValue();
  isDropdownVisible.value = false;
};

const handleDeleteSelectItem = (index: number) => {
  selectedOptions.value.splice(index, 1);
  selectedOptions.value = [...selectedOptions.value];
};

const handleSearchClick = () => {
  if (!isDropdownVisible.value && inputRef.value) {
    inputRef.value.focus();
    isDropdownVisible.value = true;
    return;
  }
  if (isDropdownVisible.value) {
    isDropdownVisible.value = false;
    return;
  }
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

const handleKeyDown = (event: KeyboardEvent) => {
  const key = event.key;
  if (key === "Enter" || key.startsWith("Arrow")) {
    if (!isDropdownVisible.value) {
      isDropdownVisible.value = true;
      event.stopPropagation();
    }
  }

  if (key === "Escape" && isDropdownVisible.value) {
    clearInputValue();
    isDropdownVisible.value = false;
  }
};

const clearAllSelectedOptions = () => {
  if (!hasClearable.value) {
    return;
  }

  selectedOptions.value = [];
  clearInputValue();
};

const handleSortSelectedOptions = (
  options: Array<{ label: string; value: string }>
) => {
  emit("update", options);
};
</script>

<template>
  <Dropdown
    :triggers="[]"
    :shown="isDropdownVisible"
    auto-size
    :auto-hide="false"
    :distance="10"
    container="body"
    class="w-full"
    popper-class="select-container-dropdown"
  >
    <div
      ref="selectContainerRef"
      tabindex="-1"
      class="select-container relative items-center"
      @focusout.stop="handleSearchFocusout"
      @click.stop="handleSearchClick"
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
    <template #popper>
      <SelectDropdownContainer
        v-if="isDropdownVisible"
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
  </Dropdown>
</template>
<style lang="scss">
.select-container-dropdown {
  .v-popper__arrow-container {
    display: none;
  }
}
</style>
