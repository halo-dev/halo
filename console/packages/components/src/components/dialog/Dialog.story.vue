<script lang="ts" setup>
import { VButton } from "@/components/button";
import { Toast } from "../toast";
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
      Toast.info("已删除");
    },
    onCancel: () => {
      Toast.info("已取消");
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
