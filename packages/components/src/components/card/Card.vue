<script lang="ts" setup>
defineProps<{
  title?: string;
  bodyClass?: string[];
}>();
</script>

<template>
  <div class="card-wrapper">
    <div v-if="title || $slots.header" class="card-header">
      <slot name="header">
        <div class="card-header-title">
          {{ title }}
        </div>
        <div class="card-header-actions">
          <slot name="actions" />
        </div>
      </slot>
    </div>
    <div :class="bodyClass" class="card-body">
      <slot />
    </div>
    <div v-if="$slots.footer" class="card-footer">
      <slot name="footer" />
    </div>
  </div>
</template>
<style lang="scss">
.card-wrapper {
  @apply box-border
  flex
  flex-col
  bg-white
  shadow-sm
  overflow-hidden
  rounded-base;
  border: 1px solid #eaecf0;

  .card-header {
    @apply flex
    justify-between;
    border-bottom: 1px solid #eaecf0;

    .card-header-title {
      @apply self-center
      text-base
      font-bold;
      padding: 12px 16px;
    }

    .card-header-actions {
      @apply self-center;
    }
  }

  .card-body {
    padding: 12px 16px;
  }

  .card-footer {
    border-top: 1px solid #eaecf0;
    padding: 12px 16px;
  }
}
</style>
