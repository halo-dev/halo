<script lang="ts" setup>
import { VButton } from "../button";
import { VSpace } from "../space";
import { Toast } from "./toast-manager";
import type { Type } from "./interface";

function handleShowToast(type: Type, content: string) {
  Toast[type](content);
}
</script>
<template>
  <Story title="Toast">
    <VSpace>
      <VButton type="primary" @click="handleShowToast('success', 'Hello Halo')">
        成功
      </VButton>
      <VButton
        type="secondary"
        @click="handleShowToast('info', '这是一个提示')"
      >
        提示
      </VButton>

      <VButton
        type="default"
        @click="handleShowToast('warning', '这是一个警告提示')"
      >
        警告
      </VButton>

      <VButton
        type="danger"
        @click="handleShowToast('error', '这是一个错误提示')"
      >
        错误
      </VButton>
    </VSpace>
  </Story>
</template>
