<script lang="ts" setup>
import { VButton } from "@/components/base/button";
import { VModal } from "@/components/base/modal";
import { VSpace } from "@/components/base/space";

function initState() {
  return {
    visible: false,
    fullscreen: false,
    width: 500,
    title: "测试",
  };
}
</script>
<template>
  <Story :init-state="initState" title="Modal">
    <template #default="{ state }">
      <VButton type="secondary" @click="state.visible = true">打开</VButton>
      <VModal
        v-model:visible="state.visible"
        :fullscreen="state.fullscreen"
        :title="state.title"
        :width="state.width"
      >
        <div class="flex flex-col">
          <img class="w-full" src="https://ryanc.cc/avatar" />
          <img class="w-full" src="https://ryanc.cc/avatar" />
          <img class="w-full" src="https://halo.run/logo" />
        </div>

        <template #footer>
          <VSpace>
            <VButton loading type="primary" @click="state.visible = false"
              >确定
            </VButton>
            <VButton @click="state.visible = false">取消</VButton>
          </VSpace>
        </template>
      </VModal>
    </template>
    <template #controls="{ state }">
      <HstCheckbox v-model="state.visible" title="Visible" />
      <HstText v-model="state.title" title="Title" />
      <HstNumber v-model="state.width" title="Width" />
      <HstCheckbox v-model="state.fullscreen" title="Fullscreen" />
    </template>
  </Story>
</template>
