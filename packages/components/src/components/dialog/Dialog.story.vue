<script lang="ts" setup>
import { VButton } from "@/components/button";
import { VDialog } from "@/components/dialog";

const initState = () => {
  return {
    visible: false,
    title: "提示",
    description: "是否确定删除这篇文章？",
    confirmText: "确定",
    cancelText: "取消",
    type: "info",
    onConfirm: () => {
      alert("已删除");
    },
    onCancel: () => {
      alert("已取消");
    },
  };
};
</script>
<template>
  <Story :init-state="initState" title="Dialog">
    <template #default="{ state }">
      <VButton type="danger" @click="state.visible = true">删除</VButton>
      <VDialog
        :cancel-text="state.cancelText"
        :confirm-text="state.confirmText"
        :description="state.description"
        :title="state.title"
        :type="state.type"
        :visible="state.visible"
      ></VDialog>
    </template>
  </Story>
</template>
