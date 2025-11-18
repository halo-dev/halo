<script lang="ts" setup>
import { Compartment, EditorState } from "@codemirror/state";
import { EditorView } from "@codemirror/view";
import { basicSetup } from "codemirror";
import { onBeforeUnmount, onMounted, shallowRef, watch } from "vue";
import { presetLanguages, type CodemirrorProps } from "./supports";

const props = withDefaults(defineProps<CodemirrorProps>(), {
  modelValue: "",
  height: "auto",
  language: "yaml",
  extensions: () => [],
});

const emit = defineEmits<{
  (e: "update:modelValue", value: string): void;
  (e: "change", value: string): void;
}>();

const wrapper = shallowRef<HTMLDivElement>();
const cmState = shallowRef<EditorState>();
const cmView = shallowRef<EditorView>();

const themeCompartment = new Compartment();

const createCustomTheme = (height: string) => {
  return EditorView.theme({
    "&": {
      height,
      width: "100%",
    },
  });
};

const createCmEditor = () => {
  const language =
    typeof props.language === "string"
      ? presetLanguages[props.language]
      : props.language;

  let extensions = [
    basicSetup,
    EditorView.lineWrapping,
    themeCompartment.of(createCustomTheme(props.height)),
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

  watch(
    () => props.height,
    (newHeight) => {
      if (cmView.value) {
        cmView.value.dispatch({
          effects: themeCompartment.reconfigure(createCustomTheme(newHeight)),
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
