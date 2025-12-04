<script setup lang="ts">
import Input from "@/components/base/Input.vue";
import { i18n } from "@/locales";
import type { BubbleItemComponentProps } from "@/types";
import { computed } from "vue";
import { ExtensionImage } from "./index";

const props = defineProps<BubbleItemComponentProps>();

const src = computed({
  get: () => {
    return props.editor.getAttributes(ExtensionImage.name).src;
  },
  set: (src: string) => {
    props.editor
      .chain()
      .updateAttributes(ExtensionImage.name, { src: src })
      .setNodeSelection(props.editor.state.selection.from)
      .focus()
      .run();
  },
});
</script>

<template>
  <div v-if="visible?.({ editor: props.editor })" class="w-64">
    <Input
      v-model="src"
      :label="i18n.global.t('editor.extensions.image.src_input_label')"
      :placeholder="i18n.global.t('editor.common.placeholder.link_input')"
    />
  </div>
</template>
