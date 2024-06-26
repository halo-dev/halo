<script lang="ts" setup>
import MdiDeleteForeverOutline from "@/components/icon/MdiDeleteForeverOutline.vue";
import { i18n } from "@/locales";
import type { Editor } from "@/tiptap/vue-3";
import MdiArrowULeftBottom from "~icons/mdi/arrow-u-left-bottom";
import BlockActionButton from "./BlockActionButton.vue";
import BlockActionSeparator from "./BlockActionSeparator.vue";

const props = withDefaults(
  defineProps<{
    selected: boolean;
    editor: Editor;
    getPos: () => number;
    deleteNode: () => void;
  }>(),
  {
    selected: false,
  }
);

function handleInsertNewLine() {
  props.editor.commands.insertContentAt(
    props.getPos() + 1,
    [{ type: "paragraph", content: "" }],
    {
      updateSelection: true,
    }
  );
  props.editor.commands.focus(props.getPos() + 2, {
    scrollIntoView: true,
  });
}
</script>

<template>
  <section
    class="editor-block group"
    :class="{ 'editor-block--selected': selected }"
  >
    <div class="editor-block__content">
      <slot name="content"></slot>
    </div>
    <div
      class="invisible group-hover:visible pb-2 absolute -top-12 right-0"
      :class="{ '!visible': selected }"
    >
      <div class="editor-block__actions">
        <slot name="actions"></slot>

        <!-- @vue-ignore -->
        <BlockActionButton
          :tooltip="i18n.global.t('editor.common.button.new_line')"
          @click="handleInsertNewLine"
        >
          <template #icon>
            <MdiArrowULeftBottom />
          </template>
        </BlockActionButton>

        <BlockActionSeparator />

        <BlockActionButton
          :tooltip="i18n.global.t('editor.common.button.delete')"
          @click="deleteNode"
        >
          <template #icon>
            <MdiDeleteForeverOutline />
          </template>
        </BlockActionButton>
      </div>
    </div>
  </section>
</template>

<style lang="scss">
.editor-block {
  @apply relative my-9;

  &__content {
    @apply transition-all
    rounded;
  }

  &__actions {
    @apply p-1 flex flex-row rounded-lg border gap-0.5 items-center bg-gray-100 h-11 shadow-lg;
  }

  &:hover & {
    &__content {
      @apply bg-gray-50;
    }
  }

  &--selected & {
    &__content {
      @apply bg-gray-50;
    }
  }
}
</style>
