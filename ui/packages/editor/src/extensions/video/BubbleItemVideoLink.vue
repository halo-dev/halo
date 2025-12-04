<script setup lang="ts">
import Input from "@/components/base/Input.vue";
import { i18n } from "@/locales";
import type { BubbleItemComponentProps } from "@/types";
import { computed } from "vue";
import { ExtensionVideo } from "./index";

const props = defineProps<BubbleItemComponentProps>();

const src = computed({
  get: () => {
    return props.editor.getAttributes(ExtensionVideo.name).src;
  },
  set: (src: string) => {
    props.editor
      .chain()
      .updateAttributes(ExtensionVideo.name, { src: src })
      .run();
  },
});
</script>

<template>
  <div class="w-64">
    <Input
      v-model="src"
      :placeholder="i18n.global.t('editor.common.placeholder.link_input')"
      :label="i18n.global.t('editor.extensions.video.src_input_label')"
    />
  </div>
</template>
