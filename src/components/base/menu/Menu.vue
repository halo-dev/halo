<script lang="ts" setup>
import type { PropType } from "vue";
import { provide } from "vue";

const props = defineProps({
  openIds: {
    type: Object as PropType<string[]>,
    required: false,
  },
});

provide<string[] | undefined>("openIds", props.openIds);
</script>

<template>
  <div class="menu-container w-full p-3">
    <ul>
      <slot />
    </ul>
  </div>
</template>
