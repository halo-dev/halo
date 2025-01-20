<script lang="ts" setup>
import { Dropdown as VDropdown } from "floating-vue";
import { computed, ref, watch } from "vue";
import IconArrowDownLine from "~icons/ri/arrow-down-s-line";

export interface Option {
  label: string;
  value: string;
}
const props = defineProps<{
  container?: any;
  containerClass?: string;
  options: Option[];
  filterSort?: (options: Option[], query: string) => number;
}>();

const value = defineModel({
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
  isFocus.value = false;
  if (inputValue.value) {
    value.value = inputValue.value;
    inputValue.value = "";
  }
};

const handleSelectOption = (option: Option) => {
  selectedOption.value = option;
  value.value = option.value;
  inputValue.value = "";
  inputRef.value?.blur();
  emit("select");
};

const selectedIndex = ref(-1);

const handleOptionKeydown = (event: KeyboardEvent) => {
  const key = event.key;
  if (key === "ArrowUp") {
    selectedIndex.value =
      (selectedIndex.value - 1 + filterOptions.value.length) %
      filterOptions.value.length;
    return true;
  }

  if (key === "ArrowDown") {
    selectedIndex.value =
      (selectedIndex.value + 1) % filterOptions.value.length;
    return true;
  }

  if (key === "Enter") {
    if (selectedIndex.value === -1) {
      return true;
    }
    handleSelectOption(filterOptions.value[selectedIndex.value]);
    return true;
  }
};

watch(
  value,
  (newValue) => {
    if (newValue) {
      selectedOption.value =
        props.options.find((option) => option.value === newValue) || null;
      selectedIndex.value = props.options.findIndex(
        (option) => option.value === newValue
      );
    }
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

const handleScrollIntoView = () => {
  if (selectedIndex.value === -1) {
    return;
  }
  const optionElement = document.querySelector(
    `.select > div:nth-child(${selectedIndex.value + 1})`
  );
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
  <VDropdown
    :triggers="[]"
    :shown="isFocus"
    :auto-hide="false"
    :distance="0"
    auto-size
    :container="container || 'body'"
  >
    <div class="relative inline-block w-full" @keydown="handleOptionKeydown">
      <div class="h-8">
        <div
          class="select-input w-full h-full grid items-center text-sm rounded-md px-3 cursor-pointer box-border"
          :class="{
            'bg-white': isFocus,
            'border-[1px]': isFocus,
          }"
        >
          <span class="absolute top-0 bottom-0">
            <input
              ref="inputRef"
              v-model="inputValue"
              class="appearance-none bg-transparent h-full ps-0 pe-0 border-none outline-none m-0 p-0 cursor-auto"
              :placeholder="isFocus ? displayLabel : ''"
              @focus="handleInputFocus"
              @blur="handleInputBlur"
            />
          </span>
          <span v-show="!isFocus" class="text-ellipsis text-sm">
            {{ displayLabel }}
          </span>
          <span class="justify-self-end" @click="inputRef?.focus()">
            <IconArrowDownLine />
          </span>
        </div>
      </div>
    </div>

    <template #popper>
      <div class="bg-white">
        <div class="select max-h-64 cursor-pointer p-1">
          <template v-if="filterOptions && filterOptions.length > 0">
            <div
              v-for="(option, index) in filterOptions"
              :key="option.value"
              :index="index"
              class="w-full h-8 flex items-center rounded-md text-base px-3 py-1 hover:bg-zinc-100"
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
              class="w-full h-8 flex items-center rounded-md text-base px-3 py-1"
            >
              <span class="flex-1 text-ellipsis text-sm">No options</span>
            </div>
          </template>
        </div>
      </div>
    </template>
  </VDropdown>
</template>
<style lang="scss" scoped>
.select-input {
  grid-template-columns: 1fr auto;
}
</style>
