<script lang="ts" setup>
import type {
  NodeDragEventData,
  NodeTouchEventData,
} from "@formkit/drag-and-drop";
import { useDragAndDrop } from "@formkit/drag-and-drop/vue";
import { watch } from "vue";
import MultipleOverflowItem from "./MultipleOverflowItem.vue";
import MultipleSelectItem from "./MultipleSelectItem.vue";
import type { SelectOption } from "./types";

const props = defineProps<{
  selectedOptions: SelectOption[];
  sortable: boolean;
}>();

const emit = defineEmits<{
  (event: "deleteItem", index: number, option?: SelectOption): void;
  (event: "sort", value: SelectOption[]): void;
}>();

const [parent, options] = useDragAndDrop<SelectOption>(props.selectedOptions, {
  disabled: !props.sortable,
  sortable: true,
  handleEnd: (
    data: NodeDragEventData<SelectOption> | NodeTouchEventData<SelectOption>
  ) => {
    const nodeData = data.targetData.node.data;
    const dragBeforeIndex = props.selectedOptions.findIndex(
      (option) => option.value === nodeData.value.value
    );
    if (dragBeforeIndex != nodeData.index) {
      emit("sort", options.value);
    }
  },
});

watch(
  () => props.selectedOptions,
  (value) => {
    options.value = value;
  }
);
</script>

<template>
  <div ref="parent">
    <MultipleOverflowItem
      v-for="(option, index) in options"
      :key="option.value"
    >
      <MultipleSelectItem
        class="mx-1 my-0.5 ml-0"
        @delete-select-item="emit('deleteItem', index, option)"
      >
        <span
          tabindex="-1"
          class="select-item mr-1 inline-block cursor-default overflow-hidden truncate whitespace-pre"
        >
          {{ option.label }}
        </span>
      </MultipleSelectItem>
    </MultipleOverflowItem>
  </div>
</template>
