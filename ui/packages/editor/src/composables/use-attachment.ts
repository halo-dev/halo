import { Toast } from "@halo-dev/components";
import { stores, utils, type AttachmentSimple } from "@halo-dev/ui-shared";
import { computed, ref, watch, type Ref } from "vue";
import type { ExtensionUploadStorage } from "@/extensions/upload";
import { i18n } from "@/locales";
import type { Editor } from "@/tiptap";
import { isExternalAsset as isTransferableExternalAsset } from "@/utils/upload";

export function useExternalAssetsTransfer(
  src: Ref<string | undefined>,
  callback: (attachment: AttachmentSimple) => void,
  editor: Editor
) {
  const { globalInfo } = stores.globalInfo();

  const uploadStorage = computed(() => {
    return (editor.storage as { upload?: ExtensionUploadStorage }).upload;
  });

  watch(
    src,
    (value) => {
      if (!value || !uploadStorage.value?.upload) {
        return;
      }
      if (
        !isTransferableExternalAsset(
          value,
          globalInfo?.externalUrl || globalThis.window?.location.origin || ""
        )
      ) {
        return;
      }
      void uploadStorage.value
        .matchAttachmentPermalinks([value])
        .catch((error) => {
          console.error("Failed to match attachment permalinks:", error);
        });
    },
    {
      immediate: true,
    }
  );

  const isExternalAsset = computed(() => {
    const storage = uploadStorage.value;

    if (!src.value || !storage?.upload) {
      return false;
    }

    if (
      !isTransferableExternalAsset(
        src.value,
        globalInfo?.externalUrl || globalThis.window?.location.origin || ""
      )
    ) {
      return false;
    }

    const cacheVersion = storage.cacheVersion.value;
    return cacheVersion >= 0 && storage.matchCache.get(src.value) === false;
  });

  const transferring = ref(false);

  async function handleTransfer() {
    const storage = uploadStorage.value;
    if (!src.value || !storage?.upload) {
      return;
    }

    transferring.value = true;

    try {
      await storage.matchAttachmentPermalinks([src.value]);
      if (storage.matchCache.get(src.value)) {
        return;
      }

      const uploadedAttachment = await storage.upload(src.value);
      if (!uploadedAttachment) {
        return;
      }

      const attachment = utils.attachment.convertToSimple(uploadedAttachment);
      if (!attachment?.url) {
        return;
      }

      callback(attachment);
      Toast.success(i18n.global.t("editor.common.toast.save_success"));
    } catch (error) {
      console.error("Failed to upload external asset:", error);
    } finally {
      transferring.value = false;
    }
  }

  return {
    isExternalAsset,
    transferring,
    handleTransfer,
  };
}
