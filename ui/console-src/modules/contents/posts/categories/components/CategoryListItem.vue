<script setup lang="ts">
import { formatDatetime } from "@/utils/date";
import { usePermission } from "@/utils/permission";
import { coreApiClient, type Category } from "@halo-dev/api-client";
import {
  Dialog,
  IconEyeOff,
  IconList,
  IconMore,
  Toast,
  VDropdown,
  VDropdownItem,
  VStatusDot,
} from "@halo-dev/components";
import "@he-tree/vue/style/default.css";
import { useQueryClient } from "@tanstack/vue-query";
import { ref } from "vue";
import { useI18n } from "vue-i18n";
import GridiconsLinkBreak from "~icons/gridicons/link-break";
import { convertCategoryTreeToCategory, type CategoryTreeNode } from "../utils";
import CategoryEditingModal from "./CategoryEditingModal.vue";

const { currentUserHasPermission } = usePermission();
const { t } = useI18n();
const queryClient = useQueryClient();

const props = withDefaults(
  defineProps<{
    isChildLevel?: boolean;
    categoryTreeNode: CategoryTreeNode;
  }>(),
  {
    isChildLevel: false,
  }
);

const handleDelete = async () => {
  Dialog.warning({
    title: t("core.post_category.operations.delete.title"),
    description: t("core.post_category.operations.delete.description"),
    confirmType: "danger",
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    onConfirm: async () => {
      try {
        await coreApiClient.content.category.deleteCategory({
          name: props.categoryTreeNode.metadata.name,
        });

        Toast.success(t("core.common.toast.delete_success"));

        queryClient.invalidateQueries({ queryKey: ["post-categories"] });
      } catch (e) {
        console.error("Failed to delete category", e);
      }
    },
  });
};

// Editing category
const editingModal = ref(false);
const selectedCategory = ref<Category>();
const selectedParentCategory = ref<Category>();

function onEditingModalClose() {
  selectedCategory.value = undefined;
  selectedParentCategory.value = undefined;
  editingModal.value = false;
}

const handleOpenEditingModal = () => {
  selectedCategory.value = convertCategoryTreeToCategory(
    props.categoryTreeNode
  );
  editingModal.value = true;
};

const handleOpenCreateByParentModal = () => {
  selectedParentCategory.value = convertCategoryTreeToCategory(
    props.categoryTreeNode
  );
  editingModal.value = true;
};
</script>
<template>
  <div
    class="group relative flex w-full items-center justify-between px-4 py-3 hover:bg-gray-50"
  >
    <div class="min-w-0 flex-1 shrink">
      <div
        v-permission="['system:posts:manage']"
        class="drag-element absolute inset-y-0 left-0 hidden w-3.5 cursor-move items-center bg-gray-100 transition-all hover:bg-gray-200 group-hover:flex"
      >
        <IconList class="h-3.5 w-3.5" />
      </div>
      <div class="flex flex-col gap-1">
        <div class="inline-flex items-center gap-2">
          <span class="truncate text-sm font-medium text-gray-900">
            {{ categoryTreeNode.spec.displayName }}
          </span>
        </div>
        <a
          v-if="categoryTreeNode.status?.permalink"
          :href="categoryTreeNode.status?.permalink"
          :title="categoryTreeNode.status?.permalink"
          target="_blank"
          class="truncate text-xs text-gray-500 group-hover:text-gray-900"
        >
          {{ categoryTreeNode.status.permalink }}
        </a>
      </div>
    </div>
    <div class="flex flex-none items-center gap-6">
      <VStatusDot
        v-if="categoryTreeNode.metadata.deletionTimestamp"
        v-tooltip="$t('core.common.status.deleting')"
        state="warning"
        animate
      />
      <IconEyeOff
        v-if="categoryTreeNode.spec.hideFromList"
        v-tooltip="$t('core.post_category.list.fields.hide_from_list')"
        class="cursor-pointer text-sm transition-all hover:text-blue-600"
      />
      <GridiconsLinkBreak
        v-if="categoryTreeNode.spec.preventParentPostCascadeQuery"
        v-tooltip="
          $t('core.post_category.list.fields.prevent_parent_post_cascade_query')
        "
        class="cursor-pointer text-sm transition-all hover:text-blue-600"
      />
      <span class="truncate text-xs text-gray-500">
        {{
          $t("core.common.fields.post_count", {
            count: categoryTreeNode.status?.postCount || 0,
          })
        }}
      </span>
      <span class="truncate text-xs tabular-nums text-gray-500">
        {{ formatDatetime(categoryTreeNode.metadata.creationTimestamp) }}
      </span>
      <VDropdown v-if="currentUserHasPermission(['system:posts:manage'])">
        <div
          class="cursor-pointer rounded p-1 transition-all hover:text-blue-600 group-hover:bg-gray-200/60"
          @click.stop
        >
          <IconMore />
        </div>
        <template #popper>
          <VDropdownItem @click="handleOpenEditingModal">
            {{ $t("core.common.buttons.edit") }}
          </VDropdownItem>
          <VDropdownItem @click="handleOpenCreateByParentModal">
            {{ $t("core.post_category.operations.add_sub_category.button") }}
          </VDropdownItem>
          <VDropdownItem type="danger" @click="handleDelete">
            {{ $t("core.common.buttons.delete") }}
          </VDropdownItem>
        </template>
      </VDropdown>
    </div>

    <CategoryEditingModal
      v-if="editingModal"
      :is-child-level-category="isChildLevel"
      :category="selectedCategory"
      :parent-category="selectedParentCategory"
      @close="onEditingModalClose"
    />
  </div>
</template>
