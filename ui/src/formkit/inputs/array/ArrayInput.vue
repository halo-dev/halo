<script setup lang="ts">
import {
  getNode,
  type FormKitFrameworkContext,
  type FormKitNode,
  type FormKitProps,
} from "@formkit/core";
import { undefine } from "@formkit/utils";
import { IconClose, VAccordion, VButton } from "@halo-dev/components";
import { utils } from "@halo-dev/ui-shared";
import { isNil, isNotNil } from "es-toolkit";
import { cloneDeepWith, get } from "es-toolkit/compat";
import { computed, onMounted, ref, toRaw, watch } from "vue";
import { useDraggable, VueDraggable } from "vue-draggable-plus";
import MingcuteDotsLine from "~icons/mingcute/dots-line";
import type { ArrayItemLabel, ArrayItemLabelType } from ".";
import ArrayAccordionItem from "./ArrayAccordionItem.vue";
import ArrayFormModal from "./ArrayFormModal.vue";
import ArrayItemLabels from "./ArrayItemLabels.vue";
import { renderItemLabelValue } from "./renderers";
import type { ArrayItemFormMode, FormattedItemLabel } from "./types";

const formKitChildrenId = `formkit-children-${utils.id.uuid()}`;
const draftItemKey = "array-item-draft";
const dragHandleClass = `array-drag-handle-${utils.id.uuid()}`;

export type ArrayProps = {
  removeControl?: boolean;
  addButton?: boolean;
  addLabel?: string;
  addAttrs?: Record<string, unknown>;
  min?: number;
  max?: number;
  itemLabels?: ArrayItemLabel[];
  emptyText?: string;
  itemFormMode?: ArrayItemFormMode;
};

export type ArrayValue = Record<string, unknown>[];

type ArrayInputContext = {
  commitOpenAccordionItem?: () => Promise<boolean>;
};

const props = defineProps<{
  node: FormKitNode<ArrayValue>;
}>();

const arrayValue = ref<ArrayValue>(props.node.value);
const hiddenChildrenFormKit = ref<FormKitNode<unknown>>();
const arrayModal = ref<boolean>(false);
const nodeProps = ref<Partial<FormKitProps<ArrayProps>>>(props.node.props);

type FnType = (index: number) => Record<string, unknown>;

function createValue(num: number, fn: FnType): ArrayValue {
  return Array.from({ length: num }, (_, index) => fn(index));
}

