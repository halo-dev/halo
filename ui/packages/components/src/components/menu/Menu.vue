<script lang="ts" setup>
import { provide, toRef } from "vue";
import { menuContextKey } from "./context";

const props = defineProps<{
  openIds?: string[];
  ariaLabel?: string;
}>();

provide(menuContextKey, {
  openIds: toRef(props, "openIds"),
});
</script>

<template>
  <div :aria-label="ariaLabel" class="w-full p-3" role="navigation">
    <ul>
      <slot />
    </ul>
  </div>
</template>
