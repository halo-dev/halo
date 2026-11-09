<script lang="ts" setup>
import { usePostCategory } from "@console/modules/contents/posts/categories/composables/use-post-category";
import type { CategoryTreeNode } from "@console/modules/contents/posts/categories/utils";
import {
  VFilterDropdown,
  type FilterDropdownOption,
  type FilterDropdownValue,
} from "@halo-dev/components";
import { computed } from "vue";
import { useI18n } from "vue-i18n";

withDefaults(
  defineProps<{
    label: string;
    modelValue?: string;
  }>(),
  {
    modelValue: undefined,
  }
);

const emit = defineEmits<{
  (event: "update:modelValue", value?: string): void;
}>();

const { t } = useI18n();
const { categories, categoriesTree } = usePostCategory();

const categoryMap = computed(() => {
  return new Map(
    categories.value?.map((category) => [category.metadata.name, category])
  );
});

function toOption(node: CategoryTreeNode): FilterDropdownOption {
  return {
    label: node.category.spec.displayName,
    value: node.category.metadata.name,
    description: node.category.status?.permalink,
    keywords: [node.category.metadata.name],
    children: node.children.length ? node.children.map(toOption) : undefined,
  };
}

const options = computed<FilterDropdownOption[]>(() => {
  return categoriesTree.value.map(toOption);
});

function getCategory(option: FilterDropdownOption) {
  if (option.value === undefined) {
    return undefined;
  }

  return categoryMap.value.get(String(option.value));
}

function getPostCount(option: FilterDropdownOption) {
  return t("core.common.fields.post_count", {
    count: getCategory(option)?.status?.postCount || 0,
  });
}

function handleUpdate(value?: FilterDropdownValue) {
  emit("update:modelValue", typeof value === "string" ? value : undefined);
}
</script>

<template>
  <VFilterDropdown
    :model-value="modelValue"
    :label="label"
    :options="options"
    searchable
    :search-placeholder="$t('core.common.placeholder.search')"
    :empty-text="$t('core.components.global_search.no_results')"
    panel-width="22rem"
    @update:model-value="handleUpdate"
  >
    <template #option="{ option, selected }">
      <div class="flex min-w-0 flex-1 flex-col">
        <span
          class="truncate text-sm"
          :class="selected ? 'font-medium text-gray-900' : 'text-gray-700'"
        >
          {{ option.label }}
        </span>
        <span v-if="option.description" class="truncate text-xs text-gray-500">
          {{ option.description }}
        </span>
      </div>
      <span class="shrink-0 text-xs text-gray-500">
        {{ getPostCount(option) }}
      </span>
    </template>
  </VFilterDropdown>
</template>
