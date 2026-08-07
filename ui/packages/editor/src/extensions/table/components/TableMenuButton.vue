<script setup lang="ts">
import type { Component } from "vue";
import MdiCheck from "~icons/mdi/check";

type ButtonVariant = "toolbar" | "toolbar-label" | "menu" | "segment";

defineProps<{
  label: string;
  icon?: Component;
  disabled?: boolean;
  active?: boolean;
  iconOnly?: boolean;
  hideLabelOnMobile?: boolean;
  variant?: ButtonVariant;
  hasPopup?: boolean;
  expanded?: boolean;
}>();

const variantClasses: Record<ButtonVariant, string> = {
  toolbar: "size-8 flex-none justify-center p-0",
  "toolbar-label": "h-8 flex-none justify-center px-2.5 py-0",
  menu: "w-full justify-start px-1.5 py-1 text-left",
  segment:
    "!min-h-[1.875rem] min-w-0 grow shrink basis-0 justify-center rounded-md border border-transparent px-2 py-[0.3125rem] font-medium",
};

const emit = defineEmits<{
  activate: [];
}>();
</script>

<template>
  <button
    type="button"
    class="table-menu-button inline-flex min-h-8 items-center gap-[0.4375rem] rounded-md text-[0.8125rem] leading-[1.125rem] transition-[background-color,color,box-shadow] duration-[150ms,150ms,150ms] focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 disabled:cursor-not-allowed disabled:opacity-40"
    :class="[
      variantClasses[variant ?? 'menu'],
      {
        'table-menu-button-active': active,
        '!basis-auto !px-1': variant === 'segment' && !iconOnly,
        [`table-menu-button-${variant ?? 'menu'}`]: true,
      },
    ]"
    :disabled="disabled"
    :aria-label="label"
    :aria-pressed="active === undefined ? undefined : active"
    :aria-haspopup="hasPopup ? 'true' : undefined"
    :aria-expanded="hasPopup ? expanded : undefined"
    :data-state="active === undefined ? undefined : active ? 'on' : 'off'"
    :data-variant="variant ?? 'menu'"
    :data-icon-only="iconOnly ? 'true' : undefined"
    :title="label"
    @click="emit('activate')"
  >
    <span
      v-if="icon"
      class="table-menu-button-icon-frame inline-flex flex-none items-center justify-center rounded-[0.3125rem]"
      :class="(variant ?? 'menu') === 'menu' ? 'size-7' : 'size-[1.125rem]'"
      aria-hidden="true"
    >
      <component :is="icon" class="size-4 flex-none" />
    </span>
    <span
      class="table-menu-button-label min-w-0 truncate"
      :class="{
        'sr-only': variant === 'toolbar' || iconOnly,
        'max-[480px]:sr-only max-[480px]:m-0 max-[480px]:[clip-path:inset(50%)]':
          hideLabelOnMobile,
      }"
    >
      {{ label }}
    </span>
    <MdiCheck
      v-if="active && variant === 'menu'"
      class="table-menu-button-check ms-auto size-4 flex-none"
      aria-hidden="true"
    />
  </button>
</template>

<style scoped>
.table-menu-button {
  color: var(--halo-table-menu-text, rgb(75 85 99));
}

.table-menu-button-segment {
  color: var(--halo-table-menu-muted, rgb(107 114 128));
}

.table-menu-button:hover:not(:disabled),
.table-menu-button-active {
  background: var(--halo-table-menu-hover, rgb(243 244 246));
  color: var(--halo-table-menu-text-active, rgb(17 24 39));
}

.table-menu-button-segment:hover:not(:disabled) {
  background: var(--halo-table-menu-segment-hover, rgb(255 255 255 / 72%));
  color: var(--halo-table-menu-text-active, rgb(17 24 39));
}

.table-menu-button-segment.table-menu-button-active {
  border-color: var(--halo-table-menu-segment-active-border, rgb(209 213 219));
  background: var(--halo-table-menu-background, #fff);
  box-shadow:
    0 1px 2px rgb(15 23 42 / 8%),
    0 0 0 1px rgb(255 255 255 / 48%) inset;
  color: var(--halo-table-control-active, rgb(37 132 255));
}

.table-menu-button-menu:hover:not(:disabled) .table-menu-button-icon-frame,
.table-menu-button-menu.table-menu-button-active .table-menu-button-icon-frame {
  background: var(--halo-table-menu-background, rgb(255 255 255));
}

.table-menu-button:focus-visible {
  outline-color: var(--halo-table-control-active, rgb(37 132 255));
}

.table-menu-button-menu .table-menu-button-icon-frame {
  background: var(--halo-table-menu-hover, rgb(243 244 246));
}

.table-menu-button-check {
  color: var(--halo-table-control-active, rgb(37 132 255));
}
</style>
