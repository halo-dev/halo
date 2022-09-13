<script lang="ts" setup>
import { IconList, VButton } from "@halo-dev/components";
import Draggable from "vuedraggable";
import type { CategoryTree } from "../utils";
import { ref } from "vue";
import { formatDatetime } from "@/utils/date";
import Entity from "@/components/entity/Entity.vue";
import EntityField from "@/components/entity/EntityField.vue";

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
        <Entity>
          <template #prepend>
            <div
              class="drag-element absolute inset-y-0 left-0 hidden w-3.5 cursor-move items-center bg-gray-100 transition-all hover:bg-gray-200 group-hover:flex"
            >
              <IconList class="h-3.5 w-3.5" />
            </div>
          </template>
          <template #start>
            <EntityField
              :title="category.spec.displayName"
              :description="category.status.permalink"
            />
          </template>
          <template #end>
            <EntityField v-if="category.metadata.deletionTimestamp">
              <template #description>
                <div
                  v-tooltip="`删除中`"
                  class="inline-flex h-1.5 w-1.5 rounded-full bg-red-600"
                >
                  <span
                    class="inline-block h-1.5 w-1.5 animate-ping rounded-full bg-red-600"
                  ></span>
                </div>
              </template>
            </EntityField>
            <EntityField
              :description="`${category.status?.posts?.length || 0} 篇文章`"
            />
            <EntityField
              :description="formatDatetime(category.metadata.creationTimestamp)"
            />
          </template>
          <template #menuItems>
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
          </template>
        </Entity>
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
