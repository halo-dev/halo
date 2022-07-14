<script lang="ts" setup>
import type { Component, PropType } from "vue";
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

const props = defineProps({
  type: {
    type: String as PropType<Type>,
    default: "default",
  },
  title: {
    type: String,
  },
  description: {
    type: String,
  },
  closable: {
    type: Boolean,
    default: true,
  },
});

const emit = defineEmits(["close"]);

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
  @apply flex;
  @apply flex-col;
  @apply box-border;
  @apply border;
  @apply rounded-base;
  padding: 12px 16px;

  .alert-header {
    @apply flex;

    .alert-icon {
      @apply self-center;
      @apply mr-3;
      @apply text-lg;
    }

    .alert-title {
      @apply self-center;
      @apply mr-3;
      @apply flex-1;
      @apply font-medium;
      @apply text-base;
    }

    .alert-close {
      @apply self-center;
      @apply cursor-pointer;
      @apply rounded-full;
      @apply p-0.5;

      &:hover {
        @apply transition-all;
        @apply bg-gray-300;
        @apply text-white;
      }
    }
  }

  .alert-description {
    @apply text-sm;
    @apply mt-2;
  }

  .alert-actions {
    @apply border-t;
    @apply mt-3 pt-2;
  }

  &.alert-default {
    @apply bg-gray-50;
    @apply border-gray-300;

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
    @apply bg-green-50;
    @apply border-green-300;

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
    @apply bg-sky-50;
    @apply border-sky-300;

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
    @apply bg-orange-50;
    @apply border-orange-300;

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
    @apply bg-red-50;
    @apply border-red-300;

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
