<script setup lang="ts">
import Input from "@/components/base/Input.vue";
import { ExtensionImage, ExtensionLink } from "@/extensions";
import { i18n } from "@/locales";
import type { BubbleItemComponentProps } from "@/types";
import { computed } from "vue";

const props = defineProps<BubbleItemComponentProps>();

const href = computed({
  get: () => {
    const attrs = props.editor.getAttributes(ExtensionLink.name);
    return attrs?.href || props.editor.getAttributes(ExtensionImage.name).href;
  },
  set: (href: string) => {
    props.editor.commands.setLink({ href: href, target: "_blank" });
  },
});

const target = computed({
  get() {
    const attrs = props.editor.getAttributes(ExtensionLink.name);
    return attrs?.target === "_blank";
  },
  set(value) {
    props.editor.commands.setLink({
      href: href.value,
      target: value ? "_blank" : "_self",
    });
  },
});
</script>

<template>
  <div class="w-60">
    <Input
      v-if="visible?.({ editor: props.editor })"
      v-model="href"
      :placeholder="i18n.global.t('editor.common.placeholder.alt_href')"
      :label="i18n.global.t('editor.extensions.image.href_input_label')"
    />
    <label class="mt-3 inline-flex items-center">
      <input v-model="target" type="checkbox" />
      <span class="ml-2 text-sm text-gray-500">
        {{ i18n.global.t("editor.extensions.link.open_in_new_window") }}
      </span>
    </label>
  </div>
</template>
