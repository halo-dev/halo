import { coreApiClient, type Category } from "@halo-dev/api-client";
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
      const result: Category[] = [];
      let page = 1;
      let hasNext = true;

      while (hasNext) {
        const { data } = await coreApiClient.content.category.listCategory({
          page: page,
          size: 1000,
          sort: ["metadata.creationTimestamp,desc"],
        });
        result.push(...data.items);
        page++;
        hasNext = data.hasNext;
      }

      return result;
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
