import { coreApiClient } from "@halo-dev/api-client";
import { useQuery } from "@tanstack/vue-query";
import { ref } from "vue";
import { buildCategoriesTree, type CategoryTreeNode } from "../utils";

export function usePostCategory() {
  const categoriesTree = ref<CategoryTreeNode[]>([] as CategoryTreeNode[]);

  const {
    data: categories,
    isLoading,
    refetch,
  } = useQuery({
    queryKey: ["post-categories"],
    queryFn: async () => {
      const { data } = await coreApiClient.content.category.listCategory({
        page: 0,
        size: 0,
        sort: ["metadata.creationTimestamp,desc"],
      });

      return data.items;
    },
    refetchInterval(data) {
      const hasAbnormalCategory = data?.some(
        (category) =>
          !!category.metadata.deletionTimestamp || !category.status?.permalink
      );
      return hasAbnormalCategory ? 1000 : false;
    },
    onSuccess(data) {
      categoriesTree.value = buildCategoriesTree(data);
    },
  });

  return {
    categories,
    categoriesTree,
    isLoading,
    handleFetchCategories: refetch,
  };
}
