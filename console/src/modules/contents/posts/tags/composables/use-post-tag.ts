import { apiClient } from "@/utils/api-client";
import type { Tag } from "@halo-dev/api-client";
import type { Ref } from "vue";
import { Dialog, Toast } from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";

interface usePostTagReturn {
  tags: Ref<Tag[] | undefined>;
  isLoading: Ref<boolean>;
  handleFetchTags: () => void;
  handleDelete: (tag: Tag) => void;
}

export function usePostTag(): usePostTagReturn {
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
    refetchOnWindowFocus: false,
  });

  const handleDelete = async (tag: Tag) => {
    Dialog.warning({
      title: "确定要删除该标签吗？",
      description: "删除此标签之后，对应文章的关联将被解除。该操作不可恢复。",
      confirmType: "danger",
      onConfirm: async () => {
        try {
          await apiClient.extension.tag.deletecontentHaloRunV1alpha1Tag({
            name: tag.metadata.name,
          });

          Toast.success("删除成功");
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
