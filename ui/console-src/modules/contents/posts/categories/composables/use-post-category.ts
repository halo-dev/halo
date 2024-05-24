import { apiClient } from "@/utils/api-client";
import type { Category } from "@halo-dev/api-client";
import type { Ref } from "vue";
import { ref } from "vue";
import type { CategoryTree } from "../utils";
import { buildCategoriesTree } from "../utils";
import { useQuery } from "@tanstack/vue-query";

interface usePostCategoryReturn {
  categories: Ref<Category[] | undefined>;
  categoriesTree: Ref<CategoryTree[]>;
  isLoading: Ref<boolean>;
  handleFetchCategories: () => void;
}

export function usePostCategory(): usePostCategoryReturn {
  const categoriesTree = ref<CategoryTree[]>([] as CategoryTree[]);

  const {
    data: categories,
    isLoading,
    refetch,
  } = useQuery({
    queryKey: ["post-categories"],
    queryFn: async () => {
      const { data } =
        await apiClient.extension.category.listContentHaloRunV1alpha1Category({
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
