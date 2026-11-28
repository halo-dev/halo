<script setup lang="ts">
import {
  getNode,
  type FormKitGroupValue,
  type FormKitNode,
} from "@formkit/core";
import { utils } from "@halo-dev/ui-shared";
import { computed, nextTick, onMounted, ref, watch } from "vue";

const props = defineProps<{
  node: FormKitNode;
  itemValue: Record<string, unknown>;
}>();

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
});

const formKitNodeValid = computed(() => {
  return formKitNode.value?.context?.state.valid ?? true;
});

let submitResolver: ((valid: boolean) => void) | undefined;

const resolveSubmit = (valid: boolean) => {
  submitResolver?.(valid);
  submitResolver = undefined;
};

const handleInput = (value?: FormKitGroupValue) => {
  itemModel.value = { ...value };
};

const handleSubmit = (data: Record<string, unknown>) => {
  if (!formKitNodeValid.value) {
    resolveSubmit(false);
    return;
  }
  emit("submit", data);
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
  return slot ? slot({ value: itemModel.value }) : null;
};

defineExpose({
  submit,
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
