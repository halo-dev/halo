<template>
  <FilledLayout>
    <VModal v-model:visible="visible" title="登录">
      <div class="flex justify-between">
        <div>
          <VButton type="secondary" @click="visible = false">关闭</VButton>
        </div>
        <div>
          <VButton type="secondary" @click="visible = true">打开</VButton>
        </div>
      </div>
    </VModal>
    <VButton type="secondary" @click="visible = true">打开</VButton>
  </FilledLayout>
</template>
<script lang="ts" setup>
import { FilledLayout } from "@/layouts";
import { VModal } from "@/components/base/modal";
import { VButton } from "@/components/base/button";
import { ref } from "vue";
const visible = ref(false);
</script>
