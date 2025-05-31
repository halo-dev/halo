import type { Attachment } from "@halo-dev/api-client";
import { consoleApiClient, coreApiClient } from "@halo-dev/api-client";
import { Dialog, Toast } from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import {
  computed,
  nextTick,
  ref,
  watch,
  type ComputedRef,
  type Ref,
} from "vue";
import { useI18n } from "vue-i18n";

interface useAttachmentControlReturn {
  attachments: Ref<Attachment[] | undefined>;
  isLoading: Ref<boolean>;
  isFetching: Ref<boolean>;
  selectedAttachment: Ref<Attachment | undefined>;
  selectedAttachments: ComputedRef<Attachment[]>;
  selectedAttachmentNames: Ref<Set<string>>;
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
  const selectedAttachmentNames = ref<Set<string>>(new Set<string>());
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

      const { data } =
        await consoleApiClient.storage.attachment.searchAttachments({
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
          const promises = Array.from(selectedAttachmentNames.value).map(
            (name) => {
              return coreApiClient.storage.attachment.deleteAttachment({
                name,
              });
            }
          );
          await Promise.all(promises);
          selectedAttachmentNames.value.clear();

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
        selectedAttachmentNames.value.add(attachment.metadata.name);
      });
    } else {
      selectedAttachmentNames.value.clear();
    }
  };

  const handleSelect = async (attachment: Attachment | undefined) => {
    if (!attachment) return;
    if (selectedAttachmentNames.value.has(attachment.metadata.name)) {
      selectedAttachmentNames.value.delete(attachment.metadata.name);
      return;
    }
    selectedAttachmentNames.value.add(attachment.metadata.name);
  };

  watch(
    () => selectedAttachmentNames.value.size,
    (newValue) => {
      checkedAll.value = newValue === data.value?.length;
    }
  );

  const isChecked = (attachment: Attachment) => {
    return selectedAttachmentNames.value.has(attachment.metadata.name);
  };

  const handleReset = () => {
    page.value = 1;
    selectedAttachment.value = undefined;
    selectedAttachmentNames.value.clear();
    checkedAll.value = false;
  };

  const selectedAttachments = computed(() => {
    return (
      data.value?.filter((attachment) =>
        selectedAttachmentNames.value.has(attachment.metadata.name)
      ) || []
    );
  });

  return {
    attachments: data,
    isLoading,
    isFetching,
    selectedAttachment,
    selectedAttachments,
    selectedAttachmentNames,
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
