<script lang="ts" setup>
import { VButton } from "@/components/button";
import { Dialog } from "@/components/dialog";

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

function handleConfirm() {
  Dialog.success({
    title: "Hello",
    description: "Hello World",
    onConfirm: async () => {
      await new Promise((resolve) =>
        setTimeout(() => {
          console.log("Timeout");
          resolve("");
        }, 1000)
      );
    },
  });
}
</script>
<template>
  <Story :init-state="initState" title="Dialog">
    <template #default>
      <VButton @click="handleConfirm">确定</VButton>
    </template>
  </Story>
</template>
