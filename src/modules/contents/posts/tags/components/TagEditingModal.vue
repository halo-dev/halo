<script lang="ts" setup>
import { VButton, VModal, VSpace } from "@halo-dev/components";

withDefaults(
  defineProps<{
    visible: boolean;
    tag: unknown | null;
  }>(),
  {
    visible: false,
    tag: undefined,
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
    :width="600"
    title="编辑文章标签"
    @update:visible="onVisibleChange"
  >
    <FormKit id="tag-form" type="form">
      <FormKit label="名称" type="text" validation="required"></FormKit>
      <FormKit
        help="通常作为标签访问地址标识"
        label="别名"
        type="text"
        validation="required"
      ></FormKit>
      <FormKit help="需要主题适配以支持" label="颜色" type="color"></FormKit>
      <FormKit help="需要主题适配以支持" label="封面图" type="text"></FormKit>
    </FormKit>
    <template #footer>
      <VSpace>
        <VButton type="secondary" @click="$formkit.submit('tag-form')">
          提交 ⌘ + ↵
        </VButton>
        <VButton @click="onVisibleChange(false)">取消 Esc</VButton>
      </VSpace>
    </template>
  </VModal>
</template>
