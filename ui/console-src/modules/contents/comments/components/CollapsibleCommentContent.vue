<script lang="ts" setup>
import { computed, onBeforeUnmount, onMounted, ref, useId } from "vue";
import { useContentProviderExtensionPoint } from "../composables/use-content-provider-extension-point";

defineProps<{
  content?: string;
}>();

const COLLAPSED_MAX_HEIGHT = 200;
const FADE_HEIGHT = 48;

const { data: contentProvider } = useContentProviderExtensionPoint();

const expanded = ref(false);
const collapsible = ref(false);
const contentRef = ref<HTMLElement>();
const contentId = `collapsible-comment-content-${useId()}`;

const collapsed = computed(() => collapsible.value && !expanded.value);

const collapsedStyle = computed(() => {
  if (!collapsed.value) {
    return undefined;
  }
  const mask = `linear-gradient(to bottom, black calc(100% - ${FADE_HEIGHT}px), transparent 100%)`;
  return {
    maxHeight: `${COLLAPSED_MAX_HEIGHT}px`,
    maskImage: mask,
    WebkitMaskImage: mask,
  } as Record<string, string>;
});

function handleContentClick() {
  if (collapsed.value) {
    expanded.value = true;
  }
}

let observer: ResizeObserver | undefined;

onMounted(() => {
  observer = new ResizeObserver(() => {
    if (contentRef.value) {
      collapsible.value = contentRef.value.scrollHeight > COLLAPSED_MAX_HEIGHT;
    }
  });
  if (contentRef.value) {
    observer.observe(contentRef.value);
  }
});

onBeforeUnmount(() => {
  observer?.disconnect();
});
</script>

<template>
  <div class="max-w-3xl">
    <div
      :id="contentId"
      ref="contentRef"
      :class="{ 'cursor-pointer overflow-hidden': collapsed }"
      :style="collapsedStyle"
      @click="handleContentClick"
    >
      <div :inert="collapsed">
        <component :is="contentProvider?.component" :content="content" />
      </div>
    </div>
    <button
      v-if="collapsible"
      type="button"
      class="cursor-pointer select-none text-xs text-gray-700 hover:text-gray-900"
      :aria-expanded="expanded"
      :aria-controls="contentId"
      @click="expanded = !expanded"
    >
      {{
        expanded
          ? $t("core.comment.text.collapse_content")
          : $t("core.comment.text.expand_content")
      }}
    </button>
  </div>
</template>
