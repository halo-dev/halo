import { i18n } from "@/locales";
import type { Attachment } from "@halo-dev/api-client";
import { ucApiClient } from "@halo-dev/api-client";
import { Toast } from "@halo-dev/components";
import { stores, type AttachmentSimple } from "@halo-dev/ui-shared";
import { computed, ref, type Ref } from "vue";

export function useExternalAssetsTransfer(
  src: Ref<string | undefined>,
  callback: (attachment: AttachmentSimple) => void,
  uploadFromUrl?: (url: string) => Promise<Attachment>
) {
  const { globalInfo } = stores.globalInfo();

  const isExternalAsset = computed(() => {
    if (src.value?.startsWith("/")) {
      return false;
    }

    if (!globalInfo?.externalUrl) {
      return false;
    }

    return !src.value?.startsWith(globalInfo?.externalUrl);
  });

  const transferring = ref(false);

  async function handleTransfer() {
    if (!src.value) {
      return;
    }

    transferring.value = true;

    const uploadFn =
      uploadFromUrl ??
      (async (url: string) => {
        const { data } =
          await ucApiClient.storage.attachment.uploadAttachmentForUc({ url });
        return data;
      });

    try {
      const data = await uploadFn(src.value);

      callback({
        url: data.status?.permalink || "",
        alt: data.spec.displayName,
      });

      Toast.success(i18n.global.t("editor.common.toast.save_success"));
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
