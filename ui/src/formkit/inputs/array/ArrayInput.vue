<script setup lang="ts">
import { type FormKitNode, type FormKitProps } from "@formkit/core";
import { undefine } from "@formkit/utils";
import { VButton } from "@halo-dev/components";
import { cloneDeepWith } from "lodash-es";
import objectHash from "object-hash";
import { onMounted, ref } from "vue";
import { VueDraggable } from "vue-draggable-plus";
import MdiRemove from "~icons/mdi/remove";
import RadixIconsDragHandleDots2 from "~icons/radix-icons/drag-handle-dots-2";
import ArrayFormModal from "./ArrayFormModal.vue";

export type ArrayProps = {
  removeControl?: boolean;
  upControl?: boolean;
  downControl?: boolean;
  insertControl?: boolean;
  addButton?: boolean;
  addLabel?: boolean;
  addAttrs?: Record<string, unknown>;
  min?: number;
  max?: number;
  // item 需要展示的文本项， 支持表达式方式，如：$label.xxx  $value.title - $value.description
  itemLabel: string;
};

export type ArrayValue = Record<string, unknown>[];

const props = defineProps<{
  node: FormKitNode<ArrayValue>;
}>();

const arrayModal = ref<boolean>(false);

type FnType = (index: number) => Record<string, unknown>;

function createValue(num: number, fn: FnType): ArrayValue {
  return new Array(num).fill("").map((_, index) => fn(index));
}

function arrayFeature(node: FormKitNode<ArrayValue>) {
  const nodeProps = node.props as Partial<FormKitProps<ArrayProps>>;
  node.props.removeControl = nodeProps.removeControl ?? true;
  node.props.upControl = nodeProps.upControl ?? true;
  node.props.downControl = nodeProps.downControl ?? true;
  node.props.insertControl = nodeProps.insertControl ?? true;
  node.props.addButton = nodeProps.addButton ?? true;
  node.props.addLabel = nodeProps.addLabel ?? false;
  node.props.addAttrs = nodeProps.addAttrs ?? {};
  node.props.min = nodeProps.min ? Number(nodeProps.min) : 0;
  node.props.max = nodeProps.max ? Number(nodeProps.max) : 1 / 0;
  if (node.props.min > nodeProps.max) {
    throw Error("Repeater: min must be less than max");
  }

  if ("disabled" in nodeProps) {
    node.props.disabled = undefine(nodeProps.disabled);
  }

  if (Array.isArray(node.value)) {
    if (node.value.length < node.props.min) {
      const value = createValue(node.props.min - node.value.length, () => ({}));
      node.input(node.value.concat(value), false);
    } else {
      if (node.value.length > node.props.max) {
        node.input(node.value.slice(0, node.props.max), false);
      }
    }
  } else {
    node.input(
      createValue(node.props.min, () => ({})),
      false
    );
  }
}

// 根据 itemLabel 表达式，计算出 item 需要展示的文本项
const formatItemLabel = (item: Record<string, unknown>) => {
  if (item === null || item === undefined) {
    return "";
  }

  if (props.node.props.itemLabel) {
    // TODO: 计算出 item 需要展示的文本项
    return "";
  }
  // 所有 item 的 value 集合，使用 - 连接
  return Object.values(item).join("-");
};

onMounted(() => {
  const node = props.node;
  node._c.sync = true;
  arrayFeature(node);
});

const itemValue = ref<Record<string, unknown>>({});
const currentEditIndex = ref<number>(-1);

const handleOpenArrayModal = (
  item?: Record<string, unknown>,
  index?: number
) => {
  itemValue.value = item ? cloneDeepWith(item) : {};
  currentEditIndex.value = index ?? -1;
  arrayModal.value = true;
};

const arrayValue = ref<ArrayValue>(props.node.value);

const handleCloseArrayModel = () => {
  currentEditIndex.value = -1;
  arrayModal.value = false;
  arrayValue.value = [...props.node.value];
};

const generateKey = (item: Record<string, unknown>, index: number) => {
  return `${objectHash(item)}-${index}`;
};

const handleDragUpdate = () => {
  props.node.input(arrayValue.value, false);
};

const handleRemoveItem = (index: number) => {
  arrayValue.value.splice(index, 1);
  props.node.input(arrayValue.value, false);
};
</script>
<template>
  <div class="flex flex-col gap-2">
    <div>
      <div v-if="arrayValue.length > 0">
        <VueDraggable
          v-model="arrayValue"
          :animation="150"
          class="flex flex-col gap-1"
          handle=".drag-handle"
          @update="handleDragUpdate"
        >
          <li
            v-for="(item, index) in arrayValue"
            :key="generateKey(item, index)"
            class="text-ellipsis text-nowrap rounded-md border bg-gray-100 p-2 hover:bg-gray-200"
          >
            <div class="flex items-center gap-2">
              <RadixIconsDragHandleDots2
                class="drag-handle size-4 cursor-move"
              />
              <span
                class="cursor-pointer"
                @click="handleOpenArrayModal(item, index)"
                >{{ formatItemLabel(item) }}</span
              >
              <MdiRemove
                class="size-4 cursor-pointer"
                @click="handleRemoveItem(index)"
              />
            </div>
          </li>
        </VueDraggable>
      </div>
      <div v-else>No Items</div>
    </div>
    <VButton type="primary" @click="handleOpenArrayModal()">Add Item</VButton>
  </div>

  <ArrayFormModal
    v-if="arrayModal"
    :node="node"
    :item-value="itemValue"
    :current-edit-index="currentEditIndex"
    @close="handleCloseArrayModel"
  />
</template>
