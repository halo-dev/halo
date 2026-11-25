<script lang="ts" setup>
import type { Component, ComputedRef } from "vue";
import { computed, inject } from "vue";

const props = withDefaults(
  defineProps<{
    id?: string | number;
    label?: string;
    icon?: Component;
    disabled?: boolean;
    badge?: string | number;
    ariaLabel?: string;
    persistent?: boolean;
  }>(),
  {
    id: undefined,
    label: undefined,
    icon: undefined,
    disabled: false,
    badge: undefined,
    ariaLabel: undefined,
    persistent: false,
  }
);

const activeId = inject<ComputedRef<string | number | undefined>>("activeId");
const tabsId = inject<string | undefined>("tabsId", undefined);

if (!activeId && (import.meta as { env?: { DEV?: boolean } }).env?.DEV) {
  console.warn("[VTabItem] must be used inside <VTabs>.");
}

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
    v-if="isActive || persistent"
    v-show="isActive"
    :id="panelId"
    :aria-labelledby="tabId"
    class="tabs-item-wrapper rounded-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-inset focus-visible:ring-primary"
    role="tabpanel"
    tabindex="0"
  >
    <slot />
  </div>
</template>
