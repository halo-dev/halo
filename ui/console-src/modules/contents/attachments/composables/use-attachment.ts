import type { Attachment } from "@halo-dev/api-client";
import { computed, nextTick, type Ref, ref, watch } from "vue";
import { apiClient } from "@/utils/api-client";
import { Dialog, Toast } from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import { useI18n } from "vue-i18n";
import { useClipboard } from "@vueuse/core";
import { matchMediaType } from "@/utils/media-type";

interface useAttachmentControlReturn {
  attachments: Ref<Attachment[] | undefined>;
  isLoading: Ref<boolean>;
  isFetching: Ref<boolean>;
  selectedAttachment: Ref<Attachment | undefined>;
  selectedAttachments: Ref<Set<Attachment>>;
  checkedAll: Ref<boolean>;
  total: Ref<number>;
  handleFetchAttachments: () => void;
  handleSelectPrevious: () => void;
  handleSelectNext: () => void;
  handleDeleteInBatch: () => void;
  handleCheckAll: (checkAll: boolean) => void;
  handleSelect: (attachment: Attachment | undefined) => void;
  isChecked: (attachment: Attachment) => boolean;
  handleReset: () => void;
}

export function useAttachmentControl(filterOptions: {
  policyName?: Ref<string | undefined>;
  groupName?: Ref<string | undefined>;
  user?: Ref<string | undefined>;
  accepts?: Ref<string[]>;
  keyword?: Ref<string | undefined>;
  sort?: Ref<string | undefined>;
  page: Ref<number>;
  size: Ref<number>;
}): useAttachmentControlReturn {
  const { t } = useI18n();

  const { user, policyName, groupName, keyword, sort, page, size, accepts } =
    filterOptions;

  const selectedAttachment = ref<Attachment>();
  const selectedAttachments = ref<Set<Attachment>>(new Set<Attachment>());
  const checkedAll = ref(false);

  const total = ref(0);
  const hasPrevious = ref(false);
  const hasNext = ref(false);

  const { data, isLoading, isFetching, refetch } = useQuery<Attachment[]>({
    queryKey: [
      "attachments",
      policyName,
      keyword,
      groupName,
      user,
      accepts,
      page,
      size,
      sort,
    ],
    queryFn: async () => {
      const isUnGrouped = groupName?.value === "ungrouped";

      const fieldSelectorMap: Record<string, string | undefined> = {
        "spec.policyName": policyName?.value,
        "spec.ownerName": user?.value,
        "spec.groupName": isUnGrouped ? undefined : groupName?.value,
      };

      const fieldSelector = Object.entries(fieldSelectorMap)
        .map(([key, value]) => {
          if (value) {
            return `${key}=${value}`;
          }
        })
        .filter(Boolean) as string[];

      const { data } = await apiClient.attachment.searchAttachments({
        fieldSelector,
        page: page.value,
        size: size.value,
        ungrouped: isUnGrouped,
        accepts: accepts?.value,
        keyword: keyword?.value,
        sort: [sort?.value as string].filter(Boolean),
      });

      total.value = data.total;
      hasPrevious.value = data.hasPrevious;
      hasNext.value = data.hasNext;

      return data.items;
    },
    refetchInterval(data) {
      const hasDeletingAttachment = data?.some(
        (attachment) => !!attachment.metadata.deletionTimestamp
      );
      return hasDeletingAttachment ? 1000 : false;
    },
  });

  const handleSelectPrevious = async () => {
    if (!data.value) return;

    const index = data.value?.findIndex(
      (attachment) =>
        attachment.metadata.name === selectedAttachment.value?.metadata.name
    );

    if (index === undefined) return;

    if (index > 0) {
      selectedAttachment.value = data.value[index - 1];
      return;
    }

    if (index === 0 && hasPrevious.value) {
      page.value--;
      await nextTick();
      await refetch();
      selectedAttachment.value = data.value[data.value.length - 1];
    }
  };

  const handleSelectNext = async () => {
    if (!data.value) return;

    const index = data.value?.findIndex(
      (attachment) =>
        attachment.metadata.name === selectedAttachment.value?.metadata.name
    );

    if (index === undefined) return;

    if (index < data.value?.length - 1) {
      selectedAttachment.value = data.value[index + 1];
      return;
    }

    if (index === data.value.length - 1 && hasNext.value) {
      page.value++;
      await nextTick();
      await refetch();
      selectedAttachment.value = data.value[0];
    }
  };

  const handleDeleteInBatch = () => {
    Dialog.warning({
      title: t("core.attachment.operations.delete_in_batch.title"),
      description: t("core.common.dialog.descriptions.cannot_be_recovered"),
      confirmType: "danger",
      confirmText: t("core.common.buttons.confirm"),
      cancelText: t("core.common.buttons.cancel"),
      onConfirm: async () => {
        try {
          const promises = Array.from(selectedAttachments.value).map(
            (attachment) => {
              return apiClient.extension.storage.attachment.deleteStorageHaloRunV1alpha1Attachment(
                {
                  name: attachment.metadata.name,
                }
              );
            }
          );
          await Promise.all(promises);
          selectedAttachments.value.clear();

          Toast.success(t("core.common.toast.delete_success"));
        } catch (e) {
          console.error("Failed to delete attachments", e);
        } finally {
          await refetch();
        }
      },
    });
  };

  const handleCheckAll = (checkAll: boolean) => {
    if (checkAll) {
      data.value?.forEach((attachment) => {
        selectedAttachments.value.add(attachment);
      });
    } else {
      selectedAttachments.value.clear();
    }
  };

  const handleSelect = async (attachment: Attachment | undefined) => {
    if (!attachment) return;
    if (selectedAttachments.value.has(attachment)) {
      selectedAttachments.value.delete(attachment);
      return;
    }
    selectedAttachments.value.add(attachment);
  };

  watch(
    () => selectedAttachments.value.size,
    (newValue) => {
      checkedAll.value = newValue === data.value?.length;
    }
  );

  const isChecked = (attachment: Attachment) => {
    return (
      attachment.metadata.name === selectedAttachment.value?.metadata.name ||
      Array.from(selectedAttachments.value)
        .map((item) => item.metadata.name)
        .includes(attachment.metadata.name)
    );
  };

  const handleReset = () => {
    page.value = 1;
    selectedAttachment.value = undefined;
    selectedAttachments.value.clear();
    checkedAll.value = false;
  };

  return {
    attachments: data,
    isLoading,
    isFetching,
    selectedAttachment,
    selectedAttachments,
    checkedAll,
    total,
    handleFetchAttachments: refetch,
    handleSelectPrevious,
    handleSelectNext,
    handleDeleteInBatch,
    handleCheckAll,
    handleSelect,
    isChecked,
    handleReset,
  };
}

