<script lang="ts" setup>
import { VDialog } from "./index";
import { provide, ref } from "vue";
import type { useDialogOptions } from "./interface";
import { DialogProviderProvideKey } from "@/components/dialog/interface";

const options = ref<useDialogOptions>({
  visible: false,
  title: "",
});

provide(DialogProviderProvideKey, options);
</script>
<template>
  <slot />
  <VDialog
    v-model:visible="options.visible"
    :cancel-text="options.cancelText"
    :confirm-text="options.confirmText"
    :description="options.description"
    :onCancel="options.onCancel"
    :onConfirm="options.onConfirm"
    :title="options.title"
    :type="options.type"
  ></VDialog>
</template>
