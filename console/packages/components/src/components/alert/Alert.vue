<script lang="ts" setup>
import type { Component } from "vue";
import { computed } from "vue";
import type { Type } from "./interface";
import {
  IconCheckboxCircle,
  IconClose,
  IconCloseCircle,
  IconErrorWarning,
  IconInformation,
} from "../../icons/icons";

const TypeIcons: Record<Type, Component> = {
  success: IconCheckboxCircle,
  info: IconInformation,
  default: IconInformation,
  warning: IconErrorWarning,
  error: IconCloseCircle,
};

const props = withDefaults(
  defineProps<{
    type?: Type;
    title?: string;
    description?: string;
    closable?: boolean;
  }>(),
  {
    type: "default",
    title: "",
    description: "",
    closable: true,
  }
);

const emit = defineEmits<{
  (event: "close"): void;
}>();

const classes = computed(() => {
  return [`alert-${props.type}`];
});

const handleClose = () => {
  emit("close");
};
</script>

<template>
  <div :class="classes" class="alert-wrapper">
    <div class="alert-header">
      <div class="alert-icon">
        <slot name="icon">
          <component :is="TypeIcons[type]" />
        </slot>
      </div>
      <div class="alert-title">
        {{ title }}
      </div>
      <div v-if="closable" class="alert-close" @click="handleClose">
        <IconClose />
      </div>
    </div>
    <div v-if="description || $slots.description" class="alert-description">
      <slot name="description">
        {{ description }}
      </slot>
    </div>
    <div v-if="$slots.actions" class="alert-actions">
      <slot name="actions" />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.alert-wrapper {
  @apply flex
  flex-col
  box-border
  border
  rounded-base;
  padding: 12px 16px;

  .alert-header {
    @apply flex;

    .alert-icon {
      @apply self-center
      mr-3
      text-lg;
    }

    .alert-title {
      @apply self-center
      mr-3
      flex-1
      font-medium
      text-sm;
    }

    .alert-close {
      @apply self-center
      cursor-pointer
      rounded-full
      p-0.5;

      &:hover {
        @apply transition-all
        bg-gray-300
        text-white;
      }
    }
  }

  .alert-description {
    @apply text-xs
    mt-2;
  }

  .alert-actions {
    @apply border-t
    mt-3
    pt-2;
  }

  &.alert-default {
    @apply bg-gray-50
    border-gray-300;

    .alert-icon,
    .alert-description {
      @apply text-gray-600;
    }

    .alert-close,
    .alert-title {
      @apply text-gray-700;
    }
  }

  &.alert-success {
    @apply bg-green-50
    border-green-300;

    .alert-icon,
    .alert-description {
      @apply text-green-600;
    }

    .alert-close,
    .alert-title {
      @apply text-green-700;
    }
  }

  &.alert-info {
    @apply bg-sky-50
    border-sky-300;

    .alert-icon,
    .alert-description {
      @apply text-sky-600;
    }

    .alert-close,
    .alert-title {
      @apply text-sky-700;
    }
  }

  &.alert-warning {
    @apply bg-orange-50
    border-orange-300;

    .alert-icon,
    .alert-description {
      @apply text-orange-600;
    }

    .alert-close,
    .alert-title {
      @apply text-orange-700;
    }
  }

  &.alert-error {
    @apply bg-red-50
    border-red-300;

    .alert-icon,
    .alert-description {
      @apply text-red-600;
    }

    .alert-close,
    .alert-title {
      @apply text-red-700;
    }
  }
}
</style>
