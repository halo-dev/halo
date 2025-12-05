<script lang="ts" setup>
import Input from "@/components/base/Input.vue";
import { i18n } from "@/locales";
import type { BubbleItemComponentProps } from "@/types";
import { computed } from "vue";
import { ExtensionAudio } from "./index";

const props = defineProps<BubbleItemComponentProps>();

const src = computed({
  get: () => {
    return props.editor.getAttributes(ExtensionAudio.name)?.src;
  },
  set: (src: string) => {
    props.editor
      .chain()
      .updateAttributes(ExtensionAudio.name, { src: src })
      .setNodeSelection(props.editor.state.selection.from)
      .focus()
      .run();
  },
});
</script>

<template>
  <div class="w-80">
    <Input
      v-model="src"
      :placeholder="i18n.global.t('editor.common.placeholder.link_input')"
      :label="i18n.global.t('editor.extensions.audio.src_input_label')"
    />
  </div>
</template>
