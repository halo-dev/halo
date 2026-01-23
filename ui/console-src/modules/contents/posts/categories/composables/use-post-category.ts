import {
  coreApiClient,
  paginate,
  type Category,
  type CategoryV1alpha1ApiListCategoryRequest,
} from "@halo-dev/api-client";
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
      return await paginate<CategoryV1alpha1ApiListCategoryRequest, Category>(
        (params) => coreApiClient.content.category.listCategory(params),
        {
          size: 1000,
          sort: ["metadata.creationTimestamp,desc"],
        }
      );
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
