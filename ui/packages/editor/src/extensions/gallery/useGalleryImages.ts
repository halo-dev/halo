import type { Editor } from "@/tiptap";
import type { Attachment } from "@halo-dev/api-client";
import { useFileDialog } from "@vueuse/core";
import { computed, ref } from "vue";
import { uploadFile } from "../../utils/upload";
import { ExtensionGallery, type ExtensionGalleryImageItem } from "./index";

export function getCurrentGalleryImages(
  editor: Editor
): ExtensionGalleryImageItem[] {
  return editor.getAttributes(ExtensionGallery.name).images || [];
}

export function updateGalleryImages(
  editor: Editor,
  images: ExtensionGalleryImageItem[],
  focus = true
) {
  const chain = editor
    .chain()
    .updateAttributes(ExtensionGallery.name, { images })
    .setNodeSelection(editor.state.selection.from);

  if (focus) {
    chain.focus();
  }

  chain.run();
}

export function useUploadGalleryImage(editor: Editor) {
  const controller = ref<AbortController>();

  const { open: openFileDialog, onChange } = useFileDialog({
    accept: "image/*",
    multiple: true,
    reset: true,
  });

  onChange((files) => {
    if (!files) {
      return;
    }
    if (files.length > 0) {
      handleFiles(Array.from(files));
    }
  });

  const handleFiles = (files: File[]) => {
    for (const file of files) {
      handleUploadFile(file);
    }
  };

  const uploadImage = computed(() => {
    return editor.extensionManager.extensions.find(
      (extension) => extension.name === ExtensionGallery.name
    )?.options.uploadImage;
  });

  /**
   *
   * Upload files to the attachment library.
   *
   * @param file attachments that need to be uploaded to the attachment library
   */
  const handleUploadFile = (file: File) => {
    if (!uploadImage.value) {
      return;
    }
    controller.value = new AbortController();
    uploadFile(file, uploadImage.value, {
      controller: controller.value,
      onUploadProgress: () => {},

      onFinish: (attachment?: Attachment) => {
        if (attachment) {
          editor.commands.updateAttributes(ExtensionGallery.name, {
            images: [
              ...getCurrentGalleryImages(editor),
              {
                src: attachment.status?.permalink,
                aspectRatio: 0,
              },
            ],
          });
        }
      },

      onError: () => {},
    });
  };

  return {
    openFileDialog,
  };
}
