<script lang="ts" setup>
import type { CSSProperties } from "vue";
import { computed, onBeforeUnmount, onMounted, onUpdated, ref } from "vue";
import { IconClose } from "../../icons/icons";
import { useComponentsI18n } from "../../locales";
import { vTooltip } from "../tooltip";
import type { TagSize, TagTheme, TagVariant } from "./types";

type TagStyle = CSSProperties & Record<`--${string}`, string>;

const props = withDefaults(
  defineProps<{
    theme?: TagTheme;
    size?: TagSize;
    variant?: TagVariant;
    rounded?: boolean;
    closable?: boolean;
    disabled?: boolean;
    clickable?: boolean;
    checkable?: boolean;
    checked?: boolean;
    defaultChecked?: boolean;
    maxWidth?: string;
    /**
     * Explicit tooltip content. When omitted, the full label is shown in a
     * tooltip only if it is truncated.
     */
    title?: string;
    /**
     * Custom background color. The foreground color is derived from its
     * luminance, falling back to the CSS `contrast-color()` function.
     */
    color?: string;
    /**
     * @deprecated Use the native `style` attribute or the `color` prop instead.
     */
    styles?: CSSProperties;
  }>(),
  {
    theme: "default",
    size: "sm",
    variant: "solid",
    rounded: false,
    closable: false,
    disabled: false,
    clickable: false,
    checkable: false,
    checked: undefined,
    defaultChecked: false,
    maxWidth: "12rem",
    color: undefined,
    title: undefined,
    styles: () => {
      return {};
    },
  }
);

const emit = defineEmits<{
  (event: "close", mouseEvent: MouseEvent): void;
  (event: "click", mouseEvent: MouseEvent): void;
  (event: "update:checked", checked: boolean): void;
  (event: "change", checked: boolean): void;
}>();

const { t } = useComponentsI18n();

const rootRef = ref<HTMLElement>();
const labelRef = ref<HTMLElement>();

const innerChecked = ref(props.defaultChecked);

const isChecked = computed(() => {
  return props.checked ?? innerChecked.value;
});
const isInteractive = computed(() => {
  return props.clickable || props.checkable;
});

const classes = computed(() => {
  return [
    `tag-${props.theme}`,
    `tag-${props.size}`,
    {
      "tag-soft": props.variant === "soft" && !props.color,
      "tag-rounded": props.rounded,
      "tag-interactive": isInteractive.value && !props.disabled,
      "tag-checked": props.checkable && isChecked.value,
      "tag-disabled": props.disabled,
      "tag-custom": Boolean(props.color),
    },
  ];
});

const rootStyle = computed<TagStyle>(() => {
  const style: TagStyle = {
    ...(props.styles as TagStyle),
    "--tag-max-width": props.maxWidth,
  };

  if (props.color) {
    style["--tag-custom-bg"] = props.color;
    const foreground = getContrastColor(props.color);
    if (foreground) {
      style["--tag-custom-fg"] = foreground;
    }
  }

  return style;
});

const labelText = ref("");
const isTruncated = ref(false);

let resizeObserver: ResizeObserver | undefined;

function updateTruncation() {
  const label = labelRef.value;
  if (!label) {
    return;
  }

  labelText.value = label.textContent?.trim() ?? "";
  isTruncated.value = label.scrollWidth > label.clientWidth + 1;
}

onMounted(() => {
  updateTruncation();

  if (typeof ResizeObserver === "undefined" || !labelRef.value) {
    return;
  }

  resizeObserver = new ResizeObserver(updateTruncation);
  resizeObserver.observe(labelRef.value);

  if (rootRef.value) {
    resizeObserver.observe(rootRef.value);
  }
});

onUpdated(updateTruncation);

onBeforeUnmount(() => {
  resizeObserver?.disconnect();
});

const tooltipContent = computed(() => {
  if (props.title) {
    return props.title;
  }
  return isTruncated.value ? labelText.value : undefined;
});

function activate(event: MouseEvent) {
  if (props.disabled) {
    event.preventDefault();
    return;
  }

  if (props.checkable) {
    toggleChecked();
  }

  emit("click", event);
}

function toggleChecked() {
  if (props.disabled) {
    return;
  }

  const next = !isChecked.value;

  if (props.checked === undefined) {
    innerChecked.value = next;
  }

  emit("update:checked", next);
  emit("change", next);
}

function handleClose(event: MouseEvent) {
  if (props.disabled) {
    return;
  }

  emit("close", event);
}

