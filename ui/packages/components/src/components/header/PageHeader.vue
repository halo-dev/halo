<script lang="ts" setup>
import { useEventListener } from "@vueuse/core";
import { onMounted, ref, watch } from "vue";
import { useComponentsI18n } from "../../locales";
import { VSkeleton } from "../skeleton";

const props = withDefaults(
  defineProps<{
    /**
     * Page title. Ignored when the `title` slot is used.
     */
    title?: string;
    /**
     * Secondary line below the title, e.g. counts, versions or timestamps.
     * Ignored when the `subtitle` slot is used.
     */
    subtitle?: string;
    /**
     * Sticks the header to the top of the viewport while scrolling.
     * Implies `bordered` and adds a shadow once the header is stuck.
     */
    sticky?: boolean;
    /**
     * Draws a bottom border. Enabled automatically when `sticky` is set.
     */
    bordered?: boolean;
    /**
     * Replaces the title and subtitle with skeletons and makes the actions inert.
     */
    loading?: boolean;
    /**
     * Truncates the title and subtitle to a single line. Set to `false` to wrap.
     */
    truncate?: boolean;
  }>(),
  {
    title: undefined,
    subtitle: undefined,
    sticky: false,
    bordered: false,
    loading: false,
    truncate: true,
  }
);

const { t } = useComponentsI18n();

const root = ref<HTMLElement | null>(null);
const isStuck = ref(false);

function updateStuck() {
  if (!props.sticky || !root.value) {
    isStuck.value = false;
    return;
  }

  isStuck.value =
    window.scrollY > 0 && root.value.getBoundingClientRect().top <= 0;
}

useEventListener(window, "scroll", updateStuck, { passive: true });
useEventListener(window, "resize", updateStuck);

watch(() => props.sticky, updateStuck);
onMounted(updateStuck);
</script>

<template>
  <header
    ref="root"
    class="page-header"
    :class="{
      'page-header--bordered': bordered || sticky,
      'page-header--sticky': sticky,
      'page-header--stuck': isStuck,
      'page-header--wrapped': !truncate,
    }"
    :aria-busy="loading ? 'true' : undefined"
  >
    <div class="page-header__leading">
      <div v-if="$slots.icon" class="page-header__icon" aria-hidden="true">
        <slot name="icon" />
      </div>

      <div class="page-header__titles">
        <template v-if="loading">
          <VSkeleton
            class="page-header__skeleton page-header__skeleton--title"
            width="11.25rem"
            height="1.25rem"
            animation="shimmer"
          />
          <VSkeleton
            class="page-header__skeleton page-header__skeleton--subtitle"
            width="7.5rem"
            height="0.625rem"
            animation="shimmer"
          />
        </template>
        <template v-else>
          <div v-if="title || $slots.title" class="page-header__titleline">
            <h1
              class="page-header__title"
              :title="$slots.title ? undefined : title"
            >
              <slot name="title">{{ title }}</slot>
            </h1>
            <div v-if="$slots.tags" class="page-header__tags">
              <slot name="tags" />
            </div>
          </div>
          <p
            v-if="subtitle || $slots.subtitle"
            class="page-header__subtitle"
            :title="$slots.subtitle ? undefined : subtitle"
          >
            <slot name="subtitle">{{ subtitle }}</slot>
          </p>
        </template>
      </div>
    </div>

    <div
      class="page-header__actions"
      :class="{ 'page-header__actions--loading': loading }"
      role="group"
      :aria-label="t('page_header.actions')"
      :inert="loading || undefined"
    >
      <slot name="actions" />
    </div>
  </header>
</template>

<style lang="scss">
.page-header {
  --page-header-bg: theme("colors.white");
  --page-header-text: theme("colors.gray.800");
  --page-header-subtle: theme("colors.gray.500");
  --page-header-border: theme("colors.gray.200");
  --page-header-hover: theme("colors.gray.100");

  box-sizing: border-box;
  display: flex;
  flex: 0 0 auto;
  flex-wrap: nowrap;
  align-items: center;
  justify-content: space-between;
  gap: theme("spacing.2") theme("spacing.4");
  min-height: 3.5rem;
  padding: theme("spacing.2") theme("spacing.4");
  color: var(--page-header-text);
  background-color: var(--page-header-bg);

  &--bordered {
    border-bottom: 1px solid var(--page-header-border);
  }

  &--sticky {
    position: sticky;
    top: 0;
    z-index: 10;
  }

  &--stuck {
    box-shadow: 0 8px 18px -12px rgba(16, 24, 40, 0.35);
  }

  &__leading {
    display: flex;
    align-items: center;
    gap: theme("spacing.2");
    flex: 1 1 auto;
    min-width: 0;
  }

  &__icon {
    display: inline-grid;
    place-items: center;
    flex: none;
    // Match the title so `1em` icon slots keep their previous size.
    font-size: 1.25rem;
  }

  &__titles {
    display: flex;
    flex-direction: column;
    gap: 2px;
    flex: 1 1 auto;
    min-width: 0;
  }

  &__titleline {
    display: flex;
    align-items: center;
    gap: theme("spacing.2");
    min-width: 0;
  }

  &__title {
    margin: 0;
    min-width: 0;
    font-size: 1.25rem;
    font-weight: 700;
    line-height: 1.4;
    color: var(--page-header-text);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  &__subtitle {
    margin: 0;
    min-width: 0;
    font-size: 0.75rem;
    line-height: 1.4;
    color: var(--page-header-subtle);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  &__tags {
    display: flex;
    align-items: center;
    gap: theme("spacing.1");
    flex: none;
  }

  &__actions {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: theme("spacing.2");
    flex: none;
    margin-left: auto;

    &--loading {
      pointer-events: none;
      opacity: 0.45;
    }
  }

  &--wrapped {
    .page-header__title,
    .page-header__subtitle {
      white-space: normal;
      overflow: visible;
      text-overflow: clip;
      overflow-wrap: anywhere;
    }

    .page-header__titleline {
      flex-wrap: wrap;
      align-items: flex-start;
    }
  }
}

// Tailwind's `sm` breakpoint: below it the actions move to their own row.
@media (max-width: 640px) {
  .page-header {
    flex-wrap: wrap;
  }

  .page-header__leading {
    flex-basis: 100%;
  }

  .page-header__actions {
    width: 100%;
    margin-left: 0;
    justify-content: flex-start;
    flex-wrap: wrap;
  }
}
</style>
