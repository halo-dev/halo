<script lang="ts" setup>
import type { PMNode, VueEditor } from "@/tiptap";
import type { DragButtonType } from "@/types";
import { formatShortcut } from "@/utils";
import { Dropdown as VDropdown } from "floating-vue";
import { computed, ref, type Component } from "vue";
import MaterialSymbolsArrowForwardIosRounded from "~icons/material-symbols/arrow-forward-ios-rounded";
import EditorDragHandleMenu from "./EditorDragMenu.vue";

const props = defineProps<
  DragButtonType & { editor: VueEditor; node: PMNode | null; pos: number }
>();

const emit = defineEmits<{
  (e: "close"): () => void;
}>();

const showChildren = ref(false);

const componentRef = ref<Component | void>();
const handleDragButtonItemClick = async () => {
  const visible =
    props.visible?.({
      editor: props.editor,
      node: props.node,
      pos: props.pos,
    }) ?? true;
  if (!visible) {
    return;
  }
  const disabled =
    props.disabled?.({
      editor: props.editor,
      node: props.node,
      pos: props.pos,
    }) ?? false;
  if (disabled) {
    return;
  }
  if (props.action) {
    const callback = props.action?.({
      editor: props.editor,
      node: props.node,
      pos: props.pos,
      close: handleCloseDropdown,
    });

    const resolvedCallback =
      callback instanceof Promise ? await callback : callback;

    if (typeof resolvedCallback === "object" && resolvedCallback !== null) {
      componentRef.value = resolvedCallback;
    }

    if (resolvedCallback === undefined || resolvedCallback === true) {
      handleCloseDropdown();
    }
  } else {
    showChildren.value = !showChildren.value;
  }
};

const handleCloseDropdown = () => {
  showChildren.value = false;
  emit("close");
};

const isChildrenComponent = computed(() => {
  return (props.children?.items?.length || 0) > 0;
});

const displayTitle = computed(() => {
  return typeof props.title === "function"
    ? props.title({ editor: props.editor, node: props.node, pos: props.pos })
    : props.title;
});

const triggerClick = () => {
  handleDragButtonItemClick();
};

defineExpose({
  triggerClick,
});
</script>

<template>
  <VDropdown
    v-if="
      props.visible?.({
        editor: props.editor,
        node: props.node,
        pos: props.pos,
      }) ?? true
    "
    class="inline-flex"
    :triggers="[]"
    :placement="'right'"
    :shown="showChildren && isChildrenComponent"
    :distance="8"
    @hide="showChildren = false"
  >
    <component :is="props.component" v-if="props.component" v-bind="props" />
    <template v-else>
      <div
        :class="[
          'flex w-full rounded-lg p-1.5 text-sm text-gray-600',
          {
            'bg-gray-200 !text-black': props.isActive?.({
              editor: props.editor,
              node: props.node,
              pos: props.pos,
            }),
          },
          {
            'cursor-pointer hover:bg-gray-200/80 hover:text-gray-900':
              !props.isActive?.({
                editor: props.editor,
                node: props.node,
                pos: props.pos,
              }),
          },
          props.class,
        ]"
        @click="handleDragButtonItemClick()"
      >
        <button :title="displayTitle" class="flex-grow">
          <div class="flex items-center gap-2">
            <component
              :is="props.icon"
              :style="props.iconStyle"
              class="h-4 w-4"
            />
            <span class="flex flex-grow justify-start px-0.5">{{
              displayTitle
            }}</span>
          </div>
        </button>
        <div v-if="isChildrenComponent" class="flex items-center gap-2">
          <MaterialSymbolsArrowForwardIosRounded class="h-3 w-3" />
        </div>
        <div
          v-else-if="props.keyboard"
          class="flex items-center justify-center rounded-md border border-gray-200 bg-white p-0.5 px-1 font-sans text-xs font-bold text-gray-500"
        >
          {{ formatShortcut(props.keyboard) }}
        </div>
      </div>
    </template>

    <template #popper>
      <div class="relative overflow-hidden overflow-y-auto">
        <KeepAlive v-if="componentRef">
          <component
            :is="componentRef"
            v-bind="props"
            :items="props.children?.items ?? []"
            @close="handleCloseDropdown"
          ></component>
        </KeepAlive>
        <EditorDragHandleMenu
          v-else
          :class="'!min-w-full'"
          :editor="props.editor"
          :node="props.node"
          :pos="props.pos"
          :items="props.children?.items ?? []"
          @close="handleCloseDropdown"
        />
      </div>
    </template>
  </VDropdown>
</template>
<style>
.v-popper__popper.v-popper__popper--show-from .v-popper__wrapper {
  transform: scale(0.9);
}

.v-popper__popper.v-popper__popper--show-to .v-popper__wrapper {
  transform: none;
  transition: transform 0.1s;
}
</style>
