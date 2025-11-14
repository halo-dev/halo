import type { Attachment } from "@halo-dev/api-client";
import { utils, type AttachmentLike } from "@halo-dev/console-shared";
import type { Editor } from "@halo-dev/richtext-editor";
import { useFileDialog } from "@vueuse/core";
import { computed, ref } from "vue";
import { uploadFile } from "../../utils/upload";
import Gallery, { type GalleryImage } from "./index";

export function convertAttachmentsToImages(
  attachments: AttachmentLike[]
): GalleryImage[] {
  const newImages: GalleryImage[] = [];
  for (const attachment of attachments) {
    const attachmentUrl = utils.attachment.getUrl(attachment);
    if (attachmentUrl) {
      newImages.push({
        src: attachmentUrl,
        aspectRatio: 0,
      });
    }
  }
  return newImages;
}

export function useAttachmentSelector(
  editor: Editor,
  onImagesAdded: (images: GalleryImage[]) => void
) {
  return () => {
    editor.commands.openAttachmentSelector(
      (attachments: AttachmentLike[]) => {
        if (attachments.length === 0) {
          return;
        }
        const newImages = convertAttachmentsToImages(attachments);
        onImagesAdded(newImages);
      },
      {
        accepts: ["image/*"],
        min: 1,
      }
    );
  };
}

export function getCurrentGalleryImages(editor: Editor): GalleryImage[] {
  return editor.getAttributes(Gallery.name).images || [];
}

export function updateGalleryImages(
  editor: Editor,
  images: GalleryImage[],
  focus = true
) {
  const chain = editor
    .chain()
    .updateAttributes(Gallery.name, { images })
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
      (extension) => extension.name === Gallery.name
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
          editor.commands.updateAttributes(Gallery.name, {
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