export function useAttachmentPermalinkCopy(
  attachment: Ref<Attachment | undefined>
) {
  const { copy } = useClipboard({ legacy: true });
  const { t } = useI18n();

  const mediaType = computed(() => {
    return attachment.value?.spec.mediaType;
  });

  const isImage = computed(() => {
    return mediaType.value && matchMediaType(mediaType.value, "image/*");
  });

  const isVideo = computed(() => {
    return mediaType.value && matchMediaType(mediaType.value, "video/*");
  });

  const isAudio = computed(() => {
    return mediaType.value && matchMediaType(mediaType.value, "audio/*");
  });

  const htmlText = computed(() => {
    const { permalink } = attachment.value?.status || {};
    const { displayName } = attachment.value?.spec || {};

    if (isImage.value) {
      return `<img src="${permalink}" alt="${displayName}" />`;
    } else if (isVideo.value) {
      return `<video src="${permalink}"></video>`;
    } else if (isAudio.value) {
      return `<audio src="${permalink}"></audio>`;
    }
    return `<a href="${permalink}">${displayName}</a>`;
  });

  const markdownText = computed(() => {
    const { permalink } = attachment.value?.status || {};
    const { displayName } = attachment.value?.spec || {};
    if (isImage.value) {
      return `![${displayName}](${permalink})`;
    }
    return `[${displayName}](${permalink})`;
  });

  const handleCopy = (format: "markdown" | "html" | "url") => {
    const { permalink } = attachment.value?.status || {};

    if (!permalink) return;

    if (format === "url") {
      copy(permalink);
    } else if (format === "markdown") {
      copy(markdownText.value);
    } else if (format === "html") {
      copy(htmlText.value);
    }

    Toast.success(t("core.common.toast.copy_success"));
  };

  return {
    htmlText,
    markdownText,
    handleCopy,
  };
}
