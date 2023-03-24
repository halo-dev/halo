<script lang="ts" setup>
import type { FormKitFrameworkContext } from "@formkit/core";
import type { Category } from "@halo-dev/api-client";
import { computed, provide, ref, watch, type PropType, type Ref } from "vue";
import { IconArrowRight } from "@halo-dev/components";
import { usePostCategory } from "@/modules/contents/posts/categories/composables/use-post-category";
import type { CategoryTree } from "@/modules/contents/posts/categories/utils";
import { convertTreeToCategories } from "@/modules/contents/posts/categories/utils";
import CategoryListItem from "./components/CategoryListItem.vue";
import { onClickOutside } from "@vueuse/core";
import Fuse from "fuse.js";
import CategoryTag from "./components/CategoryTag.vue";
import SearchResultListItem from "./components/SearchResultListItem.vue";
import { apiClient } from "@/utils/api-client";
import { usePermission } from "@/utils/permission";
import { slugify } from "transliteration";

const { currentUserHasPermission } = usePermission();

const props = defineProps({
  context: {
    type: Object as PropType<FormKitFrameworkContext>,
    required: true,
  },
});

const multiple = computed(() => {
  const { multiple } = props.context;
  if (multiple === undefined) {
    return false;
  }
  if (typeof multiple === "boolean") {
    return multiple;
  }
  return multiple === "true";
});

const { categories, categoriesTree, handleFetchCategories } = usePostCategory();

provide<Ref<CategoryTree[]>>("categoriesTree", categoriesTree);

const selectedCategory = ref<Category | CategoryTree>();

provide<Ref<Category | CategoryTree | undefined>>(
  "selectedCategory",
  selectedCategory
);

const dropdownVisible = ref(false);
const text = ref("");
const wrapperRef = ref<HTMLElement>();

onClickOutside(wrapperRef, () => {
  dropdownVisible.value = false;
});

// search
let fuse: Fuse<Category> | undefined = undefined;

const searchResults = computed(() => {
  if (!text.value) {
    return categories.value;
  }
  return fuse?.search(text.value).map((item) => item.item) || [];
});

watch(
  () => searchResults.value,
  (value) => {
    if (value?.length && text.value) {
      selectedCategory.value = value[0];
      scrollToSelected();
    } else {
      selectedCategory.value = undefined;
    }
  }
);

watch(
  () => categories.value,
  () => {
    fuse = new Fuse(categories.value || [], {
      keys: ["spec.displayName", "spec.slug"],
      useExtendedSearch: true,
      threshold: 0.2,
    });
  },
  {
    immediate: true,
  }
);

const selectedCategories = computed(() => {
  if (multiple.value) {
    const currentValue = props.context._value || [];
    return currentValue
      .map((categoryName): Category | undefined => {
        return categories.value?.find(
          (category) => category.metadata.name === categoryName
        );
      })
      .filter(Boolean) as Category[];
  }

  const category = categories.value?.find(
    (category) => category.metadata.name === props.context._value
  );
  return [category].filter(Boolean) as Category[];
});

const isSelected = (category: CategoryTree | Category) => {
  if (multiple.value) {
    return (props.context._value || []).includes(category.metadata.name);
  }
  return props.context._value === category.metadata.name;
};

provide<(category: CategoryTree | Category) => boolean>(
  "isSelected",
  isSelected
);

const handleSelect = (category: CategoryTree | Category) => {
  if (multiple.value) {
    const currentValue = props.context._value || [];
    if (currentValue.includes(category.metadata.name)) {
      props.context.node.input(
        currentValue.filter((name: string) => name !== category.metadata.name)
      );
    } else {
      props.context.node.input([...currentValue, category.metadata.name]);
    }
    return;
  }

  props.context.node.input(
    category.metadata.name === props.context._value
      ? ""
      : category.metadata.name
  );
};

