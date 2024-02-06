<script lang="ts" setup>
import { onBeforeUnmount, onMounted, shallowRef, watch } from "vue";
import type { EditorStateConfig } from "@codemirror/state";
import { EditorState } from "@codemirror/state";
import { EditorView } from "@codemirror/view";
import { basicSetup } from "codemirror";
import { LanguageSupport, StreamLanguage } from "@codemirror/language";
import { yaml } from "@codemirror/legacy-modes/mode/yaml";
import { html } from "@codemirror/lang-html";
import { javascript } from "@codemirror/lang-javascript";
import { css } from "@codemirror/lang-css";
import { json } from "@codemirror/lang-json";

const presetLanguages = {
  yaml: StreamLanguage.define(yaml),
  html: html(),
  javascript: javascript({
    jsx: true,
    typescript: true,
  }),
  css: css(),
  json: json(),
};

const props = withDefaults(
  defineProps<{
    modelValue?: string;
    height?: string;
    language: keyof typeof presetLanguages | LanguageSupport;
    extensions?: EditorStateConfig["extensions"];
  }>(),
  {
    modelValue: "",
    height: "auto",
    language: "yaml",
    extensions: () => [],
  }
);

const emit = defineEmits<{
  (e: "update:modelValue", value: string): void;
  (e: "change", value: string): void;
}>();

const customTheme = EditorView.theme({
  "&": {
    height: props.height,
    width: "100%",
  },
});

const wrapper = shallowRef<HTMLDivElement>();
const cmState = shallowRef<EditorState>();
const cmView = shallowRef<EditorView>();

const createCmEditor = () => {
  const language =
    typeof props.language === "string"
      ? presetLanguages[props.language]
      : props.language;

  let extensions = [
    basicSetup,
    EditorView.lineWrapping,
    customTheme,
    language,
    EditorView.updateListener.of((viewUpdate) => {
      if (viewUpdate.docChanged) {
        const doc = viewUpdate.state.doc.toString();
        emit("update:modelValue", doc);
        emit("change", doc);
      }
    }),
  ];

  if (props.extensions) {
    extensions = extensions.concat(props.extensions);
  }

  cmState.value = EditorState.create({
    doc: props.modelValue,
    extensions,
  });

  cmView.value = new EditorView({
    state: cmState.value,
    parent: wrapper.value,
  });
};

onMounted(() => {
  createCmEditor();

  // Update the codemirror editor doc when the model value changes.
  watch(
    () => props.modelValue,
    (newValue) => {
      if (newValue !== cmView.value?.state.doc.toString()) {
        cmView.value?.dispatch({
          changes: {
            from: 0,
            to: cmView.value?.state.doc.length,
            insert: newValue,
          },
        });
      }
    }
  );
});

// Destroy codemirror editor when component unmounts
onBeforeUnmount(() => {
  if (cmView.value) {
    cmView.value.destroy();
  }
});
</script>
<template>
  <div ref="wrapper" class="codemirror-wrapper contents"></div>
</template>
