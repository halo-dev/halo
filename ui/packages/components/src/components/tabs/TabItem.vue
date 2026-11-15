<script lang="ts" setup>
import type { ComputedRef } from "vue";
import { computed, inject } from "vue";

const props = defineProps<{
  id?: string;
  label?: string;
}>();

const activeId = inject<ComputedRef<string | number | undefined>>("activeId");
const tabsId = inject<string | undefined>("tabsId", undefined);

const isActive = computed(() => {
  return activeId?.value === props.id;
});

const tabId = computed(() => {
  return tabsId && props.id !== undefined
    ? `${tabsId}-tab-${props.id}`
    : undefined;
});

const panelId = computed(() => {
  return tabsId && props.id !== undefined
    ? `${tabsId}-panel-${props.id}`
    : undefined;
});
</script>
<template>
  <div
    v-if="isActive"
    :id="panelId"
    :aria-labelledby="tabId"
    class="tabs-item-wrapper"
    role="tabpanel"
  >
    <slot />
  </div>
</template>