function arrayFeature(node: FormKitNode<ArrayValue>) {
  const initProps = node.props as Partial<FormKitProps<ArrayProps>>;
  node.props.removeControl = initProps.removeControl ?? true;
  node.props.addButton = initProps.addButton ?? true;
  node.props.addAttrs = initProps.addAttrs ?? {};
  node.props.itemFormMode = initProps.itemFormMode ?? "modal";
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

  node.on("input", ({ payload }) => {
    arrayValue.value = toRaw(payload);
  });

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

onMounted(async () => {
  const node = props.node;
  node._c.sync = true;
  arrayFeature(node);
  hiddenChildrenFormKit.value = getNode(formKitChildrenId);
  const context = node.context as
    | (FormKitFrameworkContext & ArrayInputContext)
    | undefined;
  if (context) {
    context.commitOpenAccordionItem = commitOpenAccordionItem;
  }
  await updateFormattedLabels();
});

const parseItemLabel = async (
  itemLabel: ArrayItemLabel,
  item: Record<string, unknown>
): Promise<FormattedItemLabel[] | undefined> => {
  if (!itemLabel.label) {
    return;
  }

  if (itemLabel.label.startsWith("$value.")) {
    const path = itemLabel.label.split("$value.")[1];
    const value = get(item, path);
    const node = hiddenChildrenFormKit.value?.at(path);

    if (!node) {
      return [
        {
          type: itemLabel.type,
          value: String(value ?? ""),
        } as FormattedItemLabel,
      ];
    }
    const renderedValue = await renderItemLabelValue(node, value);
    const castRenderedValueArray = Array.isArray(renderedValue)
      ? renderedValue
      : [renderedValue];
    if (castRenderedValueArray.length === 0) {
      return [
        {
          type: itemLabel.type,
          value: String(value ?? ""),
        } as FormattedItemLabel,
      ];
    }
    return castRenderedValueArray
      .map((renderedValue) => {
        return {
          type: itemLabel.type,
          value: isNil(renderedValue.value) ? value : renderedValue.value,
          ...renderedValue,
        } as FormattedItemLabel;
      })
      .filter((item) => isNotNil(item.value));
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
    const results = await Promise.all<FormattedItemLabel[][]>(
      itemLabels.map((label: ArrayItemLabel) => parseItemLabel(label, item))
    ).then((results) => results.flat());

    return results.filter(isNotNil);
  }
  return [];
};

const itemValue = ref<Record<string, unknown>>({});
const currentEditIndex = ref<number>(-1);

const formattedItemLabels = ref<Map<string, FormattedItemLabel[]>>(new Map());

const itemKeys = new WeakMap<object, string>();
let itemKeySeed = 0;

const getItemKey = (item: Record<string, unknown>) => {
  let key = itemKeys.get(item);
  if (!key) {
    key = `array-item-${itemKeySeed++}`;
    itemKeys.set(item, key);
  }
  return key;
};

const updateFormattedLabels = async () => {
  const newLabelsMap = new Map<string, FormattedItemLabel[]>();

  for (let index = 0; index < arrayValue.value.length; index++) {
    const item = arrayValue.value[index];
    const key = getItemKey(item);
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

const applyItemData = (data: Record<string, unknown>, index: number) => {
  const oldNodeValue = [...((props.node._value as ArrayValue) || [])];
  if (index === -1) {
    props.node.input(oldNodeValue.concat(data), false);
    return;
  }

  if (index >= oldNodeValue.length) {
    throw new Error("Current edit index is out of range");
  }

  const currentItem = oldNodeValue[index];
  Object.keys(currentItem).forEach((key) => {
    if (!(key in data)) {
      delete currentItem[key];
    }
  });
  Object.assign(currentItem, data);
  props.node.input(oldNodeValue, false);
};

const handleModalSubmit = (data: Record<string, unknown>) => {
  applyItemData(data, currentEditIndex.value);
  arrayModal.value = false;
};

const handleDragUpdate = () => {
  props.node.input(arrayValue.value, false);
};

const handleRemoveItem = (index: number) => {
  if (arrayValue.value.length <= (nodeProps.value.min ?? 0)) {
    return;
  }
  arrayValue.value.splice(index, 1);
  props.node.input(arrayValue.value, false);
};

const isAccordionMode = computed(() => {
  return nodeProps.value.itemFormMode === "accordion";
});

const openAccordionItem = ref<string>();
const creatingAccordionItem = ref(false);
const accordion = ref<InstanceType<typeof VAccordion> | null>(null);
const existingAccordionItems = ref<InstanceType<typeof ArrayAccordionItem>[]>(
  []
);
const draftAccordionItem = ref<InstanceType<typeof ArrayAccordionItem> | null>(
  null
);
const accordionEl = computed(
  () => accordion.value?.$el as HTMLElement | undefined
);

const accordionDraggable = useDraggable(accordionEl, arrayValue, {
  immediate: false,
  animation: 150,
  handle: `.${dragHandleClass}`,
  draggable: ".array-accordion-item:not(.array-accordion-item--draft)",
  onUpdate: handleDragUpdate,
});

watch(
  accordionEl,
  (el) => {
    if (!isAccordionMode.value) {
      return;
    }
    if (el) {
      accordionDraggable.start(el);
    } else {
      accordionDraggable.destroy();
    }
  },
  { flush: "post" }
);

watch(openAccordionItem, (value) => {
  if (value !== draftItemKey) {
    creatingAccordionItem.value = false;
  }
});

const handleCreateAccordionItem = () => {
  if (arrayValue.value.length >= (nodeProps.value.max ?? 1 / 0)) {
    return;
  }
  creatingAccordionItem.value = true;
  openAccordionItem.value = draftItemKey;
};

const closeAccordionItem = () => {
  openAccordionItem.value = undefined;
  creatingAccordionItem.value = false;
};

const handleSaveAccordionItem = (
  data: Record<string, unknown>,
  index: number
) => {
  applyItemData(data, index);
  closeAccordionItem();
};

const handleRemoveAccordionItem = (index: number) => {
  closeAccordionItem();
  handleRemoveItem(index);
};

const commitOpenAccordionItem = async () => {
  if (!isAccordionMode.value || !openAccordionItem.value) {
    return true;
  }
  const openItem =
    openAccordionItem.value === draftItemKey
      ? draftAccordionItem.value
      : existingAccordionItems.value.find(
          (item) => item.value === openAccordionItem.value
        );
  return (await openItem?.commit()) ?? true;
};
</script>

<template>
  <div
    class="mt-4 w-full space-y-2"
    :class="isAccordionMode ? 'sm:max-w-2xl' : 'sm:max-w-lg'"
  >
    <template v-if="!isAccordionMode">
      <VueDraggable
        v-if="arrayValue.length > 0"
        v-model="arrayValue"
        :animation="150"
        class="flex flex-col gap-1"
        :handle="`.${dragHandleClass}`"
        @update="handleDragUpdate"
      >
        <div
          v-for="(item, index) in arrayValue"
          :key="getItemKey(item)"
          class="group/item flex h-12 items-center gap-2.5 rounded-md border bg-gray-50 px-2 transition-colors hover:bg-gray-100 active:bg-gray-200"
          @click="handleOpenArrayModal(item, index)"
        >
          <MingcuteDotsLine
            class="drag-handle size-4 flex-none cursor-move text-gray-500"
            :class="dragHandleClass"
          />
          <div
            class="line-clamp-1 flex min-w-0 flex-1 shrink cursor-pointer items-center gap-2 whitespace-nowrap text-sm text-gray-900"
          >
            <ArrayItemLabels
              :labels="formattedItemLabels.get(getItemKey(item))"
            />
          </div>
          <IconClose
            v-if="nodeProps.removeControl"
            :disabled="arrayValue.length <= (nodeProps.min ?? 0)"
            class="size-4 flex-none cursor-pointer text-gray-500 opacity-0 transition-opacity hover:text-gray-900 group-hover/item:opacity-100"
            :class="{
              'pointer-events-none cursor-not-allowed opacity-50 hover:text-gray-500 group-hover/item:opacity-50':
                arrayValue.length <= (nodeProps.min ?? 0),
            }"
            @click.stop="handleRemoveItem(index)"
          />
        </div>
      </VueDraggable>
      <div v-else class="text-sm text-gray-500">
        {{ nodeProps.emptyText ?? $t("core.formkit.array.empty_text") }}
      </div>
    </template>

    <template v-else>
      <VAccordion
        v-if="arrayValue.length > 0 || creatingAccordionItem"
        ref="accordion"
        v-model="openAccordionItem"
        type="single"
        collapsible
        variant="plain"
        :unmount-on-hide="true"
        class="array-accordion flex w-full flex-col gap-1"
      >
        <ArrayAccordionItem
          v-for="(item, index) in arrayValue"
          ref="existingAccordionItems"
          :key="getItemKey(item)"
          :node="node"
          :value="getItemKey(item)"
          :item="item"
          :labels="formattedItemLabels.get(getItemKey(item))"
          :open="openAccordionItem === getItemKey(item)"
          :drag-handle-class="dragHandleClass"
          :remove-control="nodeProps.removeControl"
          :can-remove="arrayValue.length > (nodeProps.min ?? 0)"
          @save="handleSaveAccordionItem($event, index)"
          @cancel="closeAccordionItem"
          @remove="handleRemoveAccordionItem(index)"
        />
        <ArrayAccordionItem
          v-if="creatingAccordionItem"
          ref="draftAccordionItem"
          :node="node"
          :value="draftItemKey"
          :item="{}"
          :open="openAccordionItem === draftItemKey"
          :drag-handle-class="dragHandleClass"
          is-draft
          :remove-control="false"
          :can-remove="false"
          @save="handleSaveAccordionItem($event, -1)"
          @cancel="closeAccordionItem"
        />
      </VAccordion>
      <div
        v-if="arrayValue.length === 0 && !creatingAccordionItem"
        class="text-sm text-gray-500"
      >
        {{ nodeProps.emptyText ?? $t("core.formkit.array.empty_text") }}
      </div>
    </template>

    <VButton
      v-if="nodeProps.addButton"
      v-bind="nodeProps.addAttrs"
      :disabled="
        arrayValue.length >= (nodeProps.max ?? 1 / 0) ||
        (isAccordionMode && creatingAccordionItem)
      "
      size="sm"
      type="default"
      @click="
        isAccordionMode ? handleCreateAccordionItem() : handleOpenArrayModal()
      "
    >
      {{ nodeProps.addLabel ?? $t("core.common.buttons.add") }}
    </VButton>
  </div>

  <FormKit v-show="false" :id="formKitChildrenId" type="group" ignore="true">
    <component :is="node.context?.slots.default" />
  </FormKit>

  <ArrayFormModal
    v-if="!isAccordionMode && arrayModal"
    :node="node"
    :item-value="itemValue"
    @submit="handleModalSubmit"
    @close="handleCloseArrayModal"
  />
</template>

<style lang="scss">
.array-accordion.v-accordion--plain {
  .v-accordion-item {
    @apply overflow-hidden rounded-md border border-gray-200 bg-gray-50 transition-colors;
  }

  .v-accordion-item[data-state="open"] {
    @apply bg-white;
  }

  .v-accordion-trigger {
    @apply h-12 gap-2.5 px-2 py-0;

    &:hover {
      @apply bg-gray-100;
    }
  }

  .v-accordion-item[data-state="open"] .v-accordion-trigger {
    @apply bg-gray-50;
  }

  .v-accordion-item[data-state="open"] .v-accordion-trigger:hover {
    @apply bg-gray-100;
  }

  .v-accordion-content-inner {
    @apply p-0;
  }
}
</style>
