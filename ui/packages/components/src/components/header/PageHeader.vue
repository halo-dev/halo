<script lang="ts" setup>
defineProps<{
  title?: string;
}>();
</script>
<template>
  <div
    class="page-header"
    :class="{ 'page-header--has-center': $slots.center }"
  >
    <h2 class="page-header__title">
      <slot name="icon" />
      <span class="page-header__title-text">{{ title }}</span>
    </h2>
    <div v-if="$slots.center" class="page-header__center">
      <slot name="center" />
    </div>
    <div v-if="$slots.actions" class="page-header__actions">
      <slot name="actions" />
    </div>
  </div>
</template>

<style lang="scss">
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 3.5rem;
  background-color: theme("colors.white");
  padding-left: 1rem;
  padding-right: 1rem;
  padding-top: 0.5rem;
  padding-bottom: 0.5rem;
  flex-wrap: wrap;
  gap: theme("spacing.2");

  &__title {
    display: flex;
    align-items: center;
    min-width: 0;
    text-overflow: truncate;
    font-size: 1.25rem;
    font-weight: 700;
    color: theme("colors.gray.800");
    gap: theme("spacing.2");
  }

  &__title-text {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__center {
    display: contents;
  }

  &__actions {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: theme("spacing.2");
    flex-wrap: wrap;
  }
}

// Center the middle slot on the header itself instead of spacing it between
// the title and actions groups, whose widths usually differ.
@media (min-width: 640px) {
  .page-header--has-center {
    display: grid;
    grid-template-columns: minmax(0, 1fr) auto minmax(0, 1fr);

    .page-header__center {
      display: flex;
      align-items: center;
      justify-content: center;
      min-width: 0;
    }

    .page-header__actions {
      justify-self: end;
    }
  }
}
</style>
