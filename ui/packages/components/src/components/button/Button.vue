<template>
  <button
    :class="classes"
    :disabled="disabled"
    class="btn"
    type="button"
    @click="handleClick"
  >
    <span v-if="$slots.icon || loading" class="btn-icon">
      <svg
        v-if="loading"
        class="animate-spin"
        fill="none"
        viewBox="0 0 24 24"
        xmlns="http://www.w3.org/2000/svg"
      >
        <circle
          class="opacity-25"
          cx="12"
          cy="12"
          r="10"
          stroke="currentColor"
          stroke-width="4"
        ></circle>
        <path
          class="opacity-75"
          d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
          fill="currentColor"
        ></path>
      </svg>
      <slot v-else name="icon" />
    </span>
    <span class="btn-content">
      <slot />
    </span>
  </button>
</template>

<script lang="ts" setup>
import { computed } from "vue";
import type { RouteLocationRaw } from "vue-router";
import { useRouter } from "vue-router";
import type { Size, Type } from "./interface";

const props = withDefaults(
  defineProps<{
    type?: Type;
    size?: Size;
    circle?: boolean;
    block?: boolean;
    disabled?: boolean;
    loading?: boolean;
    route?: RouteLocationRaw | undefined;
    ghost?: boolean;
  }>(),
  {
    type: "default",
    size: "md",
    circle: false,
    block: false,
    disabled: false,
    loading: false,
    route: undefined,
    ghost: false,
  }
);

const router = useRouter();
const emit = defineEmits<{
  (event: "click"): void;
}>();

const classes = computed(() => {
  return [
    `btn-${props.size}`,
    `btn-${props.type}`,
    { "btn-circle": props.circle },
    { "btn-block": props.block },
    { "btn-loading": props.loading },
    { "btn-ghost": props.ghost },
  ];
});

function handleClick() {
  if (props.disabled || props.loading) return;
  if (props.route) {
    router.push(props.route);
  }
  emit("click");
}
</script>

<style lang="scss">
@use "sass:map";
$btn-sizes: (
  xs: (
    height: theme("spacing.6"),
    padding-x: theme("spacing.2"),
    font-size: theme("fontSize.xs"),
    icon-size: theme("spacing.3"),
    icon-margin: theme("spacing.2"),
  ),
  sm: (
    height: theme("spacing.7"),
    padding-x: theme("spacing.3"),
    font-size: theme("fontSize.xs"),
    icon-size: theme("spacing.3"),
    icon-margin: theme("spacing.2"),
  ),
  md: (
    height: theme("spacing.9"),
    padding-x: theme("spacing.4"),
    font-size: theme("fontSize.sm"),
    icon-size: theme("spacing.5"),
    icon-margin: theme("spacing.3"),
  ),
  lg: (
    height: theme("spacing.11"),
    padding-x: theme("spacing.5"),
    font-size: theme("fontSize.lg"),
    icon-size: theme("spacing.5"),
    icon-margin: theme("spacing.3"),
  ),
);

$btn-themes: (
  default: (
    bg: transparent,
    color: inherit,
    border: 1px solid #d9d9d9,
    hover-bg: theme("colors.gray.100"),
    icon-color: theme("colors.secondary"),
    ghost-color: inherit,
    ghost-hover-bg: theme("colors.gray.100"),
    ghost-icon-color: theme("colors.secondary"),
  ),
  primary: (
    bg: theme("colors.primary"),
    color: #fff,
    border: none,
    hover-bg: theme("colors.primary"),
    icon-color: #fff,
    ghost-color: theme("colors.primary"),
    ghost-hover-bg: theme("colors.primary / 10%"),
    ghost-icon-color: theme("colors.primary"),
  ),
  secondary: (
    bg: theme("colors.secondary"),
    color: #fff,
    border: none,
    hover-bg: theme("colors.secondary"),
    icon-color: #fff,
    ghost-color: theme("colors.secondary"),
    ghost-hover-bg: theme("colors.secondary / 10%"),
    ghost-icon-color: theme("colors.secondary"),
  ),
  danger: (
    bg: theme("colors.danger"),
    color: #fff,
    border: none,
    hover-bg: theme("colors.danger"),
    icon-color: #fff,
    ghost-color: theme("colors.danger"),
    ghost-hover-bg: theme("colors.danger / 10%"),
    ghost-icon-color: theme("colors.danger"),
  ),
);

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  flex-wrap: wrap;
  cursor: pointer;
  user-select: none;
  appearance: none;
  border-radius: theme("borderRadius.base");
  transition: all 150ms cubic-bezier(0.4, 0, 0.2, 1);
  text-align: center;
  text-decoration: none;
  vertical-align: middle;
  outline-width: 0;
  border-style: none;
  $md-config: map.get($btn-sizes, md);
  height: map.get($md-config, height);
  padding-left: map.get($md-config, padding-x);
  padding-right: map.get($md-config, padding-x);
  font-size: map.get($md-config, font-size);

  &:hover {
    opacity: 0.9;
  }

  &:active {
    opacity: 1;
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }

  &.btn-loading {
    cursor: not-allowed;

    &:hover {
      opacity: 1;
    }
  }

  &.btn-block {
    width: 100%;
  }

  .btn-icon {
    height: map.get($md-config, icon-size);
    width: map.get($md-config, icon-size);
    margin-right: map.get($md-config, icon-margin);
    color: #fff;
    display: inline-flex;
    align-items: center;
    justify-content: center;

    > * {
      height: 100%;
      width: 100%;
    }
  }
}

@each $size, $config in $btn-sizes {
  .btn-#{$size} {
    height: map.get($config, height);
    padding-left: map.get($config, padding-x);
    padding-right: map.get($config, padding-x);
    font-size: map.get($config, font-size);

    .btn-icon {
      height: map.get($config, icon-size);
      width: map.get($config, icon-size);
      margin-right: map.get($config, icon-margin);
    }

    &.btn-circle {
      width: map.get($config, height);
      padding: 0;
      border-radius: 9999px;
    }
  }
}

@each $theme, $config in $btn-themes {
  .btn-#{$theme} {
    background-color: map.get($config, bg) !important;
    color: map.get($config, color);
    border: map.get($config, border);

    &:hover {
      background-color: map.get($config, hover-bg) !important;
    }

    .btn-icon {
      color: map.get($config, icon-color);
    }
  }
}

.btn-ghost {
  background-color: transparent !important;

  @each $theme, $config in $btn-themes {
    &.btn-#{$theme} {
      color: map.get($config, ghost-color);
      border: none;

      &:hover {
        background-color: map.get($config, ghost-hover-bg) !important;
      }

      .btn-icon {
        color: map.get($config, ghost-icon-color);
      }
    }
  }
}
</style>
