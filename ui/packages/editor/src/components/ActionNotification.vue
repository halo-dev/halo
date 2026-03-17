<script lang="ts" setup>
import {
  IconCheckboxCircle,
  IconClose,
  IconCloseCircle,
  IconErrorWarning,
  IconInformation,
} from "@halo-dev/components";
import { computed } from "vue";

export type NotificationType = "success" | "info" | "warning" | "error";

const props = withDefaults(
  defineProps<{
    type?: NotificationType;
    title?: string;
    message?: string;
    closable?: boolean;
  }>(),
  {
    type: "info",
    title: "",
    message: "",
    closable: true,
  }
);

const emit = defineEmits<{
  (event: "close"): void;
}>();

const TypeIcons = {
  success: IconCheckboxCircle,
  info: IconInformation,
  warning: IconErrorWarning,
  error: IconCloseCircle,
};

const classes = computed(() => [`notification-${props.type}`]);
</script>

<template>
  <div :class="classes" class="action-notification">
    <div class="notification-header">
      <div class="notification-icon">
        <component :is="TypeIcons[type]" />
      </div>
      <div class="notification-content">
        <div v-if="title" class="notification-title">{{ title }}</div>
        <div v-if="message" class="notification-message">{{ message }}</div>
      </div>
      <div v-if="closable" class="notification-close" @click="emit('close')">
        <IconClose />
      </div>
    </div>
    <div v-if="$slots.actions" class="notification-actions">
      <slot name="actions" />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.action-notification {
  @apply box-border flex flex-col rounded-base border shadow-lg;
  padding: 12px 16px;
  min-width: 320px;
  max-width: 480px;
  background: white;

  .notification-header {
    @apply flex items-start gap-3;

    .notification-icon {
      @apply mt-0.5 flex-shrink-0 text-lg;
    }

    .notification-content {
      @apply flex-1;

      .notification-title {
        @apply text-sm font-medium;
      }

      .notification-message {
        @apply mt-1 text-xs opacity-90;
      }
    }

    .notification-close {
      @apply flex-shrink-0 cursor-pointer rounded-full p-0.5;

      &:hover {
        @apply bg-gray-200 transition-all;
      }
    }
  }

  .notification-actions {
    @apply mt-3 flex justify-end gap-2 border-t pt-3;
  }

  &.notification-info {
    @apply border-sky-300 bg-sky-50;

    .notification-icon,
    .notification-content {
      @apply text-sky-700;
    }
  }

  &.notification-success {
    @apply border-green-300 bg-green-50;

    .notification-icon,
    .notification-content {
      @apply text-green-700;
    }
  }

  &.notification-warning {
    @apply border-orange-300 bg-orange-50;

    .notification-icon,
    .notification-content {
      @apply text-orange-700;
    }
  }

  &.notification-error {
    @apply border-red-300 bg-red-50;

    .notification-icon,
    .notification-content {
      @apply text-red-700;
    }
  }
}
</style>
