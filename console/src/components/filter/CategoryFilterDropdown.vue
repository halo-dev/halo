<script lang="ts" setup>
import type { Category } from "@halo-dev/api-client";
import {
  VEntity,
  VEntityField,
  VDropdown,
  IconArrowDown,
} from "@halo-dev/components";
import { setFocus } from "@/formkit/utils/focus";
import { computed, ref, watch } from "vue";
import Fuse from "fuse.js";
import { usePostCategory } from "@/modules/contents/posts/categories/composables/use-post-category";

const props = withDefaults(
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

const { categories } = usePostCategory();

const dropdown = ref();

const handleSelect = (category: Category) => {
  if (category.metadata.name === props.modelValue) {
    emit("update:modelValue", undefined);
  } else {
    emit("update:modelValue", category.metadata.name);
  }

  dropdown.value.hide();
};

function onDropdownShow() {
  setTimeout(() => {
    setFocus("categoryFilterDropdownInput");
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

const selectedCategory = computed(() => {
  return categories.value?.find(
    (category) => category.metadata.name === props.modelValue
  );
});
</script>

<template>
  <VDropdown ref="dropdown" :classes="['!p-0']" @show="onDropdownShow">
    <div
      class="flex cursor-pointer select-none items-center text-sm text-gray-700 hover:text-black"
      :class="{ 'font-semibold text-gray-700': modelValue !== undefined }"
    >
      <span v-if="!selectedCategory" class="mr-0.5">
        {{ label }}
      </span>
      <span v-else class="mr-0.5">
        {{ label }}：{{ selectedCategory.spec.displayName }}
      </span>
      <span>
        <IconArrowDown />
      </span>
    </div>
    <template #popper>
      <div class="h-96 w-80">
        <div class="border-b border-b-gray-100 bg-white p-4">
          <FormKit
            id="categoryFilterDropdownInput"
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
              @click="handleSelect(category)"
            >
              <VEntity :is-selected="modelValue === category.metadata.name">
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