function handleKeydown(event: KeyboardEvent) {
  if (!isInteractive.value || props.disabled) {
    return;
  }

  if (event.target !== event.currentTarget) {
    return;
  }

  if (event.key === "Enter" || event.key === " ") {
    event.preventDefault();
    rootRef.value?.click();
  }
}

function getContrastColor(color: string): string | undefined {
  const hex = normalizeHex(color);
  if (!hex) {
    return undefined;
  }

  return getRelativeLuminance(hex) > 0.179 ? "#0e1731" : "#ffffff";
}

function normalizeHex(color: string): string | undefined {
  const match = /^#([0-9a-f]{3}|[0-9a-f]{6})$/i.exec(color.trim());
  if (!match) {
    return undefined;
  }

  const value = match[1];
  return value.length === 3
    ? value
        .split("")
        .map((char) => char + char)
        .join("")
    : value;
}

function getRelativeLuminance(hex: string): number {
  const channels = [0, 2, 4].map((offset) => {
    const value = parseInt(hex.slice(offset, offset + 2), 16) / 255;
    return value <= 0.03928 ? value / 12.92 : ((value + 0.055) / 1.055) ** 2.4;
  });

  return 0.2126 * channels[0] + 0.7152 * channels[1] + 0.0722 * channels[2];
}
</script>
<template>
  <div
    ref="rootRef"
    v-tooltip="tooltipContent"
    :aria-disabled="disabled ? 'true' : undefined"
    :aria-pressed="checkable ? isChecked : undefined"
    :class="classes"
    :role="isInteractive ? 'button' : undefined"
    :style="rootStyle"
    :tabindex="isInteractive && !disabled ? 0 : undefined"
    class="tag-wrapper"
    @click="activate"
    @keydown="handleKeydown"
  >
    <span
      v-if="$slots.leftIcon"
      aria-hidden="true"
      class="tag-icon tag-left-icon"
    >
      <slot name="leftIcon" />
    </span>
    <span ref="labelRef" class="tag-content tag-label">
      <slot />
    </span>
    <span v-if="$slots.rightIcon" class="tag-icon tag-right-icon">
      <slot name="rightIcon" />
    </span>
    <button
      v-if="closable"
      :aria-label="t('common.close')"
      :disabled="disabled"
      class="tag-close"
      type="button"
      @click.stop="handleClose"
    >
      <IconClose aria-hidden="true" />
    </button>
  </div>
</template>
<style lang="scss">
@use "sass:map";

$tag-sizes: (
  sm: (
    height: theme("spacing.5"),
    padding-x: theme("spacing.2"),
    font-size: theme("fontSize.xs"),
    icon-size: theme("spacing.3"),
    gap: theme("spacing.1"),
  ),
  md: (
    height: theme("spacing.6"),
    padding-x: theme("spacing[2.5]"),
    font-size: theme("fontSize.xs"),
    icon-size: theme("spacing[3.5]"),
    gap: theme("spacing.1"),
  ),
  lg: (
    height: theme("spacing.7"),
    padding-x: theme("spacing.3"),
    font-size: theme("fontSize.sm"),
    icon-size: theme("spacing.4"),
    gap: theme("spacing[1.5]"),
  ),
);

