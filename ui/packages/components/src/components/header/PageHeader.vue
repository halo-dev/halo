<script lang="ts" setup>
defineProps<{
  title?: string;
}>();
</script>
<template>
  <div class="page-header">
    <h2 class="page-header__title">
      <slot name="icon" />
      <span class="page-header__title-text">{{ title }}</span>
    </h2>
    <div class="page-header__actions">
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

  &__actions {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: theme("spacing.2");
    flex-wrap: wrap;
  }
}
</style>
