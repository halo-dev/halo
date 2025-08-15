import { useGlobalInfoStore } from "@/stores/global-info";
import { ucApiClient } from "@halo-dev/api-client";
import { Toast } from "@halo-dev/components";
import type { AttachmentLike } from "@halo-dev/console-shared";
import { computed, ref, type Ref } from "vue";
import { useI18n } from "vue-i18n";
import type { AttachmentAttr } from "../utils/attachment";

interface useAttachmentSelectReturn {
  onAttachmentSelect: (attachments: AttachmentLike[]) => void;
  attachmentResult: AttachmentResult;
}

export interface AttachmentResult {
  updateAttachment: (attachments: AttachmentLike[]) => void;
}

export function useAttachmentSelect(): useAttachmentSelectReturn {
  const attachmentResult = {
    updateAttachment: (attachments: AttachmentLike[]) => {
      return attachments;
    },
  };
  const onAttachmentSelect = (attachmentLikes: AttachmentLike[]) => {
    attachmentResult.updateAttachment(attachmentLikes);
  };

  return {
    onAttachmentSelect,
    attachmentResult,
  };
}

export function useExternalAssetsTransfer(
  src: Ref<string | undefined>,
  callback: (attachment: AttachmentAttr) => void
) {
  const { globalInfo } = useGlobalInfoStore();
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
      url: data.status?.permalink,
      name: data.spec.displayName,
    });

    Toast.success(t("core.common.toast.save_success"));

    transferring.value = false;
  }

  return {
    isExternalAsset,
    transferring,
    handleTransfer,
  };
}