const handleKeydown = (e: KeyboardEvent) => {
  if (!searchResults.value) return;

  if (e.key === "ArrowDown") {
    e.preventDefault();

    const categoryIndices = text.value
      ? searchResults.value
      : convertTreeToCategories(categoriesTree.value);

    const index = categoryIndices.findIndex(
      (category) =>
        category.metadata.name === selectedCategory.value?.metadata.name
    );

    if (index < searchResults.value.length - 1) {
      selectedCategory.value = categoryIndices[index + 1];
    }
    scrollToSelected();
  }

  if (e.key === "ArrowUp") {
    e.preventDefault();

    const categoryIndices = text.value
      ? searchResults.value
      : convertTreeToCategories(categoriesTree.value);

    const index = categoryIndices.findIndex(
      (tag) => tag.metadata.name === selectedCategory.value?.metadata.name
    );
    if (index > 0) {
      selectedCategory.value = categoryIndices[index - 1];
    }
    scrollToSelected();
  }

  if (e.key === "Enter") {
    if (searchResults.value.length === 0 && text.value) {
      handleCreateCategory();
      return;
    }
    if (selectedCategory.value) {
      handleSelect(selectedCategory.value);
      text.value = "";
      e.preventDefault();
    }
  }
};

const scrollToSelected = () => {
  if (!selectedCategory.value) {
    return;
  }
  const selectedNode = document.getElementById(
    `category-${selectedCategory.value?.metadata.name}`
  );
  if (selectedNode) {
    selectedNode.scrollIntoView({
      behavior: "smooth",
      block: "nearest",
      inline: "start",
    });
  }
};

const handleCreateCategory = async () => {
  if (!currentUserHasPermission(["system:posts:manage"])) {
    return;
  }

  const { data } =
    await apiClient.extension.category.createcontentHaloRunV1alpha1Category({
      category: {
        spec: {
          displayName: text.value,
          slug: slugify(text.value, { trim: true }),
          description: "",
          cover: "",
          template: "",
          priority: categories.value?.length || 0 + 1,
          children: [],
        },
        apiVersion: "content.halo.run/v1alpha1",
        kind: "Category",
        metadata: {
          name: "",
          generateName: "category-",
        },
      },
    });

  handleFetchCategories();
  handleSelect(data);
  text.value = "";
};

// update value immediately during IME composition
// please see https://vuejs.org//guide/essentials/forms.html#text
const onTextInput = (e: Event) => {
  text.value = (e.target as HTMLInputElement).value;
};

// delete last category when text input is empty
const handleDelete = () => {
  if (!text.value) {
    if (multiple.value) {
      const selectedTagNames = (props.context._value as string[]) || [];
      props.context.node.input(selectedTagNames.slice(0, -1));
      return;
    }
    props.context.node.input("");
  }
};
</script>

<template>
  <div
    ref="wrapperRef"
    :class="context.classes['post-categories-wrapper']"
    @keydown="handleKeydown"
  >
    <div :class="context.classes['post-categories']">
      <CategoryTag
        v-for="(category, index) in selectedCategories"
        :key="index"
        :category="category"
        @select="handleSelect"
      />
      <input
        :value="text"
        :class="context.classes.input"
        type="text"
        @input="onTextInput"
        @focus="dropdownVisible = true"
        @keydown.delete="handleDelete"
      />
    </div>

    <div
      :class="context.classes['post-categories-button']"
      @click="dropdownVisible = !dropdownVisible"
    >
      <IconArrowRight class="rotate-90 text-gray-500 hover:text-gray-700" />
    </div>

    <div v-if="dropdownVisible" :class="context.classes['dropdown-wrapper']">
      <ul class="p-1">
        <li
          v-if="text.trim() && !searchResults?.length"
          v-permission="['system:posts:manage']"
          class="group flex cursor-pointer items-center justify-between rounded bg-gray-100 p-2"
        >
          <span class="text-xs text-gray-700 group-hover:text-gray-900">
            {{
              $t("core.formkit.category_select.creation_label", { text: text })
            }}
          </span>
        </li>
        <template v-if="text">
          <SearchResultListItem
            v-for="category in searchResults"
            :key="category.metadata.name"
            :category="category"
            @select="handleSelect"
          />
        </template>
        <template v-else>
          <CategoryListItem
            v-for="category in categoriesTree"
            :key="category.metadata.name"
            :category="category"
            @select="handleSelect"
          />
        </template>
      </ul>
    </div>
  </div>
</template>
