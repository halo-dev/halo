import { apiClient } from "@halo-dev/admin-shared";
import type { Category } from "@halo-dev/api-client";
import type { Ref } from "vue";
import { onMounted, ref } from "vue";
import type { CategoryTree } from "@/modules/contents/posts/categories/utils";
import { buildCategoriesTree } from "@/modules/contents/posts/categories/utils";
import { useDialog } from "@halo-dev/components";

interface usePostCategoryReturn {
  categories: Ref<Category[]>;
  categoriesTree: Ref<CategoryTree[]>;
  loading: Ref<boolean>;
  handleFetchCategories: () => void;
  handleDelete: (category: CategoryTree) => void;
}

export function usePostCategory(): usePostCategoryReturn {
  const categories = ref<Category[]>([] as Category[]);
  const categoriesTree = ref<CategoryTree[]>([] as CategoryTree[]);
  const loading = ref(false);

  const dialog = useDialog();

  const handleFetchCategories = async () => {
    try {
      loading.value = true;
      const { data } =
        await apiClient.extension.category.listcontentHaloRunV1alpha1Category(
          0,
          0
        );
      categories.value = data.items;
      categoriesTree.value = buildCategoriesTree(data.items);
    } catch (e) {
      console.error("Failed to fetch categories", e);
    } finally {
      loading.value = false;
    }
  };

  const handleDelete = async (category: CategoryTree) => {
    dialog.warning({
      title: "确定要删除该分类吗？",
      description: "删除此分类之后，对应文章的关联将被解除。该操作不可恢复。",
      confirmType: "danger",
      onConfirm: async () => {
        try {
          await apiClient.extension.category.deletecontentHaloRunV1alpha1Category(
            category.metadata.name
          );
        } catch (e) {
          console.error("Failed to delete tag", e);
        } finally {
          await handleFetchCategories();
        }
      },
    });
  };

  onMounted(handleFetchCategories);

  return {
    categories,
    categoriesTree,
    loading,
    handleFetchCategories,
    handleDelete,
  };
}
