import { apiClient } from "@/utils/api-client";
import type { Tag } from "@halo-dev/api-client";
import type { Ref } from "vue";
import { Dialog, Toast } from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import { useI18n } from "vue-i18n";

interface usePostTagReturn {
  tags: Ref<Tag[] | undefined>;
  isLoading: Ref<boolean>;
  handleFetchTags: () => void;
  handleDelete: (tag: Tag) => void;
}

export function usePostTag(): usePostTagReturn {
  const { t } = useI18n();

  const {
    data: tags,
    isLoading,
    refetch,
  } = useQuery({
    queryKey: ["post-tags"],
    queryFn: async () => {
      const { data } =
        await apiClient.extension.tag.listcontentHaloRunV1alpha1Tag({
          page: 0,
          size: 0,
        });

      return data.items;
    },
    refetchInterval(data) {
      const abnormalTags = data?.filter(
        (tag) => !!tag.metadata.deletionTimestamp || !tag.status?.permalink
      );
      return abnormalTags?.length ? 3000 : false;
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
          await apiClient.extension.tag.deletecontentHaloRunV1alpha1Tag({
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

  return {
    tags,
    isLoading,
    handleFetchTags: refetch,
    handleDelete,
  };
}
