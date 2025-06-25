<script lang="ts" setup>
import type { DialogProps, Type } from "@/components/dialog/interface";
import { markRaw, ref, type Component, type Raw } from "vue";
import {
  IconCheckboxCircle,
  IconClose,
  IconErrorWarning,
  IconForbidLine,
  IconInformation,
} from "../../icons/icons";
import { VButton } from "../button";
import { VModal } from "../modal";

const props = withDefaults(defineProps<DialogProps>(), {
  type: "info",
  title: "提示",
  description: "",
  confirmText: "确定",
  confirmType: "primary",
  showCancel: true,
  cancelText: "取消",
  visible: false,
  onConfirm: () => {
    return;
  },
  onCancel: () => {
    return;
  },
});

const emit = defineEmits<{
  (event: "update:visible", visible: boolean): void;
  (event: "close"): void;
}>();

const icons: Record<Type, { icon: Raw<Component>; color: string }> = {
  success: {
    icon: markRaw(IconCheckboxCircle),
    color: "green",
  },
  info: {
    icon: markRaw(IconInformation),
    color: "blue",
  },
  warning: {
    icon: markRaw(IconErrorWarning),
    color: "orange",
  },
  error: {
    icon: markRaw(IconForbidLine),
    color: "red",
  },
};

const loading = ref(false);

const handleCancel = () => {
  if (props.onCancel) {
    props.onCancel();
  }
  handleClose();
};

const handleConfirm = async () => {
  if (props.onConfirm) {
    loading.value = true;

    try {
      await props.onConfirm();
    } catch (error) {
      console.error("Failed to execute onConfirm: ", error);
    } finally {
      loading.value = false;
    }
  }
  handleClose();
};

const handleClose = () => {
  loading.value = false;
  emit("update:visible", false);
  emit("close");
};
</script>
<template>
  <VModal
    :visible="visible"
    :width="450"
    :layer-closable="false"
    :data-unique-id="uniqueId"
    @close="handleCancel()"
  >
    <div class="mb-2 flex items-start justify-between py-2">
      <div class="flex flex-row items-center gap-3">
        <component
          :is="icons[type].icon"
          :class="`text-${icons[type].color}-500`"
          class="h-6 w-6 flex-none"
        ></component>
        <div class="text-base font-bold text-gray-900">{{ title }}</div>
      </div>
      <div>
        <IconClose class="cursor-pointer" @click="handleCancel" />
      </div>
    </div>
    <div class="flex items-center gap-4">
      <div class="flex flex-1 items-stretch">
        <div class="break-all text-sm text-gray-700">{{ description }}</div>
      </div>
    </div>
    <template #footer>
      <div class="flex flex-row flex-wrap gap-3">
        <VButton :loading="loading" :type="confirmType" @click="handleConfirm">
          {{ confirmText }}
        </VButton>
        <VButton v-if="showCancel" @click="handleCancel">
          {{ cancelText }}
        </VButton>
      </div>
    </template>
  </VModal>
</template>
