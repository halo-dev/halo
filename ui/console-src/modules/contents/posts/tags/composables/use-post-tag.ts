import { apiClient } from "@/utils/api-client";
import type { Tag } from "@halo-dev/api-client";
import { ref, watch, type Ref } from "vue";
import { Dialog, Toast } from "@halo-dev/components";
import { useQuery, type QueryObserverResult } from "@tanstack/vue-query";
import { useI18n } from "vue-i18n";

interface usePostTagReturn {
  tags: Ref<Tag[] | undefined>;
  total: Ref<number>;
  hasPrevious: Ref<boolean>;
  hasNext: Ref<boolean>;
  isLoading: Ref<boolean>;
  isFetching: Ref<boolean>;
  handleFetchTags: () => Promise<QueryObserverResult<Tag[], unknown>>;
  handleDelete: (tag: Tag) => void;
  handleDeleteInBatch: (tagNames: string[]) => Promise<void>;
}

export function usePostTag(filterOptions?: {
  sort?: Ref<string | undefined>;
  page?: Ref<number>;
  size?: Ref<number>;
  keyword?: Ref<string>;
}): usePostTagReturn {
  const { t } = useI18n();

  const { sort, page, size, keyword } = filterOptions || {};

  const total = ref(0);
  const hasPrevious = ref(false);
  const hasNext = ref(false);

  const {
    data: tags,
    isLoading,
    isFetching,
    refetch,
  } = useQuery({
    queryKey: ["post-tags", sort, page, size, keyword],
    queryFn: async () => {
      const { data } = await apiClient.tag.listPostTags({
        page: page?.value || 0,
        size: size?.value || 0,
        sort: [sort?.value as string].filter(Boolean) || [
          "metadata.creationTimestamp,desc",
        ],
        keyword: keyword?.value,
      });

      total.value = data.total;
      hasPrevious.value = data.hasPrevious;
      hasNext.value = data.hasNext;

      return data.items;
    },
    refetchInterval(data) {
      const abnormalTags = data?.filter(
        (tag) => !!tag.metadata.deletionTimestamp || !tag.status?.permalink
      );
      return abnormalTags?.length ? 1000 : false;
    },
  });

  const handleDelete = async (tag: Tag) => {
    Dialog.warning({
      title: t("core.post_tag.operations.delete.title"),
      description: t("core.post_tag.operations.delete.description"),
      confirmType: "danger",
      confirmText: t("core.common.buttons.confirm"),
      cancelText: t("core.common.buttons.cancel"),
      onConfirm: async () => {
        try {
          await apiClient.extension.tag.deleteContentHaloRunV1alpha1Tag({
            name: tag.metadata.name,
          });

          Toast.success(t("core.common.toast.delete_success"));
        } catch (e) {
          console.error("Failed to delete tag", e);
        } finally {
          await refetch();
        }
      },
    });
  };

  const handleDeleteInBatch = (tagNames: string[]) => {
    return new Promise<void>((resolve) => {
      Dialog.warning({
        title: t("core.post_tag.operations.delete_in_batch.title"),
        description: t("core.common.dialog.descriptions.cannot_be_recovered"),
        confirmType: "danger",
        confirmText: t("core.common.buttons.confirm"),
        cancelText: t("core.common.buttons.cancel"),
        onConfirm: async () => {
          try {
            await Promise.all(
              tagNames.map((tagName) => {
                apiClient.extension.tag.deleteContentHaloRunV1alpha1Tag({
                  name: tagName,
                });
              })
            );

            Toast.success(t("core.common.toast.delete_success"));
            resolve();
          } catch (e) {
            console.error("Failed to delete tags in batch", e);
          } finally {
            await refetch();
          }
        },
      });
    });
  };

  watch(
    () => [sort?.value, keyword?.value],
    () => {
      if (page?.value) {
        page.value = 1;
      }
    }
  );

  return {
    tags,
    total,
    hasPrevious,
    hasNext,
    isLoading,
    isFetching,
    handleFetchTags: refetch,
    handleDelete,
    handleDeleteInBatch,
  };
}
