<script lang="ts" setup>
import type { CategoryTree } from "@/modules/contents/posts/categories/utils";
import type { Category } from "@halo-dev/api-client";
import { IconCheckboxCircle } from "@halo-dev/components";
import { inject, ref, type Ref } from "vue";

withDefaults(
  defineProps<{
    category: CategoryTree;
  }>(),
  {}
);

const isSelected = inject<(category: CategoryTree) => boolean>("isSelected");
const selectedCategory = inject<Ref<Category | CategoryTree | undefined>>(
  "selectedCategory",
  ref(undefined)
);

const emit = defineEmits<{
  (event: "select", category: CategoryTree): void;
}>();

const onSelect = (childCategory: CategoryTree) => {
  emit("select", childCategory);
};
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
        {{ category.spec.displayName }}
      </span>

      <IconCheckboxCircle
        class="text-primary opacity-0"
        :class="{ 'opacity-100': isSelected?.(category) }"
      />
    </div>

    <ul
      v-if="category.spec.children.length > 0"
      class="my-2.5 ml-2.5 border-l pl-1.5"
    >
      <CategoryListItem
        v-for="(childCategory, index) in category.spec.children"
        :key="index"
        :category="childCategory"
        @select="onSelect"
      />
    </ul>
  </li>
</template>
