import { apiClient } from "@/utils/api-client";
import type { Tag } from "@halo-dev/api-client";
import { onUnmounted, type Ref } from "vue";
import { onMounted, ref } from "vue";
import { Dialog } from "@halo-dev/components";
import { onBeforeRouteLeave } from "vue-router";

interface usePostTagReturn {
  tags: Ref<Tag[]>;
  loading: Ref<boolean>;
  handleFetchTags: () => void;
  handleDelete: (tag: Tag) => void;
}

export function usePostTag(options?: {
  fetchOnMounted: boolean;
}): usePostTagReturn {
  const { fetchOnMounted } = options || {};

  const tags = ref<Tag[]>([] as Tag[]);
  const loading = ref(false);
  const refreshInterval = ref();

  const handleFetchTags = async () => {
    try {
      clearInterval(refreshInterval.value);

      loading.value = true;
      const { data } =
        await apiClient.extension.tag.listcontentHaloRunV1alpha1Tag({
          page: 0,
          size: 0,
        });

      tags.value = data.items;

      const deletedTags = tags.value.filter(
        (tag) => !!tag.metadata.deletionTimestamp
      );

      if (deletedTags.length) {
        refreshInterval.value = setInterval(() => {
          handleFetchTags();
        }, 3000);
      }
    } catch (e) {
      console.error("Failed to fetch tags", e);
    } finally {
      loading.value = false;
    }
  };

  onUnmounted(() => {
    clearInterval(refreshInterval.value);
  });

  onBeforeRouteLeave(() => {
    clearInterval(refreshInterval.value);
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
        } catch (e) {
          console.error("Failed to delete tag", e);
        } finally {
          await handleFetchTags();
        }
      },
    });
  };

  onMounted(() => {
    fetchOnMounted && handleFetchTags();
  });

  return {
    tags,
    loading,
    handleFetchTags,
    handleDelete,
  };
}
