<script lang="ts" setup>
// core libs
import { onMounted, ref, watch } from "vue";

// components
import {
  Dialog,
  IconAddCircle,
  IconMore,
  Toast,
  VButton,
  VSpace,
  VStatusDot,
} from "@halo-dev/components";
import AttachmentGroupEditingModal from "./AttachmentGroupEditingModal.vue";

// types
import type { Group } from "@halo-dev/api-client";

import { useRouteQuery } from "@vueuse/router";
import { useFetchAttachmentGroup } from "../composables/use-attachment-group";
import { apiClient } from "@/utils/api-client";

const props = withDefaults(
  defineProps<{
    selectedGroup: Group | undefined;
    readonly?: boolean;
  }>(),
  {
    selectedGroup: undefined,
    readonly: false,
  }
);

const emit = defineEmits<{
  (event: "update:selectedGroup", group: Group): void;
  (event: "select", group: Group): void;
  (event: "update"): void;
  (event: "reload-attachments"): void;
}>();

const defaultGroups: Group[] = [
  {
    spec: {
      displayName: "全部",
    },
    apiVersion: "",
    kind: "",
    metadata: {
      name: "",
    },
  },
  {
    spec: {
      displayName: "未分组",
    },
    apiVersion: "",
    kind: "",
    metadata: {
      name: "ungrouped",
    },
  },
];

const { groups, handleFetchGroups } = useFetchAttachmentGroup();

const groupToUpdate = ref<Group | null>(null);
const loading = ref<boolean>(false);
const editingModal = ref(false);

const routeQuery = useRouteQuery<string>("group");

const handleSelectGroup = (group: Group) => {
  emit("update:selectedGroup", group);
  emit("select", group);

  if (!props.readonly) {
    routeQuery.value = group.metadata.name;
  }
};

const handleOpenEditingModal = (group: Group) => {
  groupToUpdate.value = group;
  editingModal.value = true;
};

const onEditingModalClose = () => {
  emit("update");
  handleFetchGroups();
};

const handleDelete = (group: Group) => {
  Dialog.warning({
    title: "确定要删除该分组吗？",
    description: "将删除分组，并将分组下的附件移动至未分组，该操作不可恢复。",
    confirmType: "danger",
    onConfirm: async () => {
      // TODO: 后续将修改为在后端进行批量操作处理
      const { data } = await apiClient.attachment.searchAttachments({
        group: group.metadata.name,
        page: 0,
        size: 0,
      });

      await apiClient.extension.storage.group.deletestorageHaloRunV1alpha1Group(
        { name: group.metadata.name }
      );

      // move attachments to none group
      const moveToUnGroupRequests = data.items.map((attachment) => {
        attachment.spec.groupName = undefined;
        return apiClient.extension.storage.attachment.updatestorageHaloRunV1alpha1Attachment(
          {
            name: attachment.metadata.name,
            attachment: attachment,
          }
        );
      });

      await Promise.all(moveToUnGroupRequests);

      handleFetchGroups();
      emit("reload-attachments");
      emit("update");

      Toast.success(`删除成功，${data.total} 个附件已移动至未分组`);
    },
  });
};

const handleDeleteWithAttachments = (group: Group) => {
  Dialog.warning({
    title: "确定要删除该分组吗？",
    description: "将删除分组以及分组下的所有附件，该操作不可恢复。",
    confirmType: "danger",
    onConfirm: async () => {
      // TODO: 后续将修改为在后端进行批量操作处理
      const { data } = await apiClient.attachment.searchAttachments({
        group: group.metadata.name,
        page: 0,
        size: 0,
      });

      await apiClient.extension.storage.group.deletestorageHaloRunV1alpha1Group(
        { name: group.metadata.name }
      );

      const deleteAttachmentRequests = data.items.map((attachment) => {
        return apiClient.extension.storage.attachment.deletestorageHaloRunV1alpha1Attachment(
          { name: attachment.metadata.name }
        );
      });

      await Promise.all(deleteAttachmentRequests);

      handleFetchGroups();
      emit("reload-attachments");
      emit("update");

      Toast.success(`删除成功，${data.total} 个附件已被同时删除`);
    },
  });
};

