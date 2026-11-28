<script setup lang="ts">
import {
  getNode,
  type FormKitGroupValue,
  type FormKitNode,
} from "@formkit/core";
import { utils } from "@halo-dev/ui-shared";
import { isEqual } from "es-toolkit";
import {
  cloneVNode,
  computed,
  isVNode,
  nextTick,
  onMounted,
  ref,
  watch,
} from "vue";

const props = defineProps<{
  node: FormKitNode;
  itemValue: Record<string, unknown>;
}>();

type ArrayItemFormContext = {
  arrayItemOriginal?: Record<string, unknown>;
  commitOpenAccordionItem?: () => Promise<boolean>;
};

const emit = defineEmits<{
  (event: "submit", data: Record<string, unknown>): void;
}>();

const formKitNode = ref<FormKitNode>();
const formId = `array-item-form-${utils.id.uuid()}`;
const itemModel = ref<Record<string, unknown>>({ ...props.itemValue });

watch(
  () => props.itemValue,
  (value) => {
    itemModel.value = { ...value };
  }
);

onMounted(async () => {
  await nextTick();
  formKitNode.value = getNode(formId);
  const context = formKitNode.value?.context as
    | ArrayItemFormContext
    | undefined;
  if (context) {
    context.arrayItemOriginal = props.itemValue;
  }
});

const formKitNodeValid = computed(() => {
  return formKitNode.value?.context?.state.valid ?? true;
});

const normalizeEmptyValues = (value: unknown): unknown => {
  if (Array.isArray(value)) {
    return value.map(normalizeEmptyValues);
  }
  if (value && typeof value === "object") {
    return Object.entries(value).reduce<Record<string, unknown>>(
      (result, [key, item]) => {
        if (item !== undefined) {
          result[key] = normalizeEmptyValues(item);
        }
        return result;
      },
      {}
    );
  }
  return value;
};

const isDirty = computed(() => {
  return !isEqual(
    normalizeEmptyValues(itemModel.value),
    normalizeEmptyValues(props.itemValue)
  );
});

let submitResolver: ((valid: boolean) => void) | undefined;

const resolveSubmit = (valid: boolean) => {
  submitResolver?.(valid);
  submitResolver = undefined;
};

const handleInput = (value?: FormKitGroupValue) => {
  itemModel.value = { ...value };
};

const commitOpenNestedArrays = async () => {
  const node = formKitNode.value;
  if (!node) {
    return true;
  }
  const arrayNodes: FormKitNode[] = [];
  node.walk((child) => {
    const context = child.context as ArrayItemFormContext | undefined;
    if (typeof context?.commitOpenAccordionItem === "function") {
      arrayNodes.push(child);
    }
  });
  // Children come before their ancestors so the saved parent data contains the
  // freshly committed nested values.
  for (const arrayNode of arrayNodes.reverse()) {
    const commit = (arrayNode.context as ArrayItemFormContext)
      .commitOpenAccordionItem;
    if (commit && !(await commit())) {
      return false;
    }
  }
  return true;
};

const handleSubmit = async () => {
  if (!formKitNodeValid.value) {
    resolveSubmit(false);
    return;
  }
  if (!(await commitOpenNestedArrays())) {
    resolveSubmit(false);
    return;
  }
  emit("submit", {
    ...((formKitNode.value?.value as Record<string, unknown> | undefined) ??
      {}),
  });
  resolveSubmit(true);
};

const handleSubmitInvalid = () => {
  resolveSubmit(false);
};

const submit = () => {
  const node = formKitNode.value;
  if (!node) {
    return Promise.resolve(false);
  }
  return new Promise<boolean>((resolve) => {
    submitResolver = resolve;
    node.submit();
  });
};

const ItemFormFields = () => {
  const slot = props.node.context?.slots.default;
  if (!slot) {
    return null;
  }
  const nodes = slot({ value: itemModel.value });
  return (Array.isArray(nodes) ? nodes : [nodes]).map((node, index) => {
    if (!isVNode(node)) {
      return node;
    }
    const name = (node.props as Record<string, unknown> | null)?.name;
    return cloneVNode(node, {
      key: typeof name === "string" ? name : index,
    });
  });
};

defineExpose({
  submit,
  isDirty,
});
</script>

<template>
  <div @submit.stop>
    <FormKit
      :id="formId"
      type="form"
      :ignore="true"
      :actions="false"
      :model-value="itemModel"
      @update:model-value="handleInput"
      @submit="handleSubmit"
      @submit-invalid="handleSubmitInvalid"
    >
      <ItemFormFields />
    </FormKit>
  </div>
</template>
