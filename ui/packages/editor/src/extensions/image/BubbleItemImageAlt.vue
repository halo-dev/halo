<script setup lang="ts">
import Input from "@/components/base/Input.vue";
import { i18n } from "@/locales";
import type { BubbleItemComponentProps } from "@/types";
import { computed } from "vue";
import { ExtensionImage } from "./index";

const props = defineProps<BubbleItemComponentProps>();

const alt = computed({
  get: () => {
    return props.editor.getAttributes(ExtensionImage.name).alt;
  },
  set: (alt: string) => {
    props.editor
      .chain()
      .updateAttributes(ExtensionImage.name, { alt: alt })
      .setNodeSelection(props.editor.state.selection.from)
      .focus()
      .run();
  },
});
</script>

<template>
  <div v-if="visible?.({ editor: props.editor })" class="w-56">
    <Input
      v-model="alt"
      auto-focus
      :label="i18n.global.t('editor.common.alt')"
      :placeholder="i18n.global.t('editor.common.placeholder.alt_input')"
    />
  </div>
</template>
