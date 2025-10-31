<script lang="ts" setup>
import type { Editor } from "@/tiptap";
import { useFileDialog } from "@vueuse/core";
import { onMounted, type Component } from "vue";
import Gallery from "./index";

const props = defineProps<{
  editor: Editor;
  visible: ({ editor }: { editor: Editor }) => boolean;
  isActive: ({ editor }: { editor: Editor }) => boolean;
  title: string;
  icon: Component;
  iconStyle: string;
  action: ({ editor }: { editor: Editor }) => void;
}>();
const emit = defineEmits(["close"]);

const { open: openFileDialog, onChange } = useFileDialog({
  accept: "image/*",
  multiple: true,
  reset: true,
});

onChange((files) => {
  if (files && files.length > 0) {
    const currentImages = props.editor.getAttributes(Gallery.name).images || [];
    const imageFiles = Array.from(files).filter((file) =>
      file.type.startsWith("image/")
    );
    const newBlobUrls = imageFiles.map((file) => URL.createObjectURL(file));

    props.editor
      .chain()
      .updateAttributes(Gallery.name, {
        images: [...currentImages, ...newBlobUrls],
      })
      .setNodeSelection(props.editor.state.selection.from)
      .focus()
      .run();

    emit("close");
  }
});

onMounted(() => {
  openFileDialog();
});
</script>

<template>
  <button
    :title="title"
    class="rounded-md p-2 text-lg text-gray-600 hover:bg-gray-100"
    @click="props?.action({ editor })"
  >
    <component :is="icon" :style="iconStyle" class="h-5 w-5" />
  </button>
</template>
