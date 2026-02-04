import type { Editor } from "@/tiptap";
import { i18n } from "@/locales";
import type { Attachment } from "@halo-dev/api-client";
import { Toast } from "@halo-dev/components";
import { useFileDialog } from "@vueuse/core";
import { computed, onScopeDispose, ref } from "vue";
import { uploadFile } from "../../utils/upload";
import {
  createGalleryImageItem,
  ExtensionGallery,
  type ExtensionGalleryImageItem,
} from "./index";

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
  const UPLOAD_PROGRESS_BUCKET_SIZE = 25;
  const controllers = ref<AbortController[]>([]);

  onScopeDispose(() => {
    controllers.value.forEach((c) => c.abort());
    controllers.value = [];
  });

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

    let lastReportedProgress = -1;
    const reportProgress = (progress: number) => {
      if (!Number.isFinite(progress) || progress <= 0 || progress >= 100) {
        return;
      }
      const bucket =
        Math.floor(progress / UPLOAD_PROGRESS_BUCKET_SIZE) *
        UPLOAD_PROGRESS_BUCKET_SIZE;
      if (bucket > lastReportedProgress) {
        lastReportedProgress = bucket;
        Toast.info(
          `${i18n.global.t("editor.extensions.upload.loading")} ${file.name} (${bucket}%)`
        );
      }
    };

    Toast.info(`${i18n.global.t("editor.extensions.upload.loading")} ${file.name}`, {
      duration: 6000,
    });

    const controller = new AbortController();
    controllers.value = [...controllers.value, controller];
    uploadFile(file, uploadImage.value, {
      controller,
      onUploadProgress: reportProgress,

      onFinish: (attachment?: Attachment) => {
        controllers.value = controllers.value.filter((c) => c !== controller);
        if (attachment) {
          editor.commands.updateAttributes(ExtensionGallery.name, {
            images: [
              ...getCurrentGalleryImages(editor),
              createGalleryImageItem(attachment.status?.permalink || ""),
            ],
          });
        }
      },

      onError: (error: Error) => {
        controllers.value = controllers.value.filter((c) => c !== controller);
        Toast.error(
          `${i18n.global.t("editor.extensions.upload.error")} - ${error.message}`
        );
      },
    });
  };

  return {
    openFileDialog,
  };
}
