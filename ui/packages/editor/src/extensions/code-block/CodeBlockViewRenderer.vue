<script lang="ts" setup>
import { i18n } from "@/locales";
import {
  NodeViewContent,
  NodeViewWrapper,
  type NodeViewProps,
} from "@/tiptap/vue-3";
import { useTimeout } from "@vueuse/core";
import { computed } from "vue";
import MingcuteCheckCircleLine from "~icons/mingcute/check-circle-line";
import MingcuteCopyLine from "~icons/mingcute/copy-line";
import MingcuteRightSmallFill from "~icons/mingcute/right-small-fill";
import CodeBlockSelect from "./CodeBlockSelect.vue";

const props = defineProps<NodeViewProps>();

const languageOptions = computed(() => {
  let languages: Array<{
    label: string;
    value: string;
  }> = [];
  const lang = props.extension.options.languages;
  if (typeof lang === "function") {
    languages = lang(props.editor.state);
  } else {
    languages = lang;
  }
  languages = languages || [];
  const languageValues = languages.map((language) => language.value);
  if (languageValues.indexOf("auto") === -1) {
    languages.unshift({
      label: "Auto",
      value: "auto",
    });
  }
  return languages;
});

const selectedLanguage = computed({
  get: () => {
    return props.node?.attrs.language || "auto";
  },
  set: (language: string) => {
    props.updateAttributes({ language: language });
  },
});

const themeOptions = computed(() => {
  let themes:
    | Array<{
        label: string;
        value: string;
      }>
    | undefined = [];
  const theme = props.extension.options.themes;
  if (typeof theme === "function") {
    themes = theme(props.editor.state);
  } else {
    themes = theme;
  }

  if (!themes) {
    return undefined;
  }
  return themes;
});

const selectedTheme = computed({
  get: () => {
    return props.node?.attrs.theme || themeOptions.value?.[0].value;
  },
  set: (theme: string) => {
    props.updateAttributes({ theme: theme });
  },
});

const collapsed = computed<boolean>({
  get: () => {
    return props.node.attrs.collapsed || false;
  },
  set: (collapsed: boolean) => {
    props.updateAttributes({ collapsed: collapsed });
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
  <node-view-wrapper
    as="div"
    class="code-node mt-3 overflow-hidden rounded border-[1px]"
  >
    <div
      contenteditable="false"
      class="flex items-center justify-between border-b-[1px] border-b-gray-100 bg-neutral-100 py-1"
    >
      <div
        class="flex flex-1 items-center pl-3"
        @click.self="collapsed ? (collapsed = false) : null"
      >
        <div class="flex items-center pr-3">
          <button
            type="button"
            class="flex size-8 cursor-pointer items-center justify-center rounded transition-colors hover:bg-gray-200 active:!bg-gray-300"
            @click.stop="collapsed = !collapsed"
          >
            <MingcuteRightSmallFill
              class="size-6 transition-all"
              :class="{ 'rotate-90': !collapsed }"
            />
          </button>
        </div>
        <CodeBlockSelect
          v-model="selectedLanguage"
          class="w-48"
          :container="editor.options.element"
          :options="languageOptions"
        >
        </CodeBlockSelect>
        <CodeBlockSelect
          v-if="themeOptions && themeOptions.length > 0"
          v-model="selectedTheme"
          :container="editor.options.element"
          class="w-48"
          :options="themeOptions"
          @select="editor.commands.focus()"
        >
        </CodeBlockSelect>
      </div>
      <div class="flex items-center pr-3">
        <button
          v-tooltip="
            ready
              ? i18n.global.t('editor.common.codeblock.copy_code')
              : i18n.global.t('editor.common.codeblock.copy_code_success')
          "
          type="button"
          class="flex size-8 cursor-pointer items-center justify-center rounded"
          :class="{ 'hover:bg-gray-200 active:!bg-gray-300': ready }"
          @click="handleCopyCode"
        >
          <MingcuteCheckCircleLine
            v-if="!ready"
            class="size-4 text-green-500"
          />
          <MingcuteCopyLine v-else class="size-4 text-gray-500" />
        </button>
      </div>
    </div>
    <pre v-show="!collapsed"><node-view-content as="code" class="hljs" /></pre>
  </node-view-wrapper>
</template>
