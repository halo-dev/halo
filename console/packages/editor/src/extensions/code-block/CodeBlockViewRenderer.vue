<script lang="ts" setup>
import type { Node as ProseMirrorNode, Decoration } from "@/tiptap/pm";
import type { Editor, Node } from "@/tiptap/vue-3";
import { NodeViewContent, NodeViewWrapper } from "@/tiptap/vue-3";
import lowlight from "./lowlight";
import { computed } from "vue";

const props = defineProps<{
  editor: Editor;
  node: ProseMirrorNode;
  decorations: Decoration[];
  selected: boolean;
  extension: Node<any, any>;
  getPos: () => number;
  updateAttributes: (attributes: Record<string, any>) => void;
  deleteNode: () => void;
}>();

const languages = computed(() => {
  return lowlight.listLanguages();
});

const selectedLanguage = computed({
  get: () => {
    return props.node?.attrs.language;
  },
  set: (language: string) => {
    props.updateAttributes({ language: language });
  },
});
</script>
<template>
  <node-view-wrapper as="div" class="code-node">
    <div class="py-1.5">
      <select
        v-model="selectedLanguage"
        contenteditable="false"
        class="block px-2 py-1.5 text-sm text-gray-900 border border-gray-300 rounded-md bg-gray-50 focus:ring-blue-500 focus:border-blue-500"
      >
        <option :value="null">auto</option>
        <option
          v-for="(language, index) in languages"
          :key="index"
          :value="language"
        >
          {{ language }}
        </option>
      </select>
    </div>
    <pre><node-view-content as="code" class="hljs" /></pre>
  </node-view-wrapper>
</template>
