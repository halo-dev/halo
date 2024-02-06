<script lang="ts" setup></script>

<template>
  <dl class="description-wrapper">
    <slot />
  </dl>
</template>

<style>
.description-wrapper {
  @apply divide-y divide-gray-100;
}
</style>
