<script lang="ts" setup>
import { IconList, IconSettings, VButton, VSpace } from "@halo-dev/components";
import Draggable from "vuedraggable";
import type { CategoryTree } from "../utils";
import { ref } from "vue";
import { formatDatetime } from "@/utils/date";

withDefaults(
  defineProps<{
    categories: CategoryTree[];
  }>(),
  {
    categories: () => [],
  }
);

const emit = defineEmits<{
  (event: "change"): void;
  (event: "open-editing", category: CategoryTree): void;
  (event: "delete", category: CategoryTree): void;
}>();

const isDragging = ref(false);

function onChange() {
  emit("change");
}

function onOpenEditingModal(category: CategoryTree) {
  emit("open-editing", category);
}

function onDelete(category: CategoryTree) {
  emit("delete", category);
}
</script>
<template>
  <draggable
    :list="categories"
    class="box-border h-full w-full divide-y divide-gray-100"
    ghost-class="opacity-50"
    group="category-item"
    handle=".drag-element"
    item-key="metadata.name"
    tag="ul"
    @change="onChange"
    @end="isDragging = false"
    @start="isDragging = true"
  >
    <template #item="{ element: category }">
      <li>
        <div
          class="group relative block cursor-pointer px-4 py-3 transition-all hover:bg-gray-50"
        >
          <div
            class="drag-element absolute inset-y-0 left-0 hidden w-3.5 cursor-move items-center bg-gray-100 transition-all hover:bg-gray-200 group-hover:flex"
          >
            <IconList class="h-3.5 w-3.5" />
          </div>
          <div class="relative flex flex-row items-center">
            <div class="flex-1">
              <div class="flex flex-col sm:flex-row">
                <span
                  class="mr-0 truncate text-sm font-medium text-gray-900 sm:mr-2"
                >
                  {{ category.spec.displayName }}
                </span>
                <VSpace class="mt-1 sm:mt-0"></VSpace>
              </div>
              <div class="mt-1 flex">
                <span class="text-xs text-gray-500">
                  /categories/{{ category.spec.slug }}
                </span>
              </div>
            </div>
            <div class="flex">
              <div
                class="inline-flex flex-col items-end gap-4 sm:flex-row sm:items-center sm:gap-6"
              >
                <FloatingTooltip
                  v-if="category.metadata.deletionTimestamp"
                  class="mr-4 hidden items-center sm:flex"
                >
                  <div class="inline-flex h-1.5 w-1.5 rounded-full bg-red-600">
                    <span
                      class="inline-block h-1.5 w-1.5 animate-ping rounded-full bg-red-600"
                    ></span>
                  </div>
                  <template #popper> 删除中</template>
                </FloatingTooltip>
                <!--TODO: Get post count-->
                <div
                  v-if="false"
                  class="cursor-pointer text-sm text-gray-500 hover:text-gray-900"
                >
                  20 篇文章
                </div>
                <time class="text-sm text-gray-500">
                  {{ formatDatetime(category.metadata.creationTimestamp) }}
                </time>
                <span class="self-center">
                  <FloatingDropdown>
                    <IconSettings
                      class="cursor-pointer transition-all hover:text-blue-600"
                    />
                    <template #popper>
                      <div class="w-48 p-2">
                        <VSpace class="w-full" direction="column">
                          <VButton
                            v-close-popper
                            block
                            type="secondary"
                            @click="onOpenEditingModal(category)"
                          >
                            修改
                          </VButton>
                          <VButton
                            v-close-popper
                            block
                            type="danger"
                            @click="onDelete(category)"
                          >
                            删除
                          </VButton>
                        </VSpace>
                      </div>
                    </template>
                  </FloatingDropdown>
                </span>
              </div>
            </div>
          </div>
        </div>
        <CategoryListItem
          :categories="category.spec.children"
          class="pl-10 transition-all duration-300"
          @change="onChange"
          @delete="onDelete"
          @open-editing="onOpenEditingModal"
        />
      </li>
    </template>
  </draggable>
</template>
