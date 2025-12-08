<script setup lang="ts">
import { getNode, type FormKitNode } from "@formkit/core";
import { VButton, VModal, VSpace } from "@halo-dev/components";
import { utils } from "@halo-dev/ui-shared";
import { computed, onMounted, ref } from "vue";
import type { ArrayValue } from "./ArrayInput.vue";

const { node, itemValue, currentEditIndex } = defineProps<{
  node: FormKitNode;
  itemValue: Record<string, unknown>;
  currentEditIndex: number;
}>();

const modal = ref<InstanceType<typeof VModal> | null>(null);
const formKitNode = ref<FormKitNode>();
const isSubmitting = ref(false);
const formId = `array-item-form-${utils.id.uuid()}`;

onMounted(() => {
  formKitNode.value = getNode(formId);
});

const formKitNodeValid = computed(() => {
  return formKitNode.value?.context?.state.valid ?? true;
});

const emit = defineEmits<{
  (event: "close"): void;
}>();

const handleSave = (data: Record<string, unknown>) => {
  if (!formKitNodeValid.value) {
    return;
  }
  const oldNodeValue = (node._value as ArrayValue) || [];
  if (currentEditIndex === -1) {
    node.input(oldNodeValue.concat(data), false);
    modal.value?.close();
    return;
  }

  if (currentEditIndex >= oldNodeValue.length) {
    throw new Error("Current edit index is out of range");
  }

  oldNodeValue[currentEditIndex] = data;
  node.input(oldNodeValue, false);
  modal.value?.close();
};
</script>
<template>
  <VModal
    ref="modal"
    title="编辑条目"
    :width="700"
    mount-to-body
    layer-closable
    :centered="false"
    @close="emit('close')"
  >
    <FormKit
      :id="formId"
      type="form"
      :ignore="true"
      :actions="false"
      :model-value="itemValue"
      @submit="handleSave"
    >
      <component
        :is="node.context?.slots.default"
        v-bind="{
          value: itemValue,
        }"
      />
    </FormKit>

    <template #footer>
      <VSpace>
        <VButton
          type="secondary"
          :loading="isSubmitting"
          @click="() => formKitNode?.submit()"
        >
          {{ $t("core.common.buttons.submit") }}
        </VButton>
        <VButton @click="modal?.close()">
          {{ $t("core.common.buttons.cancel_and_shortcut") }}
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>
