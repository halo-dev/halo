<script lang="ts" setup>
import type { NodeViewProps } from "@/tiptap/vue-3";
import { NodeViewContent, NodeViewWrapper } from "@/tiptap/vue-3";
import { computed, ref, watch } from "vue";

const props = defineProps<NodeViewProps>();

const width = computed(() => props.node?.attrs.width);
const placeholder = computed(() => props.node?.attrs["data-placeholder"]);
const isEmpty = ref(props.node?.textContent.trim().length === 0);

watch(
  () => props.node?.textContent,
  (newContent) => {
    isEmpty.value = newContent?.trim().length === 0;
  }
);
</script>

<template>
  <node-view-wrapper
    as="figcaption"
    :data-empty="isEmpty ? 'true' : undefined"
    :data-placeholder="placeholder"
    :style="{
      width: width || '',
      maxWidth: width ? '100%' : '',
      textAlign: width ? 'center' : '',
    }"
  >
    <node-view-content />
  </node-view-wrapper>
</template>
