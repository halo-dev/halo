<script lang="ts" setup>
import type { Type } from "./interface";
import { computed, onMounted, ref, watchEffect } from "vue";
import {
  IconCheckboxCircle,
  IconErrorWarning,
  IconForbidLine,
  IconInformation,
  IconClose,
} from "@/icons/icons";

const props = withDefaults(
  defineProps<{
    type?: Type;
    content?: string;
    duration?: number;
    closable?: boolean;
    frozenOnHover?: boolean;
    count?: 0;
    onClose?: () => void;
  }>(),
  {
    type: "success",
    content: "",
    duration: 3000,
    closable: true,
    frozenOnHover: true,
    count: 0,
    onClose: undefined,
  }
);

const timer = ref();

const emit = defineEmits<{
  (event: "close"): void;
}>();

const icons = {
  success: {
    icon: IconCheckboxCircle,
    color: "text-green-500",
  },
  info: {
    icon: IconInformation,
    color: "text-sky-500",
  },
  warning: {
    icon: IconErrorWarning,
    color: "text-orange-500",
  },
  error: {
    icon: IconForbidLine,
    color: "text-red-500",
  },
};

const icon = computed(() => icons[props.type]);

const createTimer = () => {
  if (props.duration < 0) return;
  timer.value = setTimeout(() => {
    close();
  }, props.duration);
};

const clearTimer = () => {
  clearTimeout(timer.value);
};

const close = () => {
  emit("close");
};

const handleMouseEnter = () => {
  if (!props.frozenOnHover) {
    return;
  }
  clearTimer();
};

const handleMouseLeave = () => {
  if (!props.frozenOnHover) {
    return;
  }
  createTimer();
};

watchEffect(() => {
  if (props.count > 0) {
    clearTimer();
    createTimer();
  }
});

onMounted(createTimer);

defineExpose({ close });
</script>

<template>
  <transition
    appear
    enter-active-class="transform ease-out duration-300 transition"
    enter-from-class="translate-x-0 -translate-y-2"
    enter-to-class="translate-y-0"
    leave-active-class="transition ease-in duration-100"
    leave-from-class="opacity-100"
    leave-to-class="opacity-0"
  >
    <div
      class="toast-wrapper"
      @mouseenter="handleMouseEnter"
      @mouseleave="handleMouseLeave"
    >
      <div class="toast-body">
        <div class="toast-icon">
          <component :is="icon.icon" :class="[icon.color]" />
        </div>
        <div class="toast-content">
          <div class="toast-description">
            <slot>{{ content }}</slot>
          </div>
        </div>
        <div v-if="closable" class="toast-control">
          <IconClose class="" @click="close" />
        </div>
      </div>
      <div v-if="count" class="toast-count">
        <span>{{ count }}</span>
      </div>
    </div>
  </transition>
</template>
<style lang="scss">
.toast-container {
  @apply fixed pointer-events-none flex z-[9999] flex-col box-border transition-all w-full left-0 top-0 items-center justify-center p-4 gap-3;

  .toast-wrapper {
    @apply inline-block max-w-xs pointer-events-auto relative;
  }

  .toast-body {
    @apply cursor-pointer flex items-center px-2.5 py-2 overflow-hidden break-all bg-white shadow hover:shadow-md transition-all rounded gap-2;
  }

  .toast-content {
    @apply text-sm flex flex-col gap-1;
  }

  .toast-description {
    @apply text-gray-800;
  }

  .toast-control {
    @apply text-gray-600 hover:text-gray-900 transition-all cursor-pointer rounded-full hover:bg-gray-100 p-0.5;
  }

  .toast-count {
    @apply bg-red-500 rounded-full absolute -right-1 -top-1 w-4 h-4 flex items-center justify-center;

    span {
      @apply text-[0.7rem] text-white;
    }
  }
}
</style>
