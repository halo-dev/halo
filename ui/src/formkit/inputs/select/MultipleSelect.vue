<script lang="ts" setup>
import type {
  NodeDragEventData,
  NodeTouchEventData,
} from "@formkit/drag-and-drop";
import { useDragAndDrop } from "@formkit/drag-and-drop/vue";
import { watch } from "vue";
import MultipleOverflowItem from "./MultipleOverflowItem.vue";
import MultipleSelectItem from "./MultipleSelectItem.vue";

const props = defineProps<{
  selectedOptions: Array<{
    label: string;
    value: string;
  }>;
  sortable: boolean;
}>();

const emit = defineEmits<{
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

const [parent, options] = useDragAndDrop<{
  label: string;
  value: string;
}>(props.selectedOptions, {
  disabled: !props.sortable,
  sortable: true,
  handleEnd: (
    data:
      | NodeDragEventData<{
          label: string;
          value: string;
        }>
      | NodeTouchEventData<{
          label: string;
          value: string;
        }>
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
