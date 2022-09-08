import type {
  Attachment,
  AttachmentList,
  Group,
  Policy,
  User,
} from "@halo-dev/api-client";
import type { Ref } from "vue";
import { ref, watch } from "vue";
import { apiClient, type AttachmentLike } from "@halo-dev/admin-shared";
import { useDialog } from "@halo-dev/components";
import type { Content, Editor } from "@halo-dev/richtext-editor";

interface useAttachmentControlReturn {
  attachments: Ref<AttachmentList>;
  loading: Ref<boolean>;
  selectedAttachment: Ref<Attachment | undefined>;
  selectedAttachments: Ref<Set<Attachment>>;
  checkedAll: Ref<boolean>;
  handleFetchAttachments: () => void;
  handlePaginationChange: ({
    page,
    size,
  }: {
    page: number;
    size: number;
  }) => void;
  handleSelectPrevious: () => void;
  handleSelectNext: () => void;
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
}): useAttachmentControlReturn {
  const { user, policy, group, keyword } = filterOptions || {};

  const attachments = ref<AttachmentList>({
    page: 1,
    size: 60,
    total: 0,
    items: [],
    first: true,
    last: false,
    hasNext: false,
    hasPrevious: false,
  });
  const loading = ref<boolean>(false);

  const selectedAttachment = ref<Attachment>();
  const selectedAttachments = ref<Set<Attachment>>(new Set<Attachment>());
  const checkedAll = ref(false);

  const dialog = useDialog();

  const handleFetchAttachments = async () => {
    try {
      loading.value = true;
      const { data } =
        await apiClient.extension.storage.attachment.searchAttachments({
          policy: policy?.value?.metadata.name,
          displayName: keyword?.value,
          group: group?.value?.metadata.name,
          uploadedBy: user?.value?.metadata.name,
          page: attachments.value.page,
          size: attachments.value.size,
        });
      attachments.value = data;
    } catch (e) {
      console.error("Failed to fetch attachments", e);
    } finally {
      loading.value = false;
    }
  };

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

  const handleDeleteInBatch = () => {
    dialog.warning({
      title: "确定要删除所选的附件吗？",
      description: "其中 20 个附件包含关联关系，删除之后将无法恢复",
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
    const images: Content[] = attachments.map((attachment) => {
      const attrs: { src?: string; alt?: string } = {};
      if (typeof attachment === "string") {
        attrs.src = attachment;
        return {
          type: "image",
          attrs,
        };
      }
      if ("url" in attachment) {
        attrs.src = attachment.url;
        attrs.alt = attachment.type;
      }
      if ("spec" in attachment) {
        attrs.src = attachment.status?.permalink;
        attrs.alt = attachment.spec.displayName;
      }
      return {
        type: "image",
        attrs,
      };
    });
    editor.value
      ?.chain()
      .focus()
      .insertContent([...images, { type: "paragraph", content: "" }])
      .run();
  };

  return {
    onAttachmentSelect,
  };
}
