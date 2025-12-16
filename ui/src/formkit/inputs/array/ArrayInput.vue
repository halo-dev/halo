<script setup lang="ts">
import { getNode, type FormKitNode, type FormKitProps } from "@formkit/core";
import { undefine } from "@formkit/utils";
import { IconClose, VButton } from "@halo-dev/components";
import { utils } from "@halo-dev/ui-shared";
import { Icon } from "@iconify/vue";
import { cloneDeepWith } from "es-toolkit";
import { get } from "es-toolkit/compat";
import objectHash from "object-hash";
import { onMounted, ref, toRaw } from "vue";
import { VueDraggable } from "vue-draggable-plus";
import MingcuteDotsLine from "~icons/mingcute/dots-line";
import type { ArrayItemLabel, ArrayItemLabelType } from ".";
import ArrayFormModal from "./ArrayFormModal.vue";

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

onMounted(() => {
  const node = props.node;
  node._c.sync = true;
  arrayFeature(node);
  hiddenChildrenFormKit.value = getNode(formKitChildrenId);
});

const renderItemLabelValue = (
  node: FormKitNode<unknown>,
  value: unknown
): Record<string, unknown> => {
  switch (node.props.type) {
    case "select": {
      let renderValue = value;
      const options = node.context?.attrs.options;
      const selectedOption = findSelectedOption(options, value);
      if (selectedOption) {
        renderValue = selectedOption.label;
      }
      return {
        value: renderValue,
      };
    }
    case "nativeSelect": {
      let renderValue = value;
      const options = node.context?.options as unknown[];
      const selectedOption = findSelectedOption(options, value);
      if (selectedOption) {
        renderValue = selectedOption.group
          ? `${selectedOption.group}/${selectedOption.label}`
          : selectedOption.label;
      }
      return {
        value: renderValue,
      };
    }
    case "iconify": {
      const format = node.props.format;
      return {
        format,
      };
    }
    default: {
      return {
        value,
      };
    }
  }
};

/**
 * from options to find the selected option
 *
 * @param options - the options to search
 * @param value - the value to search for
 * @returns the selected option
 */
function findSelectedOption(
  options: unknown[],
  value: unknown
):
  | {
      label: string;
      value: unknown;
      group?: unknown;
    }
  | undefined {
  if (!options || options.length === 0) {
    return;
  }

  for (const option of options) {
    if (!option) {
      continue;
    }

    if (typeof option === "string") {
      if (option === value) {
        return {
          label: option,
          value: option,
        };
      }
    }

    if (typeof option === "object") {
      if ("value" in option && "label" in option) {
        if (option.value === value) {
          return {
            label: option.label as string,
            value: option.value,
          };
        }
      }

      if ("group" in option && "options" in option) {
        const options = option.options as unknown[];
        const selectedOption = findSelectedOption(options, value);
        if (selectedOption) {
          return {
            label: selectedOption.label,
            value: selectedOption.value,
            group: option.group,
          };
        }
      }
    }
  }
}

const parseItemLabel = (
  itemLabel: ArrayItemLabel,
  item: Record<string, unknown>
) => {
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
        value: value,
      };
    }
    return {
      type: itemLabel.type,
      value,
      ...renderItemLabelValue(node, value),
    };
  }
};

const formatItemLabel = (item: Record<string, unknown>) => {
  const defaultItemLabel = Object.keys(item).map((key) => {
    return {
      type: "text",
      label: `$value.${key}`,
    };
  });
  const itemLabels = props.node.props.itemLabels ?? defaultItemLabel;
  if (itemLabels.length > 0) {
    const result = itemLabels
      .map((itemLabel: ArrayItemLabel) => {
        return parseItemLabel(itemLabel, item);
      })
      .filter(Boolean)
      .filter((itemLabel) => !!itemLabel.value);
    return result;
  }
  return [];
};

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
            v-for="itemLabel in formatItemLabel(item)"
            :key="itemLabel.label"
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
                :src="itemLabel.value"
                class="size-full object-cover"
              />
            </a>
            <span v-if="itemLabel.type === 'text'">
              {{ itemLabel.value }}
            </span>
            <div
              v-if="itemLabel.type === 'iconify'"
              class="inline-flex items-center [&>*]:size-4"
            >
              <img
                v-if="['url', 'dataurl'].includes(itemLabel.format)"
                :src="itemLabel.value"
                class="max-w-none"
              />
              <Icon
                v-else-if="itemLabel.format === 'name'"
                :icon="itemLabel.value"
              />
              <div
                v-else
                class="inline-flex items-center justify-center"
                v-html="itemLabel.value"
              ></div>
            </div>
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
