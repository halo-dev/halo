<script lang="ts" setup>
import { i18n } from "@/locales";
import { type Editor } from "@/tiptap/vue-3";
import { TextSelection } from "@tiptap/pm/state";
import { Dropdown as VDropdown, vTooltip } from "floating-vue";
import { test } from "linkifyjs";
import { computed, type Component } from "vue";
import MdiLinkVariant from "~icons/mdi/link-variant";

const props = defineProps<{
  editor: Editor;
  isActive: ({ editor }: { editor: Editor }) => boolean;
  visible?: ({ editor }: { editor: Editor }) => boolean;
  icon?: Component;
  title?: string;
  action?: ({ editor }: { editor: Editor }) => void;
}>();

const href = computed({
  get() {
    const attrs = props.editor.getAttributes("link");
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
    const attrs = props.editor.getAttributes("link");
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
    const attrs = props.editor.getAttributes("link");
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
  if (props.isActive({ editor: props.editor })) {
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
        isActive({ editor })
          ? i18n.global.t('editor.extensions.link.edit_link')
          : i18n.global.t('editor.extensions.link.add_link')
      "
      class="rounded-md p-2 text-lg text-gray-600 hover:bg-gray-100"
      :class="{ 'bg-gray-200 !text-black': isActive({ editor }) }"
    >
      <MdiLinkVariant />
    </button>

    <template #popper>
      <div
        class="relative max-h-72 w-96 overflow-hidden overflow-y-auto rounded-md bg-white p-1 shadow"
      >
        <input
          v-model.lazy="href"
          :placeholder="i18n.global.t('editor.extensions.link.placeholder')"
          class="block w-full rounded-md border border-gray-300 bg-gray-50 px-2 py-1.5 text-sm text-gray-900 hover:bg-gray-100 focus:border-blue-500 focus:ring-blue-500"
        />
        <label class="mr-2 mt-2 inline-flex items-center">
          <input
            v-model="target"
            type="checkbox"
            class="form-checkbox rounded border-gray-300 text-blue-600 focus:ring-blue-500"
          />
          <span class="ml-2 text-sm text-gray-500">
            {{ i18n.global.t("editor.extensions.link.open_in_new_window") }}
          </span>
        </label>
        <label class="mt-2 inline-flex items-center">
          <!-- nofollow -->
          <input
            v-model="rel"
            type="checkbox"
            class="form-checkbox rounded border-gray-300 text-blue-600 focus:ring-blue-500"
          />
          <span class="ml-2 text-sm text-gray-500">
            {{ i18n.global.t("editor.extensions.link.nofollow") }}
          </span>
        </label>
      </div>
    </template>
  </VDropdown>
</template>
