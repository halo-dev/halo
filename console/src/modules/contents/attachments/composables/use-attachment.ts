import type { Attachment, Group, Policy, User } from "@halo-dev/api-client";
import { computed, type Ref } from "vue";
import { ref, watch } from "vue";
import type { AttachmentLike } from "@halo-dev/console-shared";
import { apiClient } from "@/utils/api-client";
import { Dialog, Toast } from "@halo-dev/components";
import type { Content, Editor } from "@halo-dev/richtext-editor";
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
  handleDelete: (attachment: Attachment) => void;
  handleDeleteInBatch: () => void;
  handleCheckAll: (checkAll: boolean) => void;
  handleSelect: (attachment: Attachment | undefined) => void;
  isChecked: (attachment: Attachment) => boolean;
  handleReset: () => void;
}

interface useAttachmentSelectReturn {
  onAttachmentSelect: (attachments: AttachmentLike[]) => void;
}

export function useAttachmentControl(filterOptions: {
  policy?: Ref<Policy | undefined>;
  group?: Ref<Group | undefined>;
  user?: Ref<User | undefined>;
  keyword?: Ref<string | undefined>;
  sort?: Ref<string | undefined>;
  page: Ref<number>;
  size: Ref<number>;
}): useAttachmentControlReturn {
  const { t } = useI18n();

  const { user, policy, group, keyword, sort, page, size } = filterOptions;

  const selectedAttachment = ref<Attachment>();
  const selectedAttachments = ref<Set<Attachment>>(new Set<Attachment>());
  const checkedAll = ref(false);

  const total = ref(0);
  const hasPrevious = ref(false);
  const hasNext = ref(false);

  const { data, isLoading, isFetching, refetch } = useQuery<Attachment[]>({
    queryKey: ["attachments", policy, keyword, group, user, page, size, sort],
    queryFn: async () => {
      const { data } = await apiClient.attachment.searchAttachments({
        policy: policy?.value?.metadata.name,
        displayName: keyword?.value,
        group: group?.value?.metadata.name,
        ungrouped: group?.value?.metadata.name === "ungrouped",
        uploadedBy: user?.value?.metadata.name,
        page: page?.value,
        size: size?.value,
        sort: [sort?.value as string].filter(Boolean),
      });

      total.value = data.total;
      hasPrevious.value = data.hasPrevious;
      hasNext.value = data.hasNext;

      return data.items;
    },
    refetchInterval(data) {
      const deletingAttachments = data?.filter(
        (attachment) => !!attachment.metadata.deletionTimestamp
      );
      return deletingAttachments?.length ? 3000 : false;
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

    if (index === 0 && hasPrevious) {
      page.value--;
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

    if (index === data.value.length - 1 && hasNext) {
      page.value++;
      await refetch();
      selectedAttachment.value = data.value[0];
    }
  };

  const handleDelete = (attachment: Attachment) => {
    Dialog.warning({
      title: t("core.attachment.operations.delete.title"),
      description: t("core.common.dialog.descriptions.cannot_be_recovered"),
      confirmType: "danger",
      confirmText: t("core.common.buttons.confirm"),
      cancelText: t("core.common.buttons.cancel"),
      onConfirm: async () => {
        try {
          await apiClient.extension.storage.attachment.deletestorageHaloRunV1alpha1Attachment(
            {
              name: attachment.metadata.name,
            }
          );
          if (
            selectedAttachment.value?.metadata.name === attachment.metadata.name
          ) {
            selectedAttachment.value = undefined;
          }
          selectedAttachments.value.delete(attachment);

          Toast.success(t("core.common.toast.delete_success"));
        } catch (e) {
          console.error("Failed to delete attachment", e);
        } finally {
          await refetch();
        }
      },
    });
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
              return apiClient.extension.storage.attachment.deletestorageHaloRunV1alpha1Attachment(
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
    handleDelete,
    handleDeleteInBatch,
    handleCheckAll,
    handleSelect,
    isChecked,
    handleReset,
  };
}

export function useAttachmentSelect(
  editor: Ref<Editor | undefined>
): useAttachmentSelectReturn {
  const onAttachmentSelect = (attachments: AttachmentLike[]) => {
    const contents: Content[] = attachments
      .map((attachment) => {
        if (typeof attachment === "string") {
          return {
            type: "image",
            attrs: {
              src: attachment,
            },
          };
        }

        if ("url" in attachment) {
          return {
            type: "image",
            attrs: {
              src: attachment.url,
              alt: attachment.type,
            },
          };
        }

        if ("spec" in attachment) {
          const { mediaType, displayName } = attachment.spec;
          const { permalink } = attachment.status || {};
          if (mediaType?.startsWith("image/")) {
            return {
              type: "image",
              attrs: {
                src: permalink,
                alt: displayName,
              },
            };
          }

          if (mediaType?.startsWith("video/")) {
            return {
              type: "video",
              attrs: {
                src: permalink,
              },
            };
          }

          if (mediaType?.startsWith("audio/")) {
            return {
              type: "audio",
              attrs: {
                src: permalink,
              },
            };
          }

          return {
            type: "text",
            marks: [
              {
                type: "link",
                attrs: {
                  href: permalink,
                },
              },
            ],
            text: displayName,
          };
        }
      })
      .filter(Boolean) as Content[];

    editor.value?.chain().focus().insertContent(contents).run();
  };

  return {
    onAttachmentSelect,
  };
}

export function useAttachmentPermalinkCopy(
  attachment: Ref<Attachment | undefined>
) {
  const { copy } = useClipboard();
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
