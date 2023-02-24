<script lang="ts" setup>
import {
  getCategoryPath,
  type CategoryTree,
} from "@/modules/contents/posts/categories/utils";
import type { Category } from "@halo-dev/api-client";
import { IconCheckboxCircle } from "@halo-dev/components";
import { computed, inject, ref, type Ref } from "vue";

const props = withDefaults(
  defineProps<{
    category: Category;
  }>(),
  {}
);

const categoriesTree = inject<Ref<CategoryTree[]>>("categoriesTree", ref([]));
const selectedCategory = inject<Ref<Category | CategoryTree | undefined>>(
  "selectedCategory",
  ref(undefined)
);

const isSelected =
  inject<(category: Category | CategoryTree) => boolean>("isSelected");

const emit = defineEmits<{
  (event: "select", category: CategoryTree | Category): void;
}>();

const label = computed(() => {
  const categories = getCategoryPath(
    categoriesTree.value,
    props.category.metadata.name
  );
  return categories
    ?.map((category: CategoryTree) => category.spec.displayName)
    .join(" / ");
});
</script>

<template>
  <li :id="`category-${category.metadata.name}`">
    <div
      class="flex cursor-pointer items-center justify-between rounded p-2 hover:bg-gray-100"
      :class="{
        'bg-gray-100':
          selectedCategory?.metadata.name === category.metadata.name,
      }"
      @click="emit('select', category)"
    >
      <span
        class="flex-1 truncate text-xs text-gray-700 group-hover:text-gray-900"
        :class="{
          'text-gray-900':
            isSelected?.(category) &&
            selectedCategory?.metadata.name === category.metadata.name,
        }"
      >
        {{ label }}
      </span>
      <IconCheckboxCircle
        class="h-5 w-5 text-primary opacity-0"
        :class="{ 'opacity-100': isSelected?.(category) }"
      />
    </div>
  </li>
</template>