.tag-wrapper {
  box-sizing: border-box;
  display: inline-flex;
  flex: none;
  align-items: center;
  justify-content: center;
  gap: 4px;
  min-block-size: 20px;
  min-inline-size: 0;
  max-inline-size: min(var(--tag-max-width, 12rem), 100%);
  padding-inline: 8px;
  border: 1px solid #d9d9d9;
  border-radius: theme("borderRadius.base");
  background: transparent;
  color: inherit;
  font-size: 12px;
  font-weight: 500;
  line-height: 1;
  text-align: center;
  vertical-align: middle;
  white-space: nowrap;
  user-select: none;
  transition:
    background-color 120ms ease,
    border-color 120ms ease,
    color 120ms ease,
    filter 120ms ease;

  @each $size, $config in $tag-sizes {
    &.tag-#{$size} {
      min-block-size: map.get($config, height);
      padding-inline: map.get($config, padding-x);
      font-size: map.get($config, font-size);
      gap: map.get($config, gap);

      .tag-icon {
        inline-size: map.get($config, icon-size);
        block-size: map.get($config, icon-size);

        svg,
        img {
          inline-size: 100%;
          block-size: 100%;
          object-fit: contain;
        }
      }
    }
  }

  &.tag-rounded {
    border-radius: theme("borderRadius.full");
  }

  .tag-content {
    min-inline-size: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .tag-icon {
    display: inline-flex;
    flex: none;
    align-items: center;
    justify-content: center;
  }

  .tag-close {
    display: inline-flex;
    flex: none;
    align-items: center;
    justify-content: center;
    inline-size: theme("spacing[3.5]");
    block-size: theme("spacing[3.5]");
    margin-inline-end: -2px;
    padding: 0;
    border: 0;
    border-radius: 2px;
    background: transparent;
    color: inherit;
    cursor: pointer;
    opacity: 0.72;

    &:hover {
      opacity: 1;
      background: color-mix(in srgb, currentColor 14%, transparent);
    }

    &:focus-visible {
      outline: 2px solid currentColor;
      outline-offset: 1px;
    }

    &:disabled {
      cursor: not-allowed;
      opacity: 0.5;
    }

    svg {
      inline-size: 10px;
      block-size: 10px;
    }
  }

  &.tag-default {
    background: transparent;
    border-color: #d9d9d9;
    color: inherit;
  }

  &.tag-primary {
    background: theme("colors.primary");
    border-color: theme("colors.primary");
    color: theme("colors.secondary");
  }

  &.tag-secondary {
    background: theme("colors.secondary");
    border-color: theme("colors.secondary");
    color: #fff;
  }

  &.tag-danger {
    background: theme("colors.danger");
    border-color: theme("colors.danger");
    color: #fff;
  }

  &.tag-success {
    background: theme("colors.success");
    border-color: theme("colors.success");
    color: #fff;
  }

  &.tag-warning {
    background: theme("colors.warning");
    border-color: theme("colors.warning");
    color: #fff;
  }

  &.tag-info {
    background: theme("colors.info");
    border-color: theme("colors.info");
    color: #fff;
  }

  &.tag-soft {
    &.tag-default {
      background: theme("colors.gray.100");
      border-color: theme("colors.gray.100");
      color: theme("colors.gray.700");
    }

    &.tag-primary {
      background: theme("colors.primary-soft");
      border-color: theme("colors.primary-soft");
      color: theme("colors.secondary");
    }

    &.tag-secondary {
      background: theme("colors.secondary-soft");
      border-color: theme("colors.secondary-soft");
      color: theme("colors.secondary");
    }

    &.tag-danger {
      background: theme("colors.danger-soft");
      border-color: theme("colors.danger-soft");
      color: theme("colors.danger-ink");
    }

    &.tag-success {
      background: theme("colors.success-soft");
      border-color: theme("colors.success-soft");
      color: theme("colors.success");
    }

    &.tag-warning {
      background: theme("colors.warning-soft");
      border-color: theme("colors.warning-soft");
      color: theme("colors.warning");
    }

    &.tag-info {
      background: theme("colors.info-soft");
      border-color: theme("colors.info-soft");
      color: theme("colors.info");
    }
  }

  &.tag-checked {
    &.tag-default {
      background: theme("colors.gray.600");
      border-color: theme("colors.gray.600");
      color: #fff;
    }

    &.tag-primary {
      background: theme("colors.primary");
      border-color: theme("colors.primary");
      color: theme("colors.secondary");
    }

    &.tag-secondary {
      background: theme("colors.secondary");
      border-color: theme("colors.secondary");
      color: #fff;
    }

    &.tag-danger {
      background: theme("colors.danger");
      border-color: theme("colors.danger");
      color: #fff;
    }

    &.tag-success {
      background: theme("colors.success");
      border-color: theme("colors.success");
      color: #fff;
    }

    &.tag-warning {
      background: theme("colors.warning");
      border-color: theme("colors.warning");
      color: #fff;
    }

    &.tag-info {
      background: theme("colors.info");
      border-color: theme("colors.info");
      color: #fff;
    }
  }

  &.tag-custom {
    background: var(--tag-custom-bg);
    border-color: var(--tag-custom-bg);
    color: var(--tag-custom-fg, contrast-color(var(--tag-custom-bg)));
  }

  &.tag-custom.tag-checked {
    box-shadow: inset 0 0 0 2px
      color-mix(in srgb, var(--tag-custom-fg, #fff) 45%, transparent);
  }

  &.tag-interactive {
    cursor: pointer;

    &:hover {
      filter: brightness(0.96);
    }

    &.tag-default:hover {
      background: theme("colors.gray.100");
      border-color: theme("colors.gray.400");
      filter: none;
    }

    &.tag-custom:hover {
      filter: brightness(0.94);
    }

    &:focus-visible {
      outline: 2px solid theme("colors.primary-ink");
      outline-offset: 2px;
    }
  }

  &.tag-disabled {
    cursor: not-allowed;
    opacity: 0.5;
  }

  &.tag-disabled:hover {
    filter: none;
  }
}
</style>
