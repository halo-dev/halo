<script lang="ts" setup>
import { VLoading } from "@halo-dev/components";
import { vScroll } from "@vueuse/components";
import { useEventListener, type UseScrollReturn } from "@vueuse/core";
import { computed, ref, watch } from "vue";
import SelectOptionItem from "./SelectOptionItem.vue";
import { isFalse } from "./isFalse";

const props = defineProps<{
  options: Array<Record<string, unknown> & { label: string; value: string }>;
  selectedOptions?: Array<{
    label: string;
    value: string;
  }>;
  multiple: boolean;
  maxCount: number;
  nextLoading: boolean;
}>();

const emit = defineEmits<{
  (
    event: "selected",
    value: Record<string, unknown> & { label: string; value: string }
  ): void;
  (event: "loadMore"): void;
}>();

const selectedIndex = ref<number>(0);
const selectOptionRef = ref<HTMLElement>();
const oldEvent = ref<MouseEvent | undefined>();
const isCursorHidden = ref(false);

const selectedValues = computed(() =>
  props.selectedOptions?.map((option) => option.value)
);

const getSelectedIndex = () => {
  if (props.multiple) {
    return 0;
  }
  if (selectedValues.value && selectedValues.value.length > 0) {
    const value = selectedValues.value[0];
    const index = props.options.findIndex((option) => option.value === value);
    return index === -1 ? 0 : index;
  }

  return 0;
};

const handleKeydown = (event: KeyboardEvent) => {
  if (selectOptionRef.value) {
    isCursorHidden.value = true;
  }
  const key = event.key;
  if (key === "ArrowUp") {
    selectedIndex.value =
      selectedIndex.value - 1 < 0
        ? props.options.length - 1
        : selectedIndex.value - 1;
    event.preventDefault();
  }

  if (key === "ArrowDown") {
    selectedIndex.value =
      selectedIndex.value + 1 >= props.options.length
        ? 0
        : selectedIndex.value + 1;
    event.preventDefault();
  }

  if (key === "Enter") {
    handleSelected(selectedIndex.value);
    event.preventDefault();
  }
};

useEventListener(document, "keydown", handleKeydown);

const handleSelected = (index: number) => {
  const option = props.options[index];
  if (reachMaximumLimit.value) {
    const index = props.selectedOptions?.findIndex(
      (selected) => selected.value === option.value
    );
    if (index === -1) {
      return;
    }
  }
  selectedIndex.value = index;
  if (option && !isDisabled(option)) {
    emit("selected", option);
  }
};

const handleScrollIntoView = () => {
  if (selectedIndex.value === -1) {
    return;
  }
  const optionElement = document.querySelector(
    `#select-option > div:nth-child(${selectedIndex.value + 1})`
  );
  if (optionElement) {
    optionElement.scrollIntoView({
      behavior: "instant",
      block: "nearest",
      inline: "nearest",
    });
  }
};

const reachMaximumLimit = computed(() => {
  if (!props.multiple || isNaN(props.maxCount)) {
    return false;
  }
  if (props.selectedOptions && props.selectedOptions.length >= props.maxCount) {
    return true;
  }
  return false;
});

const isDisabled = (
  option: Record<string, unknown> & { label: string; value: string }
) => {
  const attrs = option.attrs as Record<string, unknown>;
  return (
    (reachMaximumLimit.value &&
      selectedValues.value &&
      !selectedValues.value.includes(option.value)) ||
    !isFalse(attrs?.disabled as string | boolean | undefined)
  );
};

const handleOptionScroll = (state: UseScrollReturn) => {
  if (selectOptionRef.value) {
    const scrollHeight = (selectOptionRef.value as HTMLElement).scrollHeight;
    const clientHeight = (selectOptionRef.value as HTMLElement).clientHeight;
    const scrollPercentage =
      (state.y.value / (scrollHeight - clientHeight)) * 100;
    if (scrollPercentage > 50) {
      emit("loadMore");
    }
  }
};

watch(
  props.options,
  () => {
    selectedIndex.value = getSelectedIndex();
  },
  {
    immediate: true,
  }
);

watch(
  () => selectedIndex.value,
  () => {
    handleScrollIntoView();
  }
);

/**
 * check if cursor is changed
 *
 * @param newEvent
 * @param oldEvent
 */
const isCursorChanged = (
  newEvent: MouseEvent,
  oldEvent: MouseEvent | undefined
) => {
  if (!oldEvent) {
    return true;
  }
  return (
    newEvent.screenX !== oldEvent.screenX ||
    newEvent.screenY !== oldEvent.screenY
  );
};

const handleMouseover = (event: MouseEvent) => {
  if (isCursorHidden.value) {
    if (!oldEvent.value) {
      oldEvent.value = event;
    }

    if (isCursorChanged(event, oldEvent.value)) {
      isCursorHidden.value = false;
      oldEvent.value = undefined;
    }
    return;
  }
  const target = event.target as HTMLElement;
  if (
    target.classList.contains("select-option-item") &&
    target instanceof HTMLElement
  ) {
    const parentElement = target.parentElement as HTMLElement;
    const index = Array.from(parentElement.children).indexOf(target);
    selectedIndex.value = index;
  }
};
</script>

<template>
  <div
    id="select-option"
    ref="selectOptionRef"
    v-scroll="[handleOptionScroll, { throttle: 10 }]"
    class="select max-h-64 overflow-y-auto p-1.5"
    :class="[isCursorHidden ? 'cursor-none' : 'cursor-pointer']"
    role="list"
    tabindex="-1"
    @keydown="handleKeydown"
    @mouseover="handleMouseover"
  >
    <template v-for="(option, index) in options" :key="option.value">
      <SelectOptionItem
        class="select-option-item"
        :option="option"
        :class="{
          'bg-zinc-100': !isDisabled(option) && selectedIndex === index,
          'selected !bg-zinc-200/60':
            selectedValues && selectedValues.includes(option.value),
          'cursor-not-allowed opacity-25': isDisabled(option),
        }"
        @mousedown.stop="handleSelected(index)"
      >
      </SelectOptionItem>
    </template>
    <div v-if="nextLoading">
      <VLoading></VLoading>
    </div>
  </div>
</template>
<style lang="scss">
.select-option-item:has(+ .select-option-item:not(.selected))
  + .select-option-item.selected {
  border-end-start-radius: 0;
  border-end-end-radius: 0;
}

.select-option-item.selected + .select-option-item.selected {
  border-start-start-radius: 0;
  border-start-end-radius: 0;
}

.select-option-item.selected:has(+ .select-option-item.selected) {
  border-end-start-radius: 0;
  border-end-end-radius: 0;
}

.select-option-item.selected + .select-option-item:not(.selected) {
  border-start-start-radius: 0;
  border-start-end-radius: 0;
}
</style>
