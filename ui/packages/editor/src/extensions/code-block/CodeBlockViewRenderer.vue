<script lang="ts" setup>
import { i18n } from "@/locales";
import type { Decoration, Node as ProseMirrorNode } from "@/tiptap/pm";
import type { Editor, Node } from "@/tiptap/vue-3";
import { NodeViewContent, NodeViewWrapper } from "@/tiptap/vue-3";
import { useTimeout } from "@vueuse/core";
import { computed } from "vue";
import BxBxsCopy from "~icons/bx/bxs-copy";
import IconCheckboxCircle from "~icons/ri/checkbox-circle-line";
import lowlight from "./lowlight";

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

const { ready, start } = useTimeout(2000, { controls: true, immediate: false });

const handleCopyCode = () => {
  if (!ready.value) return;
  const code = props.node.textContent;
  navigator.clipboard.writeText(code).then(() => {
    start();
  });
};
</script>
<template>
  <node-view-wrapper as="div" class="code-node border-[1px] rounded mt-3">
    <div
      class="bg-neutral-100 border-b-[1px] border-b-gray-100 py-1 flex items-center justify-between rounded-t"
    >
      <div class="flex-1 flex items-center pl-3">
        <select
          v-model="selectedLanguage"
          contenteditable="false"
          class="block !leading-8 text-sm text-gray-900 border select-none border-transparent rounded-md bg-transparent focus:ring-blue-500 focus:border-blue-500 cursor-pointer hover:bg-zinc-200"
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
      <div class="pr-3 flex items-center">
        <div
          v-tooltip="
            ready
              ? i18n.global.t('editor.common.codeblock.copy_code')
              : i18n.global.t('editor.common.codeblock.copy_code_success')
          "
          class="w-8 h-8 cursor-pointer rounded flex items-center justify-center"
          :class="{ 'hover:bg-zinc-200': ready }"
          @click="handleCopyCode"
        >
          <IconCheckboxCircle v-if="!ready" class="w-4 h-4 text-green-500" />
          <BxBxsCopy v-else class="w-4 h-4 text-gray-500" />
        </div>
      </div>
    </div>
    <pre><node-view-content as="code" class="hljs" /></pre>
  </node-view-wrapper>
</template>
