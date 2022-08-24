<script lang="ts" setup>
import { VButton, VModal, VSpace } from "@halo-dev/components";

withDefaults(
  defineProps<{
    visible: boolean;
    group: object | null;
  }>(),
  {
    visible: false,
    group: null,
  }
);

const emit = defineEmits<{
  (event: "update:visible", visible: boolean): void;
  (event: "close"): void;
}>();

const onVisibleChange = (visible: boolean) => {
  emit("update:visible", visible);
  if (!visible) {
    emit("close");
  }
};
</script>
<template>
  <VModal
    :visible="visible"
    :width="500"
    title="附件分组"
    @update:visible="onVisibleChange"
  >
    <FormKit id="attachment-group-form" type="form">
      <FormKit label="名称" type="text" validation="required"></FormKit>
    </FormKit>
    <template #footer>
      <VSpace>
        <VButton
          type="secondary"
          @click="$formkit.submit('attachment-group-form')"
        >
          保存 ⌘ + ↵
        </VButton>
        <VButton @click="onVisibleChange(false)">取消 Esc</VButton>
      </VSpace>
    </template>
  </VModal>
</template>
