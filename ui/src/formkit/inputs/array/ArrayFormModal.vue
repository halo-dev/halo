<script setup lang="ts">
import type { FormKitNode } from "@formkit/core";
import { VButton, VModal, VSpace } from "@halo-dev/components";
import { ref } from "vue";
import ArrayItemForm from "./ArrayItemForm.vue";

const { node, itemValue } = defineProps<{
  node: FormKitNode;
  itemValue: Record<string, unknown>;
}>();

const modal = ref<InstanceType<typeof VModal> | null>(null);
const formRef = ref<InstanceType<typeof ArrayItemForm> | null>(null);

const emit = defineEmits<{
  (event: "close"): void;
  (event: "submit", data: Record<string, unknown>): void;
}>();

const handleSubmit = (data: Record<string, unknown>) => {
  emit("submit", data);
  modal.value?.close();
};
</script>
<template>
  <VModal
    ref="modal"
    :title="$t('core.formkit.array.item_modal.title')"
    :width="700"
    mount-to-body
    layer-closable
    :centered="false"
    @close="emit('close')"
  >
    <ArrayItemForm
      ref="formRef"
      :node="node"
      :item-value="itemValue"
      @submit="handleSubmit"
    />

    <template #footer>
      <VSpace>
        <VButton type="secondary" @click="formRef?.submit()">
          {{ $t("core.common.buttons.submit") }}
        </VButton>
        <VButton @click="modal?.close()">
          {{ $t("core.common.buttons.cancel_and_shortcut") }}
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>
