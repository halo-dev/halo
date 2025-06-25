<script lang="ts" setup>
import {
  IconCheckboxCircle,
  IconClose,
  IconErrorWarning,
  IconForbidLine,
  IconInformation,
} from "@/icons/icons";
import {
  markRaw,
  onMounted,
  ref,
  watchEffect,
  type Component,
  type Raw,
} from "vue";
import type { Type } from "./interface";

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

const icons: Record<Type, { icon: Raw<Component>; color: string }> = {
  success: {
    icon: markRaw(IconCheckboxCircle),
    color: "text-green-500",
  },
  info: {
    icon: markRaw(IconInformation),
    color: "text-sky-500",
  },
  warning: {
    icon: markRaw(IconErrorWarning),
    color: "text-orange-500",
  },
  error: {
    icon: markRaw(IconForbidLine),
    color: "text-red-500",
  },
};

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
          <component :is="icons[type].icon" :class="icons[type].color" />
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
  @apply pointer-events-none fixed left-0 top-0 z-[9999] box-border flex w-full flex-col items-center justify-center gap-3 p-4 transition-all;

  .toast-wrapper {
    @apply pointer-events-auto relative inline-block max-w-xs;
  }

  .toast-body {
    @apply flex cursor-pointer items-center gap-2 overflow-hidden break-all rounded bg-white px-2.5 py-2 shadow transition-all hover:shadow-md;
  }

  .toast-content {
    @apply flex flex-col gap-1 text-sm;
  }

  .toast-description {
    @apply text-gray-800;
  }

  .toast-control {
    @apply cursor-pointer rounded-full p-0.5 text-gray-600 transition-all hover:bg-gray-100 hover:text-gray-900;
  }

  .toast-count {
    @apply absolute -right-1 -top-1 flex h-4 w-4 items-center justify-center rounded-full bg-red-500;

    span {
      @apply text-[0.7rem] text-white;
    }
  }
}
</style>
