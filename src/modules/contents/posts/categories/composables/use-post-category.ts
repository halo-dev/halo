import { apiClient } from "@/utils/api-client";
import type { Category } from "@halo-dev/api-client";
import { onUnmounted, type Ref } from "vue";
import { onMounted, ref } from "vue";
import type { CategoryTree } from "@/modules/contents/posts/categories/utils";
import { buildCategoriesTree } from "@/modules/contents/posts/categories/utils";
import { Dialog } from "@halo-dev/components";
import { onBeforeRouteLeave } from "vue-router";

interface usePostCategoryReturn {
  categories: Ref<Category[]>;
  categoriesTree: Ref<CategoryTree[]>;
  loading: Ref<boolean>;
  handleFetchCategories: (fetchOptions?: { mute?: boolean }) => void;
  handleDelete: (category: CategoryTree) => void;
}

export function usePostCategory(options?: {
  fetchOnMounted: boolean;
}): usePostCategoryReturn {
  const { fetchOnMounted } = options || {};

  const categories = ref<Category[]>([] as Category[]);
  const categoriesTree = ref<CategoryTree[]>([] as CategoryTree[]);
  const loading = ref(false);
  const refreshInterval = ref();

  const handleFetchCategories = async (fetchOptions?: { mute?: boolean }) => {
    try {
      clearInterval(refreshInterval.value);

      if (!fetchOptions?.mute) {
        loading.value = true;
      }
      const { data } =
        await apiClient.extension.category.listcontentHaloRunV1alpha1Category({
          page: 0,
          size: 0,
        });
      categories.value = data.items;
      categoriesTree.value = buildCategoriesTree(data.items);

      const deletedCategories = categories.value.filter(
        (category) => !!category.metadata.deletionTimestamp
      );

      if (deletedCategories.length) {
        refreshInterval.value = setInterval(() => {
          handleFetchCategories({ mute: true });
        }, 3000);
      }
    } catch (e) {
      console.error("Failed to fetch categories", e);
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

  const handleDelete = async (category: CategoryTree) => {
    Dialog.warning({
      title: "确定要删除该分类吗？",
      description: "删除此分类之后，对应文章的关联将被解除。该操作不可恢复。",
      confirmType: "danger",
      onConfirm: async () => {
        try {
          await apiClient.extension.category.deletecontentHaloRunV1alpha1Category(
            {
              name: category.metadata.name,
            }
          );
        } catch (e) {
          console.error("Failed to delete tag", e);
        } finally {
          await handleFetchCategories();
        }
      },
    });
  };

  onMounted(() => {
    fetchOnMounted && handleFetchCategories();
  });

  return {
    categories,
    categoriesTree,
    loading,
    handleFetchCategories,
    handleDelete,
  };
}
