<script setup lang="ts">
import { computed, ref } from "vue";
import SelectSearchInput from "./SelectSearchInput.vue";

const props = defineProps<{
  placeholder?: string;
  selectedOptions: Array<{
    label: string;
    value: string;
  }>;
  isDropdownVisible: boolean;
  searchable: boolean;
}>();

const emit = defineEmits<{
  (event: "search", value: string, e?: Event): void;
}>();

const selectLabel = computed(() => {
  if (props.selectedOptions && props.selectedOptions.length > 0) {
    return props.selectedOptions[0].label.toString();
  }
  return undefined;
});

const inputValue = ref("");
const isCombinationInput = ref(false);
const handleSearch = (value: string, event?: Event) => {
  inputValue.value = value;
  if (event && event instanceof InputEvent) {
    isCombinationInput.value = event.isComposing;
  } else {
    isCombinationInput.value = false;
  }
  emit("search", value, event);
};

const showPlaceholder = computed(() => {
  return !inputValue.value && !isCombinationInput.value;
});
</script>

<template>
  <SelectSearchInput :searchable="searchable" @search="handleSearch">
    <template #placeholder>
      <span
        v-if="showPlaceholder"
        :class="{
          'text-gray-400': isDropdownVisible || !selectLabel,
        }"
      >
        {{ selectLabel || placeholder }}
      </span>
    </template>
  </SelectSearchInput>
</template>
