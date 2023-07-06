<script lang="ts" setup>
import { IconArrowDown, VDropdown, VDropdownItem } from "@halo-dev/components";
import { computed } from "vue";

const props = withDefaults(
  defineProps<{
    items: {
      label: string;
      value?: string | boolean | number;
    }[];
    label: string;
    modelValue: string | boolean | number | undefined;
  }>(),
  {}
);

defineEmits<{
  (
    event: "update:modelValue",
    modelValue: string | boolean | number | undefined
  ): void;
}>();

const selectedItem = computed(() => {
  return props.items.find((item) => item.value === props.modelValue);
});
</script>

<template>
  <VDropdown>
    <div
      class="flex cursor-pointer select-none items-center text-sm text-gray-700 hover:text-black"
    >
      <span v-if="!selectedItem" class="mr-0.5">
        {{ label }}
      </span>
      <span v-else> {{ label }}：{{ selectedItem.label }} </span>
      <span>
        <IconArrowDown />
      </span>
    </div>
    <template #popper>
      <VDropdownItem
        v-for="(item, index) in items"
        :key="index"
        :selected="item.value === modelValue"
        @click="$emit('update:modelValue', item.value)"
      >
        {{ item.label }}
      </VDropdownItem>
    </template>
  </VDropdown>
</template>
