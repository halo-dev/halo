<script lang="ts" setup>
import { ToastProvider, type GlobalToastManager } from "reka-ui";
import { useComponentsI18n } from "../../locales";
import type { ToastItemData } from "./internal";
import { toastConfig } from "./toast-state";
import ToastList from "./ToastList.vue";

defineProps<{
  manager: GlobalToastManager<ToastItemData>;
}>();

const { t } = useComponentsI18n();
</script>

<template>
  <ToastProvider
    :toast-manager="manager"
    :label="t('toast.notification')"
    :duration="toastConfig.duration"
    swipe-direction="right"
    :swipe-threshold="60"
  >
    <ToastList />
  </ToastProvider>
</template>
