<script lang="ts" setup>
defineProps({
  value: {
    type: [String, Number, Boolean],
  },
});
</script>

<template>
  <option :value="value">
    <slot />
  </option>
</template>
