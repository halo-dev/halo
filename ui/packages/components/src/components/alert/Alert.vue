<script lang="ts" setup>
import type { FunctionalComponent, Raw, SVGAttributes } from "vue";
import { computed, markRaw } from "vue";
import {
  IconCheckboxCircle,
  IconClose,
  IconCloseCircle,
  IconErrorWarning,
  IconInformation,
} from "../../icons/icons";
import type { Type } from "./interface";

const TypeIcons: Record<Type, Raw<FunctionalComponent<SVGAttributes>>> = {
  success: markRaw(IconCheckboxCircle),
  info: markRaw(IconInformation),
  default: markRaw(IconInformation),
  warning: markRaw(IconErrorWarning),
  error: markRaw(IconCloseCircle),
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
      <div v-if="title || $slots.title" class="alert-title">
        <slot name="title">
          {{ title }}
        </slot>
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
  @apply box-border flex flex-col rounded-base border;
  padding: 12px 16px;

  .alert-header {
    @apply flex;

    .alert-icon {
      @apply mr-3 self-center text-lg;
    }

    .alert-title {
      @apply mr-3 flex-1 self-center text-sm font-medium;
    }

    .alert-close {
      @apply cursor-pointer self-center rounded-full p-0.5;

      &:hover {
        @apply bg-gray-300 text-white transition-all;
      }
    }
  }

  .alert-description {
    @apply mt-2 text-xs;
  }

  .alert-actions {
    @apply mt-3 border-t pt-2;
  }

  &.alert-default {
    @apply border-gray-300 bg-gray-50;

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
    @apply border-green-300 bg-green-50;

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
    @apply border-sky-300 bg-sky-50;

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
    @apply border-orange-300 bg-orange-50;

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
    @apply border-red-300 bg-red-50;

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
