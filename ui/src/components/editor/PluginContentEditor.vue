<script setup lang="ts">
import { VAlert, VLoading } from "@halo-dev/components";
import { computed, onMounted, ref } from "vue";
import { useI18n } from "vue-i18n";
import { useEditorExtensionPoints } from "@/composables/use-editor-extension-points";

defineOptions({ inheritAttrs: false });
const props = withDefaults(
  defineProps<{
    rawType?: string;
    preferredEditor?: string;
    requirePreferredEditor?: boolean;
  }>(),
  { rawType: "HTML", preferredEditor: undefined }
);
const { editorProviders, fetchEditorProviders } = useEditorExtensionPoints();
const { t } = useI18n();
const loading = ref(true);
const editor = ref<{ flush?: () => void | Promise<void> }>();
const provider = computed(() => {
  const compatible = editorProviders.value.filter(
    (item) => item.rawType.toLowerCase() === props.rawType.toLowerCase()
  );
  return (
    compatible.find((item) => item.name === props.preferredEditor) ||
    (props.requirePreferredEditor && props.preferredEditor
      ? undefined
      : compatible[0])
  );
});
onMounted(async () => {
  try {
    await fetchEditorProviders();
  } finally {
    loading.value = false;
  }
});
defineExpose({
  flush: async () => {
    await editor.value?.flush?.();
  },
});
</script>
<template>
  <VLoading v-if="loading" />
  <component
    :is="provider.component"
    v-else-if="provider"
    :key="provider.name"
    ref="editor"
    v-bind="$attrs"
  />
  <VAlert
    v-else
    type="warning"
    :title="
      t('core.common.dialog.descriptions.editor_not_found', {
        raw_type: rawType,
      })
    "
  />
</template>
