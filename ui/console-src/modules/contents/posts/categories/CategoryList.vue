<script lang="ts" setup>
import { consoleApiClient } from "@halo-dev/api-client";
import {
  IconAddCircle,
  IconBookRead,
  VButton,
  VCard,
  VEmpty,
  VLoading,
  VPageHeader,
  VSpace,
} from "@halo-dev/components";
import { Draggable } from "@he-tree/vue";
import "@he-tree/vue/style/default.css";
import { ref } from "vue";
import CategoryEditingModal from "./components/CategoryEditingModal.vue";
import CategoryListItem from "./components/CategoryListItem.vue";
import { usePostCategory } from "./composables/use-post-category";
import { buildCategoryPositionRequest } from "./utils";

const creationModal = ref(false);

const {
  categories,
  categoriesTree,
  previousCategoriesTree,
  isLoading,
  handleFetchCategories,
  setCategoriesTree,
} = usePostCategory();

const positionUpdating = ref(false);

async function handleUpdatePosition() {
  if (positionUpdating.value) {
    return;
  }

  const positionRequest = buildCategoryPositionRequest(
    previousCategoriesTree.value,
    categoriesTree.value
  );
  if (!positionRequest) {
    await handleFetchCategories();
    return;
  }

  try {
    positionUpdating.value = true;
    const { data } =
      await consoleApiClient.content.category.updateCategoryPosition({
        name: positionRequest.name,
        categoryPositionRequest: {
          parentName: positionRequest.parentName,
          beforeName: positionRequest.beforeName,
        },
      });
    setCategoriesTree(data);
  } catch (e) {
    console.error("Failed to update category position", e);
    await handleFetchCategories();
  } finally {
    positionUpdating.value = false;
  }
}
</script>
<template>
  <CategoryEditingModal v-if="creationModal" @close="creationModal = false" />
  <VPageHeader :title="$t('core.post_category.title')">
    <template #icon>
      <IconBookRead />
    </template>

    <template #actions>
      <VButton
        v-permission="['system:posts:manage']"
        type="secondary"
        @click="creationModal = true"
      >
        <template #icon>
          <IconAddCircle />
        </template>
        {{ $t("core.common.buttons.new") }}
      </VButton>
    </template>
  </VPageHeader>
  <div class="m-0 md:m-4">
    <VCard :body-class="['!p-0']">
      <template #header>
        <div class="block w-full bg-gray-50 px-4 py-3">
          <div
            class="relative flex flex-col items-start sm:flex-row sm:items-center"
          >
            <div class="flex w-full flex-1 sm:w-auto">
              <span class="text-base font-medium">
                {{
                  $t("core.post_category.header.title", {
                    count: categories?.length || 0,
                  })
                }}
              </span>
            </div>
          </div>
        </div>
      </template>
      <VLoading v-if="isLoading" />
      <Transition v-else-if="!categories?.length" appear name="fade">
        <VEmpty
          :message="$t('core.post_category.empty.message')"
          :title="$t('core.post_category.empty.title')"
        >
          <template #actions>
            <VSpace>
              <VButton @click="handleFetchCategories">
                {{ $t("core.common.buttons.refresh") }}
              </VButton>
              <VButton
                v-permission="['system:posts:manage']"
                type="secondary"
                @click="creationModal = true"
              >
                <template #icon>
                  <IconAddCircle />
                </template>
                {{ $t("core.common.buttons.new") }}
              </VButton>
            </VSpace>
          </template>
        </VEmpty>
      </Transition>
      <Transition v-else appear name="fade">
        <Draggable
          v-model="categoriesTree"
          :class="{
            'cursor-progress opacity-60': positionUpdating,
          }"
          :disable-drag="positionUpdating"
          trigger-class="drag-element"
          :indent="40"
          @after-drop="handleUpdatePosition"
        >
          <template #default="{ node }">
            <CategoryListItem :category-tree-node="node" />
          </template>
        </Draggable>
      </Transition>
    </VCard>
  </div>
</template>
<style scoped>
:deep(.vtlist-inner) {
  @apply divide-y divide-gray-100;
}
:deep(.he-tree-drag-placeholder) {
  box-sizing: border-box;
  height: 60px;
  width: 100%;
  border: 1px dashed #00d9ff;
  background: #ddf2f9;
}
</style>
