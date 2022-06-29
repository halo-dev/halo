<script lang="ts" setup>
import { VModal } from "@/components/modal";
import { VSpace } from "@/components/space";
import { VButton } from "@/components/button";
import {
  IconCheckboxCircle,
  IconClose,
  IconErrorWarning,
  IconForbidLine,
  IconInformation,
} from "@/icons/icons";
import type { PropType } from "vue";
import { computed, ref } from "vue";
import type { Type } from "@/components/dialog/interface";

const props = defineProps({
  type: {
    type: String as PropType<Type>,
    default: "info",
  },
  title: {
    type: String,
    default: "提示",
  },
  description: {
    type: String,
    default: "",
  },
  confirmText: {
    type: String,
    default: "确定",
  },
  cancelText: {
    type: String,
    default: "取消",
  },
  visible: {
    type: Boolean,
    default: false,
  },
  onConfirm: {
    type: Function as PropType<() => void>,
  },
  onCancel: {
    type: Function as PropType<() => void>,
  },
});

const emit = defineEmits(["update:visible", "close"]);

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
    await props.onConfirm();
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
  <VModal :visible="visible" :width="450" @close="handleCancel()">
    <div class="flex justify-between items-start py-2 mb-2">
      <div>
        <div
          :class="`ring-${icon.color}-100`"
          class="inline-flex rounded-full bg-teal-50 p-1.5 ring-4"
        >
          <component
            :is="icon.icon"
            :class="`text-${icon.color}-500`"
            class="w-5 h-5"
          ></component>
        </div>
      </div>
      <div>
        <IconClose class="cursor-pointer" @click="handleCancel" />
      </div>
    </div>
    <div class="flex items-center gap-4">
      <div class="flex-1 flex flex-col items-stretch gap-2">
        <div class="text-base text-gray-900 font-bold">{{ title }}</div>
        <div class="text-sm text-gray-700">{{ description }}</div>
      </div>
    </div>
    <template #footer>
      <VSpace>
        <VButton :loading="loading" type="secondary" @click="handleConfirm">
          {{ confirmText }}
        </VButton>
        <VButton @click="handleCancel">{{ cancelText }}</VButton>
      </VSpace>
    </template>
  </VModal>
</template>
