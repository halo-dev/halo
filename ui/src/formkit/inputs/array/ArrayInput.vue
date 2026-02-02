<script setup lang="ts">
import { getNode, type FormKitNode, type FormKitProps } from "@formkit/core";
import { undefine } from "@formkit/utils";
import { IconClose, VButton } from "@halo-dev/components";
import { utils } from "@halo-dev/ui-shared";
import { isNil } from "es-toolkit";
import { cloneDeepWith, get } from "es-toolkit/compat";
import objectHash from "object-hash";
import { onMounted, ref, toRaw, watch } from "vue";
import { VueDraggable } from "vue-draggable-plus";
import MingcuteDotsLine from "~icons/mingcute/dots-line";
import type { ArrayItemLabel, ArrayItemLabelType } from ".";
import type { IconifyFormat, IconifyValue } from "../iconify/types";
import ArrayFormModal from "./ArrayFormModal.vue";
import ColorLabel from "./labels/ColorLabel.vue";
import IconifyLabel from "./labels/IconifyLabel.vue";
import { renderItemLabelValue } from "./renderers";

const formKitChildrenId = `formkit-children-${utils.id.uuid()}`;

export type ArrayProps = {
  removeControl?: boolean;
  addButton?: boolean;
  addLabel?: boolean;
  addAttrs?: Record<string, unknown>;
  min?: number;
  max?: number;
  itemLabels: ArrayItemLabelType[];
};

export type ArrayValue = Record<string, unknown>[];

const props = defineProps<{
  node: FormKitNode<ArrayValue>;
}>();

const arrayValue = ref<ArrayValue>(props.node.value);
const hiddenChildrenFormKit = ref<FormKitNode<unknown>>();
const arrayModal = ref<boolean>(false);
const nodeProps = ref<Partial<FormKitProps<ArrayProps>>>(props.node.props);

type FnType = (index: number) => Record<string, unknown>;

function createValue(num: number, fn: FnType): ArrayValue {
  return new Array(num).fill("").map((_, index) => fn(index));
}

function arrayFeature(node: FormKitNode<ArrayValue>) {
  const initProps = node.props as Partial<FormKitProps<ArrayProps>>;
  node.props.removeControl = initProps.removeControl ?? true;
  node.props.addButton = initProps.addButton ?? true;
  node.props.addAttrs = initProps.addAttrs ?? {};
  node.props.min = initProps.min ? Number(initProps.min) : 0;
  node.props.max = initProps.max ? Number(initProps.max) : 1 / 0;
  if (node.props.min > node.props.max) {
    throw Error("Array: min must be less than max");
  }

  if ("disabled" in initProps) {
    node.props.disabled = undefine(initProps.disabled);
  }

  nodeProps.value = {
    ...initProps,
  };

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

  node.on("input", ({ payload }) => {
    arrayValue.value = toRaw(payload);
  });
}

onMounted(async () => {
  const node = props.node;
  node._c.sync = true;
  arrayFeature(node);
  hiddenChildrenFormKit.value = getNode(formKitChildrenId);
  await updateFormattedLabels();
});

type FormattedItemLabel =
  | {
      type: "text" | "image" | "color";
      value: string;
    }
  | {
      type: "iconify";
      value: string | IconifyValue;
      format: IconifyFormat;
      valueOnly?: boolean;
    };

const parseItemLabel = async (
  itemLabel: ArrayItemLabel,
  item: Record<string, unknown>
): Promise<FormattedItemLabel | undefined> => {
  if (!itemLabel.label) {
    return;
  }

  if (itemLabel.label.startsWith("$value.")) {
    const path = itemLabel.label.split("$value.")[1];
    const value = get(item, path);
    const node = hiddenChildrenFormKit.value?.at(path);

    if (!node) {
      return {
        type: itemLabel.type,
        value: String(value ?? ""),
      } as FormattedItemLabel;
    }
    const renderedValue = await renderItemLabelValue(node, value);
    renderedValue.value = isNil(renderedValue.value)
      ? value
      : renderedValue.value;
    return {
      type: itemLabel.type,
      value: renderedValue.value,
      ...renderedValue,
    } as FormattedItemLabel;
  }
};

