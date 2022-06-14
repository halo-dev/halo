<script lang="ts" setup>
import tippy from "tippy.js";
import "tippy.js/dist/tippy.css";
import { onMounted } from "vue";

const props = defineProps({
  content: {
    type: String,
    default: "",
  },
});

onMounted(() => {
  tippy(".tooltip-wrapper > :first-child", {
    content: props.content,
  });
});
</script>
<template>
  <teleport to="body">
    <div class="tooltip-wrapper">
      <slot />
    </div>
  </teleport>
</template>
