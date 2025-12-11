import { ucApiClient } from "@halo-dev/api-client";
import { Toast } from "@halo-dev/components";
import { stores, type AttachmentSimple } from "@halo-dev/ui-shared";
import { computed, ref, type Ref } from "vue";
import { useI18n } from "vue-i18n";

export function useExternalAssetsTransfer(
  src: Ref<string | undefined>,
  callback: (attachment: AttachmentSimple) => void
) {
  const { globalInfo } = stores.globalInfo();
  const { t } = useI18n();

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

    const { data } =
      await ucApiClient.storage.attachment.externalTransferAttachment1({
        ucUploadFromUrlRequest: {
          url: src.value,
        },
        waitForPermalink: true,
      });

    callback({
      url: data.status?.permalink || "",
      alt: data.spec.displayName,
    });

    Toast.success(t("editor.common.toast.save_success"));

    transferring.value = false;
  }

  return {
    isExternalAsset,
    transferring,
    handleTransfer,
  };
}
