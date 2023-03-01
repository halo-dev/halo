<script lang="ts" setup>
import {
  IconList,
  VButton,
  VStatusDot,
  VEntity,
  VEntityField,
} from "@halo-dev/components";
import Draggable from "vuedraggable";
import type { CategoryTree } from "../utils";
import { ref } from "vue";
import { formatDatetime } from "@/utils/date";
import { usePermission } from "@/utils/permission";

const { currentUserHasPermission } = usePermission();

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
        <VEntity>
          <template #prepend>
            <div
              v-permission="['system:posts:manage']"
              class="drag-element absolute inset-y-0 left-0 hidden w-3.5 cursor-move items-center bg-gray-100 transition-all hover:bg-gray-200 group-hover:flex"
            >
              <IconList class="h-3.5 w-3.5" />
            </div>
          </template>
          <template #start>
            <VEntityField :title="category.spec.displayName">
              <template #description>
                <a
                  v-if="category.status.permalink"
                  :href="category.status.permalink"
                  :title="category.status.permalink"
                  target="_blank"
                  class="truncate text-xs text-gray-500 group-hover:text-gray-900"
                >
                  {{ category.status.permalink }}
                </a>
              </template>
            </VEntityField>
          </template>
          <template #end>
            <VEntityField v-if="category.metadata.deletionTimestamp">
              <template #description>
                <VStatusDot v-tooltip="`删除中`" state="warning" animate />
              </template>
            </VEntityField>
            <VEntityField
              :description="`${category.status?.postCount || 0} 篇文章`"
            />
            <VEntityField>
              <template #description>
                <span class="truncate text-xs tabular-nums text-gray-500">
                  {{ formatDatetime(category.metadata.creationTimestamp) }}
                </span>
              </template>
            </VEntityField>
          </template>
          <template
            v-if="currentUserHasPermission(['system:posts:manage'])"
            #dropdownItems
          >
            <VButton
              v-permission="['system:posts:manage']"
              v-close-popper
              block
              type="secondary"
              @click="onOpenEditingModal(category)"
            >
              修改
            </VButton>
            <VButton
              v-permission="['system:posts:manage']"
              v-close-popper
              block
              type="danger"
              @click="onDelete(category)"
            >
              删除
            </VButton>
          </template>
        </VEntity>
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
