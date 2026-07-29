<script setup lang="ts">
import { computed } from "vue";
import Input from "@/components/base/Input.vue";
import { ExtensionImage, ExtensionLink } from "@/extensions";
import { i18n } from "@/locales";
import type { BubbleItemComponentProps } from "@/types";
import { isAllowedUri } from "@/utils/is-allowed-uri";

const props = defineProps<BubbleItemComponentProps>();

const getImageLinkAttributes = () => {
  const imageAttrs = props.editor.getAttributes(ExtensionImage.name);
  if (imageAttrs.href) {
    return imageAttrs;
  }
  return props.editor.getAttributes(ExtensionLink.name);
};

const updateImageLinkAttributes = (
  attributes: Record<string, string | null>
) => {
  if (attributes.href && !isAllowedUri(attributes.href)) {
    return;
  }
  const imagePosition = props.editor.state.selection.from;
  props.editor
    .chain()
    .unsetLink()
    .updateAttributes(ExtensionImage.name, attributes)
    .setNodeSelection(imagePosition)
    .run();
};

const href = computed({
  get: () => getImageLinkAttributes().href,
  set: (href: string) => {
    const currentAttributes = getImageLinkAttributes();
    updateImageLinkAttributes({
      href,
      target: currentAttributes.href
        ? currentAttributes.target || "_self"
        : "_blank",
    });
  },
});

const target = computed({
  get() {
    return getImageLinkAttributes().target === "_blank";
  },
  set(value) {
    updateImageLinkAttributes({
      href: href.value,
      target: value ? "_blank" : "_self",
    });
  },
});
</script>

<template>
  <div class="w-80">
    <Input
      v-if="visible?.({ editor: props.editor })"
      v-model="href"
      auto-focus
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