const formatItemLabel = async (
  item: Record<string, unknown>
): Promise<FormattedItemLabel[]> => {
  const defaultItemLabel = Object.keys(item).map((key) => {
    return {
      type: "text" as ArrayItemLabelType,
      label: `$value.${key}`,
    };
  });
  const itemLabels = props.node.props.itemLabels ?? defaultItemLabel;
  if (itemLabels.length > 0) {
    const results = await Promise.all(
      itemLabels.map((itemLabel: ArrayItemLabel) => {
        return parseItemLabel(itemLabel, item);
      })
    );
    return results.filter(
      (itemLabel): itemLabel is FormattedItemLabel => !!itemLabel?.value
    );
  }
  return [];
};

const itemValue = ref<Record<string, unknown>>({});
const currentEditIndex = ref<number>(-1);

const formattedItemLabels = ref<Map<string, FormattedItemLabel[]>>(new Map());

const updateFormattedLabels = async () => {
  const newLabelsMap = new Map();

  for (let index = 0; index < arrayValue.value.length; index++) {
    const item = arrayValue.value[index];
    const key = generateKey(item, index);
    const labels = await formatItemLabel(item);
    newLabelsMap.set(key, labels);
  }

  formattedItemLabels.value = newLabelsMap;
};

watch(
  () => arrayValue.value,
  async () => {
    await updateFormattedLabels();
  },
  { deep: true, immediate: false }
);

const handleOpenArrayModal = (
  item?: Record<string, unknown>,
  index?: number
) => {
  itemValue.value = item ? cloneDeepWith(item) : {};
  currentEditIndex.value = index ?? -1;
  arrayModal.value = true;
};

const handleCloseArrayModal = () => {
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
  if (arrayValue.value.length <= nodeProps.value.min) {
    return;
  }
  arrayValue.value.splice(index, 1);
  props.node.input(arrayValue.value, false);
};
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
        <div
          class="line-clamp-1 flex min-w-0 flex-1 shrink cursor-pointer items-center gap-2 whitespace-nowrap text-sm text-gray-900"
        >
          <template
            v-for="(itemLabel, labelIndex) in formattedItemLabels.get(
              generateKey(item, index)
            ) || []"
            :key="labelIndex"
          >
            <a
              v-if="itemLabel.type === 'image'"
              :href="itemLabel.value"
              target="_blank"
              class="block aspect-1 size-8 flex-none"
              @click.stop
            >
              <img
                v-tooltip="
                  $t('core.formkit.array.image_tooltip', {
                    value: itemLabel.value,
                  })
                "
                :src="utils.attachment.getThumbnailUrl(itemLabel.value, 'S')"
                class="size-full object-cover"
              />
            </a>
            <span v-if="itemLabel.type === 'text'">
              {{ itemLabel.value }}
            </span>
            <ColorLabel
              v-if="itemLabel.type === 'color'"
              :item-label="itemLabel"
            />
            <IconifyLabel
              v-if="itemLabel.type === 'iconify'"
              :item-label="itemLabel"
            />
          </template>
        </div>
        <IconClose
          v-if="nodeProps.removeControl"
          :disabled="arrayValue.length <= nodeProps.min"
          class="size-4.5 flex-none cursor-pointer text-gray-500 opacity-0 transition-opacity hover:text-gray-900 group-hover/item:opacity-100"
          :class="{
            'pointer-events-none cursor-not-allowed opacity-50 hover:text-gray-500 group-hover/item:opacity-50':
              arrayValue.length <= nodeProps.min,
          }"
          @click.stop="handleRemoveItem(index)"
        />
      </div>
    </VueDraggable>
    <div v-else class="text-sm text-gray-500">
      {{ nodeProps.emptyText ?? $t("core.formkit.array.empty_text") }}
    </div>
    <VButton
      v-if="nodeProps.addButton"
      v-bind="nodeProps.addAttrs"
      :disabled="arrayValue.length >= nodeProps.max"
      size="sm"
      type="default"
      @click="handleOpenArrayModal()"
    >
      {{ nodeProps.addLabel ?? $t("core.common.buttons.add") }}
    </VButton>
  </div>

  <FormKit v-show="false" :id="formKitChildrenId" type="group" ignore="true">
    <component :is="node.context?.slots.default" />
  </FormKit>

  <ArrayFormModal
    v-if="arrayModal"
    :node="node"
    :item-value="itemValue"
    :current-edit-index="currentEditIndex"
    @close="handleCloseArrayModal"
  />
</template>