watch(
  () => groups.value?.length,
  () => {
    const allGroups = [...defaultGroups, ...(groups.value || [])];
    const groupIndex = allGroups.findIndex(
      (group) => group.metadata.name === routeQuery.value
    );

    if (groupIndex < 0) {
      handleSelectGroup(defaultGroups[0]);
    }
  }
);

onMounted(async () => {
  await handleFetchGroups();
  if (routeQuery.value && !props.readonly) {
    const allGroups = [...defaultGroups, ...(groups.value || [])];
    const group = allGroups.find(
      (group) => group.metadata.name === routeQuery.value
    );
    if (group) {
      handleSelectGroup(group);
      return;
    }
  }

  handleSelectGroup(defaultGroups[0]);
});
</script>
<template>
  <AttachmentGroupEditingModal
    v-if="!readonly"
    v-model:visible="editingModal"
    :group="groupToUpdate"
    @close="onEditingModalClose"
  />
  <div class="mb-5 grid grid-cols-2 gap-x-2 gap-y-3 sm:grid-cols-6">
    <div
      v-for="(defaultGroup, index) in defaultGroups"
      :key="index"
      :class="{
        '!bg-gray-200 !text-gray-900':
          defaultGroup.metadata.name === selectedGroup?.metadata.name,
      }"
      class="flex cursor-pointer items-center rounded-base bg-gray-100 p-2 text-gray-500 transition-all hover:bg-gray-200 hover:text-gray-900 hover:shadow-sm"
      @click="handleSelectGroup(defaultGroup)"
    >
      <div class="flex flex-1 items-center">
        <span class="text-sm">{{ defaultGroup.spec.displayName }}</span>
      </div>
    </div>
    <div
      v-for="(group, index) in groups"
      :key="index"
      :class="{
        '!bg-gray-200 !text-gray-900':
          group.metadata.name === selectedGroup?.metadata.name,
      }"
      class="flex cursor-pointer items-center rounded-base bg-gray-100 p-2 text-gray-500 transition-all hover:bg-gray-200 hover:text-gray-900 hover:shadow-sm"
      @click="handleSelectGroup(group)"
    >
      <div class="flex flex-1 items-center gap-2 truncate">
        <span class="truncate text-sm">
          {{ group.spec.displayName }}
        </span>
        <VStatusDot
          v-if="group.metadata.deletionTimestamp"
          v-tooltip="`删除中`"
          state="warning"
          animate
        />
      </div>
      <FloatingDropdown
        v-if="!readonly"
        v-permission="['system:attachments:manage']"
      >
        <IconMore @click.stop />
        <template #popper>
          <div class="w-48 p-2">
            <VSpace class="w-full" direction="column">
              <VButton
                v-close-popper
                block
                type="secondary"
                @click="handleOpenEditingModal(group)"
              >
                重命名
              </VButton>
              <FloatingDropdown
                class="w-full"
                placement="right"
                :triggers="['click']"
              >
                <VButton block type="danger">删除</VButton>
                <template #popper>
                  <div class="w-52 p-2">
                    <VSpace class="w-full" direction="column">
                      <VButton
                        v-close-popper.all
                        block
                        type="danger"
                        size="sm"
                        @click="handleDelete(group)"
                      >
                        删除并将附件移动至未分组
                      </VButton>
                      <VButton
                        v-close-popper.all
                        block
                        type="danger"
                        size="sm"
                        @click="handleDeleteWithAttachments(group)"
                      >
                        删除并同时删除附件
                      </VButton>
                    </VSpace>
                  </div>
                </template>
              </FloatingDropdown>
            </VSpace>
          </div>
        </template>
      </FloatingDropdown>
    </div>
    <div
      v-if="!loading && !readonly"
      v-permission="['system:attachments:manage']"
      class="flex cursor-pointer items-center rounded-base bg-gray-100 p-2 text-gray-500 transition-all hover:bg-gray-200 hover:text-gray-900 hover:shadow-sm"
      @click="editingModal = true"
    >
      <div class="flex flex-1 items-center truncate">
        <span class="truncate text-sm">添加分组</span>
      </div>
      <IconAddCircle />
    </div>
  </div>
</template>
