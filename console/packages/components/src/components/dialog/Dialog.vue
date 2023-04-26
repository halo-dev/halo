<script lang="ts" setup>
import { VModal } from "../modal";
import { VButton } from "../button";
import {
  IconCheckboxCircle,
  IconClose,
  IconErrorWarning,
  IconForbidLine,
  IconInformation,
} from "../../icons/icons";
import { computed, ref } from "vue";
import type { Type } from "@/components/dialog/interface";
import type { Type as ButtonType } from "@/components/button/interface";

const props = withDefaults(
  defineProps<{
    type?: Type;
    title?: string;
    description?: string;
    confirmText?: string;
    confirmType?: ButtonType;
    cancelText?: string;
    visible?: boolean;
    onConfirm?: () => void;
    onCancel?: () => void;
  }>(),
  {
    type: "info",
    title: "提示",
    description: "",
    confirmText: "确定",
    confirmType: "primary",
    cancelText: "取消",
    visible: false,
    onConfirm: () => {
      return;
    },
    onCancel: () => {
      return;
    },
  }
);

const emit = defineEmits<{
  (event: "update:visible", visible: boolean): void;
  (event: "close"): void;
}>();

const icons = {
  success: {
    icon: IconCheckboxCircle,
    color: "green",
  },
  info: {
    icon: IconInformation,
    color: "blue",
  },
  warning: {
    icon: IconErrorWarning,
    color: "orange",
  },
  error: {
    icon: IconForbidLine,
    color: "red",
  },
};
const icon = computed(() => icons[props.type]);

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
    :layer-closable="true"
    @close="handleCancel()"
  >
    <div class="flex justify-between items-start py-2 mb-2">
      <div class="flex flex-row items-center gap-3">
        <component
          :is="icon.icon"
          :class="`text-${icon.color}-500`"
          class="w-6 h-6"
        ></component>
        <div class="text-base text-gray-900 font-bold">{{ title }}</div>
      </div>
      <div>
        <IconClose class="cursor-pointer" @click="handleCancel" />
      </div>
    </div>
    <div class="flex items-center gap-4">
      <div class="flex-1 flex items-stretch">
        <div class="text-sm text-gray-700">{{ description }}</div>
      </div>
    </div>
    <template #footer>
      <div class="flex flex-col sm:flex-row gap-[10px]">
        <VButton :loading="loading" :type="confirmType" @click="handleConfirm">
          {{ confirmText }}
        </VButton>
        <VButton @click="handleCancel">{{ cancelText }}</VButton>
      </div>
    </template>
  </VModal>
</template>
