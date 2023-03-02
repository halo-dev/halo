<script lang="ts" setup>
import { IconArrowLeft, IconArrowRight } from "@/icons/icons";
import { VButton } from "../../components/button";
import { VModal } from "../../components/modal";
import { VSpace } from "../../components/space";

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
  <Story :init-state="initState" title="Modal" class="h-full">
    <template #default="{ state }">
      <VButton type="secondary" @click="state.visible = true">打开</VButton>
      <VModal
        v-model:visible="state.visible"
        :fullscreen="state.fullscreen"
        :title="state.title"
        :width="state.width"
        :mount-to-body="true"
      >
        <template #actions>
          <span>
            <IconArrowLeft role="button" />
          </span>

          <span>
            <IconArrowRight role="button" />
          </span>
        </template>
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
  </Story>
</template>
