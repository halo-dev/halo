import type {
  Attachment,
  AttachmentList,
  Group,
  Policy,
  User,
} from "@halo-dev/api-client";
import type { Ref } from "vue";
import { ref, watch } from "vue";
import type { AttachmentLike } from "@halo-dev/console-shared";
import { apiClient } from "@/utils/api-client";
import { Dialog, Toast } from "@halo-dev/components";
import type { Content, Editor } from "@halo-dev/richtext-editor";
import { onBeforeRouteLeave } from "vue-router";

interface useAttachmentControlReturn {
  attachments: Ref<AttachmentList>;
  loading: Ref<boolean>;
  selectedAttachment: Ref<Attachment | undefined>;
  selectedAttachments: Ref<Set<Attachment>>;
  checkedAll: Ref<boolean>;
  handleFetchAttachments: (options?: { mute?: boolean; page?: number }) => void;
  handlePaginationChange: ({
    page,
    size,
  }: {
    page: number;
    size: number;
  }) => void;
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

export function useAttachmentControl(filterOptions?: {
  policy?: Ref<Policy | undefined>;
  group?: Ref<Group | undefined>;
  user?: Ref<User | undefined>;
  keyword?: Ref<string | undefined>;
  sort?: Ref<string | undefined>;
}): useAttachmentControlReturn {
  const { user, policy, group, keyword, sort } = filterOptions || {};

  const attachments = ref<AttachmentList>({
    page: 1,
    size: 60,
    total: 0,
    items: [],
    first: true,
    last: false,
    hasNext: false,
    hasPrevious: false,
    totalPages: 0,
  });
  const loading = ref<boolean>(false);

  const selectedAttachment = ref<Attachment>();
  const selectedAttachments = ref<Set<Attachment>>(new Set<Attachment>());
  const checkedAll = ref(false);
  const refreshInterval = ref();

  const handleFetchAttachments = async (options?: {
    mute?: boolean;
    page?: number;
  }) => {
    try {
      clearInterval(refreshInterval.value);

      if (!options?.mute) {
        loading.value = true;
      }

      if (options?.page) {
        attachments.value.page = options.page;
      }

      const { data } = await apiClient.attachment.searchAttachments({
        policy: policy?.value?.metadata.name,
        displayName: keyword?.value,
        group: group?.value?.metadata.name,
        ungrouped: group?.value?.metadata.name === "ungrouped",
        uploadedBy: user?.value?.metadata.name,
        page: attachments.value.page,
        size: attachments.value.size,
        sort: [sort?.value as string].filter(Boolean),
      });
      attachments.value = data;

      const deletedAttachments = attachments.value.items.filter(
        (attachment) => !!attachment.metadata.deletionTimestamp
      );

      if (deletedAttachments.length) {
        refreshInterval.value = setInterval(() => {
          handleFetchAttachments({ mute: true });
        }, 3000);
      }
    } catch (e) {
      console.error("Failed to fetch attachments", e);
    } finally {
      loading.value = false;
    }
  };

  onBeforeRouteLeave(() => {
    clearInterval(refreshInterval.value);
  });

  const handlePaginationChange = async ({
    page,
    size,
  }: {
    page: number;
    size: number;
  }) => {
    attachments.value.page = page;
    attachments.value.size = size;
    await handleFetchAttachments();
  };

  const handleSelectPrevious = async () => {
    const { items, hasPrevious } = attachments.value;
    const index = items.findIndex(
      (attachment) =>
        attachment.metadata.name === selectedAttachment.value?.metadata.name
    );
    if (index > 0) {
      selectedAttachment.value = items[index - 1];
      return;
    }
    if (index === 0 && hasPrevious) {
      attachments.value.page--;
      await handleFetchAttachments();
      selectedAttachment.value =
        attachments.value.items[attachments.value.items.length - 1];
    }
  };

  const handleSelectNext = async () => {
    const { items, hasNext } = attachments.value;
    const index = items.findIndex(
      (attachment) =>
        attachment.metadata.name === selectedAttachment.value?.metadata.name
    );
    if (index < items.length - 1) {
      selectedAttachment.value = items[index + 1];
      return;
    }
    if (index === items.length - 1 && hasNext) {
      attachments.value.page++;
      await handleFetchAttachments();
      selectedAttachment.value = attachments.value.items[0];
    }
  };

  const handleDelete = (attachment: Attachment) => {
    Dialog.warning({
      title: "确定要删除该附件吗？",
      description: "删除之后将无法恢复",
      confirmType: "danger",
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

          Toast.success("删除成功");
        } catch (e) {
          console.error("Failed to delete attachment", e);
        } finally {
          await handleFetchAttachments();
        }
      },
    });
  };

  const handleDeleteInBatch = () => {
    Dialog.warning({
      title: "确定要删除所选的附件吗？",
      description: "删除之后将无法恢复",
      confirmType: "danger",
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

          Toast.success("删除成功");
        } catch (e) {
          console.error("Failed to delete attachments", e);
        } finally {
          await handleFetchAttachments();
        }
      },
    });
  };

  const handleCheckAll = (checkAll: boolean) => {
    if (checkAll) {
      attachments.value.items.forEach((attachment) => {
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
      checkedAll.value = newValue === attachments.value.items?.length;
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
    attachments.value.page = 1;
    selectedAttachment.value = undefined;
    selectedAttachments.value.clear();
    checkedAll.value = false;
  };

  return {
    attachments,
    loading,
    selectedAttachment,
    selectedAttachments,
    checkedAll,
    handleFetchAttachments,
    handlePaginationChange,
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
        }
      })
      .filter(Boolean) as Content[];
    console.log(contents);
    editor.value
      ?.chain()
      .focus()
      .insertContent([...contents, { type: "paragraph", content: "" }])
      .run();
  };

  return {
    onAttachmentSelect,
  };
}
