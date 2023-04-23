import { apiClient } from "@/utils/api-client";
import type { Category } from "@halo-dev/api-client";
import type { Ref } from "vue";
import { ref } from "vue";
import type { CategoryTree } from "@/modules/contents/posts/categories/utils";
import { buildCategoriesTree } from "@/modules/contents/posts/categories/utils";
import { Dialog, Toast } from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import { useI18n } from "vue-i18n";

interface usePostCategoryReturn {
  categories: Ref<Category[] | undefined>;
  categoriesTree: Ref<CategoryTree[]>;
  isLoading: Ref<boolean>;
  handleFetchCategories: () => void;
  handleDelete: (category: CategoryTree) => void;
}

export function usePostCategory(): usePostCategoryReturn {
  const { t } = useI18n();

  const categoriesTree = ref<CategoryTree[]>([] as CategoryTree[]);

  const {
    data: categories,
    isLoading,
    refetch,
  } = useQuery({
    queryKey: ["post-categories"],
    queryFn: async () => {
      const { data } =
        await apiClient.extension.category.listcontentHaloRunV1alpha1Category({
          page: 0,
          size: 0,
        });

      return data.items;
    },
    refetchInterval(data) {
      const abnormalCategories = data?.filter(
        (category) =>
          !!category.metadata.deletionTimestamp || !category.status?.permalink
      );
      return abnormalCategories?.length ? 3000 : false;
    },
    onSuccess(data) {
      categoriesTree.value = buildCategoriesTree(data);
    },
  });

  const handleDelete = async (category: CategoryTree) => {
    Dialog.warning({
      title: t("core.post_category.operations.delete.title"),
      description: t("core.post_category.operations.delete.description"),
      confirmType: "danger",
      confirmText: t("core.common.buttons.confirm"),
      cancelText: t("core.common.buttons.cancel"),
      onConfirm: async () => {
        try {
          await apiClient.extension.category.deletecontentHaloRunV1alpha1Category(
            {
              name: category.metadata.name,
            }
          );

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
    categories,
    categoriesTree,
    isLoading,
    handleFetchCategories: refetch,
    handleDelete,
  };
}
