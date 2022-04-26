<script lang="ts" setup>
import { VButton } from "@/components/base/button";
import { VModal } from "@/components/base/modal";

function initState() {
  return {
    visible: false,
  };
}
</script>
<template>
  <Story title="Modal" :init-state="initState">
    <template #default="{ state }">
      <VButton type="secondary" @click="state.visible = true">打开</VButton>
      <VModal v-model:visible="state.visible" title="测试">
        Hello World
        <VButton type="secondary" @click="state.visible = false">关闭</VButton>
      </VModal>
    </template>
  </Story>
</template>
