<script lang="ts" setup>
import type { Category } from "@halo-dev/api-client";
import { VEntity, VEntityField, VDropdown } from "@halo-dev/components";
import { setFocus } from "@/formkit/utils/focus";
import { computed, ref, watch } from "vue";
import Fuse from "fuse.js";
import { usePostCategory } from "@/modules/contents/posts/categories/composables/use-post-category";

const props = withDefaults(
  defineProps<{
    selected?: Category;
  }>(),
  {
    selected: undefined,
  }
);

const emit = defineEmits<{
  (event: "update:selected", category?: Category): void;
  (event: "select", category?: Category): void;
}>();

const { categories } = usePostCategory();

const handleSelect = (category: Category) => {
  if (
    props.selected &&
    category.metadata.name === props.selected.metadata.name
  ) {
    emit("update:selected", undefined);
    emit("select", undefined);
    return;
  }

  emit("update:selected", category);
  emit("select", category);
};

function onDropdownShow() {
  setTimeout(() => {
    setFocus("categoryDropdownSelectorInput");
  }, 200);
}

// search
const keyword = ref("");

let fuse: Fuse<Category> | undefined = undefined;

watch(
  () => categories.value,
  () => {
    fuse = new Fuse(categories.value || [], {
      keys: ["spec.displayName", "metadata.name"],
      useExtendedSearch: true,
      threshold: 0.2,
    });
  },
  {
    immediate: true,
  }
);

const searchResults = computed(() => {
  if (!fuse || !keyword.value) {
    return categories.value;
  }

  return fuse?.search(keyword.value).map((item) => item.item);
});
</script>

<template>
  <VDropdown :classes="['!p-0']" @show="onDropdownShow">
    <slot />
    <template #popper>
      <div class="h-96 w-80">
        <div class="border-b border-b-gray-100 bg-white p-4">
          <FormKit
            id="categoryDropdownSelectorInput"
            v-model="keyword"
            :placeholder="$t('core.common.placeholder.search')"
            type="text"
          ></FormKit>
        </div>
        <div>
          <ul
            class="box-border h-full w-full divide-y divide-gray-100"
            role="list"
          >
            <li
              v-for="(category, index) in searchResults"
              :key="index"
              v-close-popper
              @click="handleSelect(category)"
            >
              <VEntity
                :is-selected="
                  selected?.metadata.name === category.metadata.name
                "
              >
                <template #start>
                  <VEntityField
                    :title="category.spec.displayName"
                    :description="category.status?.permalink"
                  />
                </template>
                <template #end>
                  <VEntityField
                    :description="
                      $t('core.common.fields.post_count', {
                        count: category.status?.postCount || 0,
                      })
                    "
                  />
                </template>
              </VEntity>
            </li>
          </ul>
        </div>
      </div>
    </template>
  </VDropdown>
</template>
