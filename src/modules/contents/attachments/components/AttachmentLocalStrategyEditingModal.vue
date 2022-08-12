<script lang="ts" setup>
import { VButton, VModal, VSpace } from "@halo-dev/components";

defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(["update:visible", "close"]);

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
    :width="600"
    title="本地存储策略编辑"
    @update:visible="onVisibleChange"
  >
    <FormKit id="local-strategy-form" type="form">
      <FormKit label="名称" type="text" validation="required"></FormKit>
      <FormKit label="存储位置" type="text" validation="required"></FormKit>
      <FormKit
        help="使用半角逗号分隔"
        label="允许上传的文件类型"
        type="textarea"
        value="jpg,png,gif"
      ></FormKit>
    </FormKit>

    <template #footer>
      <VSpace>
        <VButton
          type="secondary"
          @click="$formkit.submit('local-strategy-form')"
        >
          保存 ⌘ + ↵
        </VButton>
        <VButton @click="onVisibleChange(false)">取消 Esc</VButton>
      </VSpace>
    </template>
  </VModal>
</template>
