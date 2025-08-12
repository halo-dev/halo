<script lang="ts" setup>
import sanitizeHtml from "sanitize-html";
defineProps<{
  content: string;
}>();
</script>
<template>
  <div
    class="comment-content markdown-body whitespace-pre-wrap rounded-lg !bg-transparent !text-sm !text-gray-900"
    v-html="sanitizeHtml(content)"
  ></div>
</template>

<style scoped lang="scss">
.comment-content :deep(ul) {
  list-style: disc !important;
}

.comment-content :deep(ol) {
  list-style: decimal !important;
}
</style>
