<script lang="ts" setup>
import { VPopover } from "@halo-dev/components";
import { computed, nextTick, ref, useTemplateRef, watch } from "vue";
import MingcuteDownLine from "~icons/mingcute/down-line";

export interface Option {
  label: string;
  value: string;
}
const props = defineProps<{
  containerClass?: string;
  options: Option[];
  filterSort?: (options: Option[], query: string) => number;
}>();

const value = defineModel<string>({
  default: "",
});

const emit = defineEmits<{
  (event: "select"): void;
}>();

const isFocus = ref(false);
const inputValue = ref<string>("");
const selectedOption = ref<Option | null>(null);
const inputRef = ref<HTMLInputElement | null>(null);

const displayLabel = computed(() => {
  if (selectedOption.value) {
    return selectedOption.value.label;
  }
  return value.value;
});

const filterOptions = computed(() => {
  if (!inputValue.value) {
    return props.options;
  }
  return props.options.filter((option) =>
    option.value
      .toLocaleLowerCase()
      .includes(inputValue.value.toLocaleLowerCase())
  );
});

const handleInputFocus = () => {
  isFocus.value = true;
  setTimeout(() => {
    handleScrollIntoView();
  }, 50);
};

const handleInputBlur = () => {
  if (inputValue.value) {
    value.value = inputValue.value;
    inputValue.value = "";
  }
};

const handleSelectOption = (option?: Option) => {
  if (!option) {
    return;
  }
  selectedOption.value = option;
  value.value = option.value;
  inputValue.value = "";
  isFocus.value = false;
  inputRef.value?.blur();
  emit("select");
};

watch(isFocus, async (focused) => {
  if (!focused) {
    return;
  }
  await nextTick();
  inputRef.value?.focus();
});

const selectedIndex = ref(-1);

const handleOptionKeydown = (event: KeyboardEvent) => {
  const key = event.key;
  if (key === "Escape") {
    isFocus.value = false;
    inputRef.value?.blur();
    return;
  }
  if (!["ArrowUp", "ArrowDown", "Enter"].includes(key)) {
    return;
  }

  event.preventDefault();

  const options = filterOptions.value;
  if (!options.length) {
    selectedIndex.value = -1;
    return;
  }

  if (key === "ArrowUp") {
    selectedIndex.value =
      selectedIndex.value <= 0 ? options.length - 1 : selectedIndex.value - 1;
    return;
  }

  if (key === "ArrowDown") {
    selectedIndex.value = (selectedIndex.value + 1) % options.length;
    return;
  }

  if (key === "Enter") {
    handleSelectOption(options[selectedIndex.value]);
  }
};

watch(
  [value, filterOptions],
  ([newValue, options]) => {
    selectedOption.value =
      props.options.find((option) => option.value === newValue) || null;

    const currentIndex = options.findIndex(
      (option) => option.value === newValue
    );
    selectedIndex.value =
      currentIndex >= 0
        ? currentIndex
        : inputValue.value && options.length
          ? 0
          : -1;
  },
  {
    immediate: true,
  }
);

watch(
  selectedIndex,
  () => {
    setTimeout(() => {
      handleScrollIntoView();
    });
  },
  {
    immediate: true,
  }
);

const selectRef = useTemplateRef<HTMLDivElement>("select");

const handleScrollIntoView = () => {
  const index = selectedIndex.value;
  if (index < 0 || index >= filterOptions.value.length) {
    return;
  }
  const optionElement = selectRef.value?.children.item(index);
  if (optionElement) {
    optionElement.scrollIntoView({
      behavior: "instant",
      block: "nearest",
      inline: "nearest",
    });
  }
};
</script>
<template>
  <VPopover v-model:open="isFocus" :offset="0" match-trigger-width>
    <div class="relative inline-block w-full" @keydown="handleOptionKeydown">
      <div class="h-8">
        <div
          class="select-input box-border grid size-full cursor-pointer items-center rounded-md px-3 text-sm"
          :class="{
            'bg-white': isFocus,
            'border-[1px]': isFocus,
          }"
          @click.stop="inputRef?.focus()"
        >
          <span class="absolute bottom-0 top-0">
            <input
              ref="inputRef"
              v-model="inputValue"
              class="m-0 h-full cursor-auto appearance-none border-none bg-transparent p-0 pe-0 ps-0 outline-none"
              :placeholder="isFocus ? displayLabel : ''"
              @focus="handleInputFocus"
              @blur="handleInputBlur"
            />
          </span>
          <span v-show="!isFocus" class="text-ellipsis text-sm">
            {{ displayLabel }}
          </span>
          <span class="justify-self-end" @click="inputRef?.focus()">
            <MingcuteDownLine />
          </span>
        </div>
      </div>
    </div>

    <template #content>
      <div class="bg-white">
        <div ref="select" class="select max-h-64 cursor-pointer p-1">
          <template v-if="filterOptions && filterOptions.length > 0">
            <div
              v-for="(option, index) in filterOptions"
              :key="option.value"
              :index="index"
              class="flex h-8 w-full items-center rounded-md px-3 py-1 text-base hover:bg-zinc-100"
              :class="{
                'bg-zinc-200': option.value === value,
                'bg-zinc-100': selectedIndex === index,
              }"
              @mousedown="handleSelectOption(option)"
            >
              <span class="flex-1 text-ellipsis text-sm">
                {{ option.label }}
              </span>
            </div>
          </template>
          <template v-else>
            <div
              class="flex h-8 w-full items-center rounded-md px-3 py-1 text-base"
            >
              <span class="flex-1 text-ellipsis text-sm">No options</span>
            </div>
          </template>
        </div>
      </div>
    </template>
  </VPopover>
</template>
<style lang="scss" scoped>
.select-input {
  grid-template-columns: 1fr auto;
}
</style>
