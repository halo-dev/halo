import {
  consoleApiClient,
  type Category,
  type CategoryTreeNode,
} from "@halo-dev/api-client";
import { useQuery } from "@tanstack/vue-query";
import { cloneDeep } from "es-toolkit";
import { ref } from "vue";
import { flattenCategoryTreeNodes } from "../utils";

export function usePostCategory() {
  const categoriesTree = ref<CategoryTreeNode[]>([]);
  const previousCategoriesTree = ref<CategoryTreeNode[]>([]);
  const categories = ref<Category[]>([]);

  function setCategoriesTree(tree: CategoryTreeNode[]) {
    categoriesTree.value = tree;
    previousCategoriesTree.value = cloneDeep(tree);
    categories.value = flattenCategoryTreeNodes(tree);
  }

  const { isLoading, refetch } = useQuery({
    queryKey: ["post-categories"],
    queryFn: async () => {
      const { data } =
        await consoleApiClient.content.category.listCategoryTree();
      return data;
    },
    refetchInterval(data) {
      const hasAbnormalCategory = flattenCategoryTreeNodes(data || []).some(
        (category) =>
          !!category.metadata.deletionTimestamp || !category.status?.permalink
      );
      return hasAbnormalCategory ? 1000 : false;
    },
    onSuccess(data) {
      setCategoriesTree(data);
    },
  });

  return {
    categories,
    categoriesTree,
    previousCategoriesTree,
    isLoading,
    handleFetchCategories: refetch,
    setCategoriesTree,
  };
}
