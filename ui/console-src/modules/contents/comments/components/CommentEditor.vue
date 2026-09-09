<script lang="ts" setup>
import { VLoading } from "@halo-dev/components";
import type { CommentEditorProvider } from "@halo-dev/ui-shared";
import { useQuery } from "@tanstack/vue-query";
import { computed, markRaw } from "vue";
import { usePluginModuleStore } from "@/stores/plugin";
import DefaultCommentEditor from "./DefaultCommentEditor.vue";

const props = withDefaults(
  defineProps<{
    autoFocus?: boolean;
    initialContent?: string;
  }>(),
  {
    autoFocus: false,
    initialContent: undefined,
  }
);

const defaultProvider: CommentEditorProvider = {
  component: markRaw(DefaultCommentEditor),
  supportsEditing: true,
};

const { pluginModules } = usePluginModuleStore();

const emit = defineEmits<{
  (event: "update", value: { content: string; characterCount: number }): void;
}>();

const { data: provider, isLoading } = useQuery({
  queryKey: ["core:comment:provider"],
  queryFn: async () => {
    const result: CommentEditorProvider[] = [];
    for (const pluginModule of pluginModules) {
      const callbackFunction =
        pluginModule?.extensionPoints?.["comment:editor:replace"];

      if (typeof callbackFunction !== "function") {
        continue;
      }

      const item = await callbackFunction();

      result.push(item);
    }

    if (result.length) {
      return result[0];
    }

    return defaultProvider;
  },
});

const editorProvider = computed(() => {
  if (props.initialContent !== undefined && !provider.value?.supportsEditing) {
    return defaultProvider;
  }
  return provider.value;
});

function onUpdate(value: { content: string; characterCount: number }) {
  emit("update", value);
}
</script>
<template>
  <VLoading v-if="isLoading" />
  <component
    :is="editorProvider?.component"
    v-else
    :auto-focus="autoFocus"
    :initial-content="initialContent"
    @update="onUpdate"
  />
  <p
    v-if="
      !isLoading &&
      initialContent !== undefined &&
      editorProvider?.component === defaultProvider.component
    "
    class="mt-2 text-sm text-gray-500"
  >
    {{ $t("core.comment.edit_modal.source_help") }}
  </p>
</template>
