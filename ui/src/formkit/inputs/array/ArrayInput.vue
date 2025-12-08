<script setup lang="ts">
import { type FormKitNode, type FormKitProps } from "@formkit/core";
import { undefine } from "@formkit/utils";
import { IconClose, VButton } from "@halo-dev/components";
import { cloneDeepWith } from "lodash-es";
import objectHash from "object-hash";
import { onMounted, ref } from "vue";
import { VueDraggable } from "vue-draggable-plus";
import MingcuteDotsLine from "~icons/mingcute/dots-line";
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

function getItemImage(item: Record<string, unknown>) {
  // TODO: @LIlGG
  return "https://www.halo.run/logo";
}
</script>
<template>
  <div class="mt-4 w-full space-y-2 sm:max-w-lg">
    <VueDraggable
      v-if="arrayValue.length > 0"
      v-model="arrayValue"
      :animation="150"
      class="flex flex-col gap-1"
      handle=".drag-handle"
      @update="handleDragUpdate"
    >
      <div
        v-for="(item, index) in arrayValue"
        :key="generateKey(item, index)"
        class="group/item flex h-12 items-center gap-2.5 rounded-md border bg-gray-50 px-2 transition-colors hover:bg-gray-100 active:bg-gray-200"
        @click="handleOpenArrayModal(item, index)"
      >
        <MingcuteDotsLine class="drag-handle size-4.5 flex-none cursor-move" />
        <a
          v-if="getItemImage(item)"
          :href="getItemImage(item)"
          target="_blank"
          class="block aspect-1 size-8 flex-none"
          @click.stop
        >
          <img
            v-tooltip="`查看图片：${getItemImage(item)}`"
            :src="getItemImage(item)"
            class="size-full object-cover"
          />
        </a>
        <div
          class="line-clamp-1 min-w-0 flex-1 shrink cursor-pointer text-sm text-gray-900"
        >
          {{ formatItemLabel(item) }}
        </div>
        <IconClose
          class="size-4.5 flex-none cursor-pointer text-gray-500 opacity-0 transition-opacity hover:text-gray-900 group-hover/item:opacity-100"
          @click.stop="handleRemoveItem(index)"
        />
      </div>
    </VueDraggable>
    <div v-else class="text-sm text-gray-500">没有条目</div>
    <VButton size="sm" type="default" @click="handleOpenArrayModal()">
      {{ $t("core.common.buttons.add") }}
    </VButton>
  </div>

  <ArrayFormModal
    v-if="arrayModal"
    :node="node"
    :item-value="itemValue"
    :current-edit-index="currentEditIndex"
    @close="handleCloseArrayModel"
  />
</template>
