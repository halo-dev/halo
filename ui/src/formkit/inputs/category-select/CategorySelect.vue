<script lang="ts" setup>
import { usePostCategory } from "@console/modules/contents/posts/categories/composables/use-post-category";
import {
  filterCategoryTreeNodes,
  flattenCategoryTreeNodes,
  getCategoryFromNode,
  type CategoryTreeNode,
} from "@console/modules/contents/posts/categories/utils";
import type { FormKitFrameworkContext } from "@formkit/core";
import type { Category } from "@halo-dev/api-client";
import { coreApiClient } from "@halo-dev/api-client";
import { IconArrowRight, VPopover } from "@halo-dev/components";
import { utils } from "@halo-dev/ui-shared";
import { useEventListener } from "@vueuse/core";
import Fuse from "fuse.js";
import ShortUniqueId from "short-unique-id";
import { slugify } from "transliteration";
import {
  computed,
  nextTick,
  provide,
  ref,
  useTemplateRef,
  watch,
  type PropType,
  type Ref,
} from "vue";
import CategoryListItem from "./components/CategoryListItem.vue";
import CategoryTag from "./components/CategoryTag.vue";
import SearchResultListItem from "./components/SearchResultListItem.vue";

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

const excludedNames = computed(() => {
  const names = props.context.excludedNames;
  if (Array.isArray(names)) {
    return names;
  }
  if (typeof names === "string") {
    return names.split(",").map((name) => name.trim());
  }
  return [];
});

const filteredCategoriesTree = computed(() => {
  return filterCategoryTreeNodes(categoriesTree.value, excludedNames.value);
});

const filteredCategories = computed(() => {
  return flattenCategoryTreeNodes(filteredCategoriesTree.value);
});

const excludedNameSet = computed(() => {
  return new Set(excludedNames.value.filter(Boolean));
});

const allowCreate = computed(() => {
  const { allowCreate } = props.context;
  if (allowCreate === undefined) {
    return true;
  }
  if (typeof allowCreate === "boolean") {
    return allowCreate;
  }
  return allowCreate === "true";
});

provide<Ref<CategoryTreeNode[]>>("categoriesTree", filteredCategoriesTree);

const selectedCategory = ref<Category | CategoryTreeNode>();

provide<Ref<Category | CategoryTreeNode | undefined>>(
  "selectedCategory",
  selectedCategory
);

const dropdownVisible = ref(false);
const text = ref("");
const inputRef = useTemplateRef<HTMLInputElement>("inputRef");

let blockEscapeKeyup = false;

// Escape closes the panel through the popover itself. The surrounding modal
// also listens for Escape on keyup, so swallow that keyup once the panel has
// handled the key.
useEventListener(
  window,
  "keydown",
  (event: KeyboardEvent) => {
    if (event.key === "Escape" && dropdownVisible.value) {
      blockEscapeKeyup = true;
    }
  },
  { capture: true }
);

useEventListener(
  window,
  "keyup",
  (event: KeyboardEvent) => {
    if (event.key !== "Escape" || !blockEscapeKeyup) {
      return;
    }
    blockEscapeKeyup = false;
    event.stopPropagation();
  },
  { capture: true }
);

// The non-modal popover moves focus to its content when it mounts, so return
// the focus to the search input after the panel has rendered.
watch(dropdownVisible, async (visible) => {
  if (!visible) {
    return;
  }
  await nextTick();
  await nextTick();
  inputRef.value?.focus();
});

// search
let fuse: Fuse<Category> | undefined = undefined;

