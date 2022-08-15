<script lang="ts" setup>
defineProps<{
  value?: string | number | boolean;
}>();
</script>

<template>
  <option :value="value">
    <slot />
  </option>
</template>
