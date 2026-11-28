<script lang="ts" setup>
import { ToastAction, ToastClose, type ToastObject } from "reka-ui";
import { computed, markRaw, type Component, type Raw } from "vue";
import {
  IconCheckboxCircle,
  IconClose,
  IconErrorWarning,
  IconForbidLine,
  IconInformation,
  IconRefreshLine,
} from "@/icons/icons";
import { useComponentsI18n } from "../../locales";
import type { ToastItemData } from "./internal";
import type { ToastType } from "./types";

const props = defineProps<{
  toast: ToastObject<ToastItemData>;
}>();

const { t } = useComponentsI18n();

const icons: Record<ToastType, { icon: Raw<Component>; color: string }> = {
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

const data = computed(() => props.toast.data);

const icon = computed<{ icon: Raw<Component>; color: string; spin: boolean }>(
  () => {
    if (props.toast.status === "loading") {
      return {
        icon: markRaw(IconRefreshLine),
        color: "text-sky-500",
        spin: true,
      };
    }

    if (data.value?.icon) {
      return {
        icon: markRaw(data.value.icon),
        color: icons[data.value.type].color,
        spin: false,
      };
    }

    const entry = icons[data.value?.type ?? "success"];
    return { ...entry, spin: false };
  }
);
</script>

<template>
  <div class="toast-body">
    <div class="toast-icon">
      <component
        :is="icon.icon"
        :class="[icon.color, { 'animate-spin': icon.spin }]"
        aria-hidden="true"
      />
    </div>
    <div class="toast-content">
      <div v-if="data?.title" class="toast-title">{{ data.title }}</div>
      <div v-if="data?.content" class="toast-description">
        {{ data.content }}
      </div>
      <div v-if="toast.actionProps" class="toast-actions">
        <ToastAction class="toast-action" />
      </div>
    </div>
    <ToastClose
      v-if="data?.closable"
      :aria-label="t('common.close')"
      class="toast-control"
    >
      <IconClose aria-hidden="true" />
    </ToastClose>
  </div>
  <div v-if="data?.count" class="toast-count" aria-hidden="true">
    <span>{{ data.count }}</span>
  </div>
</template>

<style lang="scss">
@keyframes toast-enter {
  from {
    opacity: 0;
    translate: 0 calc(var(--toast-y) - 8px);
  }

  to {
    opacity: 1;
    translate: 0 var(--toast-y);
  }
}

@keyframes toast-leave {
  from {
    opacity: 1;
    translate: 0 var(--toast-y);
  }

  to {
    opacity: 0;
    translate: 0 calc(var(--toast-y) - 6px);
  }
}

.toast-wrapper {
  @apply pointer-events-auto absolute w-fit max-w-full list-none;
  --toast-gap: 10px;
  --toast-y: calc(
    var(--reka-toast-offset-y, 0px) + var(--reka-toast-index, 0) *
      var(--toast-gap)
  );
  left: 0;
  right: 0;
  top: 0;
  margin-inline: auto;
  z-index: calc(100 - var(--reka-toast-index, 0));
  translate: 0 var(--toast-y);
  animation: toast-enter 200ms cubic-bezier(0.16, 1, 0.3, 1);
  transition:
    translate 200ms cubic-bezier(0.16, 1, 0.3, 1),
    opacity 160ms ease-out;

  &[data-state="closed"] {
    animation: toast-leave 160ms ease-in forwards;
  }

  &[data-swipe="move"] {
    transform: translateX(var(--reka-toast-swipe-move-x, 0));
    transition: none;
  }

  &[data-swipe="cancel"] {
    transform: translateX(0);
    transition: transform 150ms ease-out;
  }

  &[data-swipe="end"] {
    transform: translateX(var(--reka-toast-swipe-end-x, 0));
    transition: transform 100ms ease-out;
  }
}

.toast-body {
  @apply flex items-start gap-2.5 rounded-lg bg-white px-3.5 py-2.5 text-sm shadow-lg ring-1 ring-black/5;
}

.toast-icon {
  @apply flex shrink-0 items-center;

  svg {
    @apply h-5 w-5;
  }
}

.toast-content {
  @apply flex min-w-0 flex-1 flex-col gap-0.5;
}

.toast-title {
  @apply font-medium text-gray-900;
}

.toast-description {
  @apply break-words text-gray-700;
}

.toast-actions {
  @apply mt-1.5;
}

.toast-action {
  @apply cursor-pointer rounded px-2 py-1 text-sm font-medium text-primary transition-colors hover:bg-gray-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary;
}

.toast-control {
  @apply -my-0.5 -mr-1 flex shrink-0 cursor-pointer items-center justify-center rounded p-1 text-gray-500 transition-colors hover:bg-gray-100 hover:text-gray-900 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary;

  svg {
    @apply h-4 w-4;
  }
}

.toast-count {
  @apply absolute -right-1.5 -top-1.5 flex h-4 min-w-[1rem] items-center justify-center rounded-full bg-red-500 px-1;

  span {
    @apply text-[0.7rem] tabular-nums leading-none text-white;
  }
}

@media (prefers-reduced-motion: reduce) {
  .toast-wrapper {
    animation: none !important;
    transition: none !important;
  }
}
</style>
