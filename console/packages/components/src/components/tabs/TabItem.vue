<script lang="ts" setup>
import type { ComputedRef } from "vue";
import { computed, inject } from "vue";

const props = defineProps<{
  id?: string;
  label?: string;
}>();

const activeId = inject<ComputedRef<string | number | undefined>>("activeId");

const isActive = computed(() => {
  return activeId?.value === props.id;
});
</script>
<template>
  <div v-if="isActive" class="tabs-item-wrapper">
    <slot />
  </div>
</template>
