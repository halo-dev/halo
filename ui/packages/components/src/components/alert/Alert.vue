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
import { useComponentsI18n } from "../../locales";
import type { AlertType } from "./types";

const TypeIcons: Record<AlertType, Raw<FunctionalComponent<SVGAttributes>>> = {
  success: markRaw(IconCheckboxCircle),
  info: markRaw(IconInformation),
  default: markRaw(IconInformation),
  warning: markRaw(IconErrorWarning),
  error: markRaw(IconCloseCircle),
};

const props = withDefaults(
  defineProps<{
    type?: AlertType;
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

const { t } = useComponentsI18n();

const classes = computed(() => {
  return [`alert-${props.type}`];
});

const handleClose = () => {
  emit("close");
};
</script>

<template>
  <div :class="classes" class="alert-wrapper">
    <div class="alert-icon">
      <slot name="icon">
        <component :is="TypeIcons[type]" aria-hidden="true" />
      </slot>
    </div>
    <div class="alert-content">
      <div v-if="title || $slots.title || closable" class="alert-header">
        <div v-if="title || $slots.title" class="alert-title">
          <slot name="title">
            {{ title }}
          </slot>
        </div>
        <div
          v-if="closable"
          :aria-label="t('common.close')"
          class="alert-close"
          role="button"
          tabindex="0"
          @click="handleClose"
          @keydown.enter.prevent="handleClose"
          @keydown.space.prevent="handleClose"
        >
          <IconClose aria-hidden="true" />
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
  </div>
</template>

<style lang="scss" scoped>
.alert-wrapper {
  @apply box-border flex items-start rounded-base border;
  padding: 8px 12px;

  .alert-icon {
    @apply mr-2 flex h-5 flex-shrink-0 items-center text-base;
  }

  .alert-content {
    @apply min-w-0 flex-1;
  }

  .alert-header {
    @apply flex items-start gap-2;

    .alert-title {
      @apply flex-1 text-sm font-semibold leading-5;
    }

    .alert-close {
      @apply ml-auto flex h-5 w-5 flex-shrink-0 cursor-pointer items-center justify-center rounded text-sm text-gray-400;

      &:hover {
        @apply text-gray-600 transition-colors;
      }
    }
  }

  .alert-description {
    @apply text-sm leading-5;
  }

  .alert-header + .alert-description {
    @apply mt-0.5;
  }

  .alert-actions {
    @apply mt-2 flex flex-wrap gap-2 border-t pt-2;
  }

  &.alert-default {
    @apply border-gray-200 bg-gray-50;

    .alert-icon,
    .alert-description,
    .alert-title {
      @apply text-gray-600;
    }
  }

  &.alert-success {
    @apply border-green-200 bg-green-50;

    .alert-icon,
    .alert-description,
    .alert-title {
      @apply text-green-600;
    }
  }

  &.alert-info {
    @apply border-sky-200 bg-sky-50;

    .alert-icon,
    .alert-description,
    .alert-title {
      @apply text-sky-600;
    }
  }

  &.alert-warning {
    @apply border-orange-200 bg-orange-50;

    .alert-icon,
    .alert-description,
    .alert-title {
      @apply text-orange-600;
    }
  }

  &.alert-error {
    @apply border-red-200 bg-red-50;

    .alert-icon,
    .alert-description,
    .alert-title {
      @apply text-red-600;
    }
  }
}
</style>