const searchResults = computed(() => {
  if (!text.value) {
    return filteredCategories.value;
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
  () => filteredCategories.value,
  () => {
    fuse = new Fuse(filteredCategories.value || [], {
      keys: ["spec.displayName", "spec.slug"],
      useExtendedSearch: true,
      threshold: 0.2,
    });
    if (props.context) {
      // eslint-disable-next-line vue/no-mutating-props
      props.context.options =
        filteredCategories.value?.map((category) => {
          return {
            label: category.spec.displayName,
            value: category.metadata.name,
          };
        }) || [];
    }
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

const isSelected = (category: CategoryTreeNode | Category) => {
  const categoryName = getCategoryFromNode(category).metadata.name;
  if (multiple.value) {
    return (props.context._value || []).includes(categoryName);
  }
  return props.context._value === categoryName;
};

provide<(category: CategoryTreeNode | Category) => boolean>(
  "isSelected",
  isSelected
);

const handleSelect = (category: CategoryTreeNode | Category) => {
  const categoryName = getCategoryFromNode(category).metadata.name;
  if (excludedNameSet.value.has(categoryName)) {
    return;
  }
  if (multiple.value) {
    const currentValue = props.context._value || [];
    if (currentValue.includes(categoryName)) {
      props.context.node.input(
        currentValue.filter((name: string) => name !== categoryName)
      );
    } else {
      props.context.node.input([...currentValue, categoryName]);
      text.value = "";
    }
  } else {
    props.context.node.input(
      categoryName === props.context._value ? "" : categoryName
    );
  }
  // Selecting with the mouse moves the focus away from the input, so put it
  // back once the category has been selected.
  nextTick(() => inputRef.value?.focus());
};

const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === "Escape") {
    return;
  }

  if (!searchResults.value) return;

  if (!dropdownVisible.value) {
    if (e.key === "ArrowDown" || e.key === "ArrowUp") {
      e.preventDefault();
      dropdownVisible.value = true;
    }
    return;
  }

  if (e.key === "ArrowDown") {
    e.preventDefault();

    const categoryIndices = text.value
      ? searchResults.value
      : flattenCategoryTreeNodes(filteredCategoriesTree.value);

    const index = categoryIndices.findIndex(
      (category) =>
        category.metadata.name ===
        (selectedCategory.value
          ? getCategoryFromNode(selectedCategory.value).metadata.name
          : undefined)
    );

    if (index < categoryIndices.length - 1) {
      selectedCategory.value = categoryIndices[index + 1];
    }
    scrollToSelected();
  }

  if (e.key === "ArrowUp") {
    e.preventDefault();

    const categoryIndices = text.value
      ? searchResults.value
      : flattenCategoryTreeNodes(filteredCategoriesTree.value);

    const index = categoryIndices.findIndex(
      (category) =>
        category.metadata.name ===
        (selectedCategory.value
          ? getCategoryFromNode(selectedCategory.value).metadata.name
          : undefined)
    );
    if (index > 0) {
      selectedCategory.value = categoryIndices[index - 1];
    } else {
      selectedCategory.value = undefined;
    }
    scrollToSelected();
  }

  if (e.key === "Enter") {
    if (!selectedCategory.value && text.value && allowCreate.value) {
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
  // Wait for the options to render before looking them up.
  nextTick(() => {
    const selectedNodeName = selectedCategory.value
      ? getCategoryFromNode(selectedCategory.value).metadata.name
      : "create";
    const selectedNode = document.getElementById(
      `category-${selectedNodeName}`
    );
    if (selectedNode) {
      selectedNode.scrollIntoView({
        behavior: "smooth",
        block: "nearest",
        inline: "start",
      });
    }
  });
};

const uid = new ShortUniqueId();

const handleCreateCategory = async () => {
  if (!allowCreate.value || !utils.permission.has(["system:posts:manage"])) {
    return;
  }

  let slug = slugify(text.value, { trim: true });

  // Check if slug is unique, if not, add -1 to the slug
  const { data: categoriesWithSameSlug } =
    await coreApiClient.content.category.listCategory({
      fieldSelector: [`spec.slug=${slug}`],
      page: 1,
      size: 1,
    });

  if (categoriesWithSameSlug.total) {
    slug = `${slug}-${uid.randomUUID(8)}`;
  }

  const { data } = await coreApiClient.content.category.createCategory({
    category: {
      spec: {
        displayName: text.value,
        slug,
        description: "",
        cover: "",
        template: "",
        priority:
          categories.value?.filter((category) => !category.spec.parent)
            .length || 0,
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
  <VPopover
    v-model:open="dropdownVisible"
    :arrow="false"
    match-trigger-width
    :offset="10"
    content-class="!p-0 overflow-hidden"
  >
    <div :class="context.classes['post-categories-wrapper']">
      <!-- Keep chip interactions from toggling the popover through the trigger. -->
      <div :class="context.classes['post-categories']" @click.stop>
        <CategoryTag
          v-for="(category, index) in selectedCategories"
          :key="index"
          :category="category"
          @select="handleSelect"
        />
        <input
          ref="inputRef"
          :value="text"
          :class="context.classes.input"
          type="text"
          @input="onTextInput"
          @click.stop="dropdownVisible = true"
          @focus="dropdownVisible = true"
          @keydown="handleKeydown"
          @keydown.delete="handleDelete"
        />
      </div>

      <div :class="context.classes['post-categories-button']">
        <IconArrowRight class="rotate-90 text-gray-500 hover:text-gray-700" />
      </div>
    </div>
    <template #content>
      <div :class="context.classes['dropdown-wrapper']">
        <ul class="p-1">
          <HasPermission
            v-if="allowCreate && text.trim()"
            :permissions="['system:posts:manage']"
          >
            <li
              id="category-create"
              class="group flex cursor-pointer items-center justify-between rounded p-2"
              :class="{
                'bg-gray-100': selectedCategory === undefined,
              }"
              @click="handleCreateCategory"
            >
              <span class="text-xs text-gray-700 group-hover:text-gray-900">
                {{
                  $t("core.formkit.category_select.creation_label", {
                    text: text,
                  })
                }}
              </span>
            </li>
          </HasPermission>

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
              v-for="category in filteredCategoriesTree"
              :key="category.category.metadata.name"
              :category="category"
              @select="handleSelect"
            />
          </template>
        </ul>
      </div>
    </template>
  </VPopover>
</template>
