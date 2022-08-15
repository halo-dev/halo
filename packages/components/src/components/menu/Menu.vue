<script lang="ts" setup>
import { provide } from "vue";

const props = defineProps<{
  openIds?: string[];
}>();

provide<string[] | undefined>("openIds", props.openIds);
</script>

<template>
  <div class="menu-container w-full p-3">
    <ul>
      <slot />
    </ul>
  </div>
</template>
