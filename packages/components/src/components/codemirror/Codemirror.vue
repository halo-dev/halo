<script lang="ts" setup>
import type { PropType } from "vue";
import { onBeforeUnmount, onMounted, shallowRef, watch } from "vue";
import type { EditorStateConfig } from "@codemirror/state";
import { EditorState } from "@codemirror/state";
import { EditorView } from "@codemirror/view";
import { basicSetup } from "codemirror";
import { StreamLanguage } from "@codemirror/language";
import { yaml } from "@codemirror/legacy-modes/mode/yaml";

const languages = {
  yaml: StreamLanguage.define(yaml),
};

const props = defineProps({
  modelValue: {
    type: String,
    default: "",
  },
  height: {
    type: String,
    default: "auto",
  },
  language: {
    type: String as PropType<"yaml">,
    default: "yaml",
  },
  extensions: {
    type: Array as PropType<EditorStateConfig["extensions"]>,
    default: () => [],
  },
});

const emit = defineEmits<{
  (e: "update:modelValue", value: string): void;
  (e: "change", value: string): void;
}>();

const customTheme = EditorView.theme({
  "&": {
    height: props.height,
  },
});

const wrapper = shallowRef<HTMLDivElement>();
const cmState = shallowRef<EditorState>();
const cmView = shallowRef<EditorView>();

const createCmEditor = () => {
  let extensions = [
    basicSetup,
    EditorView.lineWrapping,
    customTheme,
    languages[props.language],
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
