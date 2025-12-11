<script lang="ts" setup>
import Input from "@/components/base/Input.vue";
import { i18n } from "@/locales";
import type { BubbleItemComponentProps } from "@/types";
import { VDropdown, vTooltip } from "@halo-dev/components";
import { TextSelection } from "@tiptap/pm/state";
import { test } from "linkifyjs";
import { computed } from "vue";
import MingcuteLinkLine from "~icons/mingcute/link-line";
import { ExtensionLink } from ".";

const props = defineProps<BubbleItemComponentProps>();

const href = computed({
  get() {
    const attrs = props.editor.getAttributes(ExtensionLink.name);
    return attrs?.href;
  },
  set(value) {
    props.editor.commands.setLink({
      href: value,
      target: target.value ? "_blank" : "_self",
      rel: rel.value ? "nofollow" : "",
    });
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
      rel: rel.value ? "nofollow" : "",
    });
  },
});

const rel = computed({
  get() {
    const attrs = props.editor.getAttributes(ExtensionLink.name);
    return attrs?.rel === "nofollow";
  },
  set(value) {
    props.editor.commands.setLink({
      href: href.value,
      target: target.value ? "_blank" : "_self",
      rel: value ? "nofollow" : "",
    });
  },
});

/**
 * Convert the currently selected text when clicking the link
 */
const handleLinkBubbleButton = () => {
  if (props.isActive?.({ editor: props.editor })) {
    return;
  }
  const { state } = props.editor;
  const { selection } = state;
  const { empty } = selection;

  if (selection instanceof TextSelection) {
    if (empty) {
      return false;
    }
    const { content } = selection.content();
    if (!content || content.childCount !== 1) {
      return false;
    }
    const text = content.firstChild?.textContent;
    if (text && test(text, "url")) {
      props.editor.commands.setLink({
        href: text,
        target: "_self",
        rel: "",
      });
    }
  }
};
</script>

<template>
  <VDropdown
    class="inline-flex"
    :triggers="['click']"
    :distance="10"
    @click="handleLinkBubbleButton"
  >
    <button
      v-tooltip="
        isActive?.({ editor })
          ? i18n.global.t('editor.extensions.link.edit_link')
          : i18n.global.t('editor.extensions.link.add_link')
      "
      class="inline-flex size-8 items-center justify-center rounded-md text-lg text-gray-600 hover:bg-gray-100 active:!bg-gray-200"
      :class="{ 'bg-gray-200 !text-black': isActive?.({ editor }) }"
    >
      <MingcuteLinkLine />
    </button>

    <template #popper>
      <div class="relative w-96">
        <Input
          v-model.lazy="href"
          auto-focus
          :placeholder="i18n.global.t('editor.extensions.link.placeholder')"
        />
        <label class="mr-2 mt-2 inline-flex items-center">
          <input v-model="target" type="checkbox" />
          <span class="ml-2 text-sm text-gray-500">
            {{ i18n.global.t("editor.extensions.link.open_in_new_window") }}
          </span>
        </label>
        <label class="mt-2 inline-flex items-center">
          <!-- nofollow -->
          <input v-model="rel" type="checkbox" />
          <span class="ml-2 text-sm text-gray-500">
            {{ i18n.global.t("editor.extensions.link.nofollow") }}
          </span>
        </label>
      </div>
    </template>
  </VDropdown>
</template>
