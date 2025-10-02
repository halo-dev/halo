<script lang="ts" setup>
import { usePluginModuleStore } from "@/stores/plugin";
import { VLoading } from "@halo-dev/components";
import type { CommentEditorProvider } from "@halo-dev/console-shared";
import { useQuery } from "@tanstack/vue-query";
import { markRaw } from "vue";
import DefaultCommentEditor from "./DefaultCommentEditor.vue";

withDefaults(
  defineProps<{
    autoFocus?: boolean;
  }>(),
  {
    autoFocus: false,
  }
);

const defaultProvider: CommentEditorProvider = {
  component: markRaw(DefaultCommentEditor),
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

function onUpdate(value: { content: string; characterCount: number }) {
  emit("update", value);
}
</script>
<template>
  <VLoading v-if="isLoading" />
  <component
    :is="provider?.component"
    v-else
    :auto-focus="autoFocus"
    @update="onUpdate"
  />
</template>
