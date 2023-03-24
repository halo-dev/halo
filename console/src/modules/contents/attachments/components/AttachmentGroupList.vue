<script lang="ts" setup>
// core libs
import { onMounted, ref, watch } from "vue";

// components
import {
  Dialog,
  IconAddCircle,
  IconMore,
  Toast,
  VDropdown,
  VDropdownItem,
  VStatusDot,
} from "@halo-dev/components";
import AttachmentGroupEditingModal from "./AttachmentGroupEditingModal.vue";

// types
import type { Group } from "@halo-dev/api-client";

import { useRouteQuery } from "@vueuse/router";
import { useFetchAttachmentGroup } from "../composables/use-attachment-group";
import { apiClient } from "@/utils/api-client";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

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
      displayName: t("core.attachment.group_list.internal_groups.all"),
    },
    apiVersion: "",
    kind: "",
    metadata: {
      name: "",
    },
  },
  {
    spec: {
      displayName: t("core.attachment.common.text.ungrouped"),
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
    title: t("core.attachment.group_list.operations.delete.title"),
    description: t("core.attachment.group_list.operations.delete.title"),
    confirmType: "danger",
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
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

      Toast.success(
        t("core.attachment.group_list.operations.delete.toast_success", {
          total: data.total,
        })
      );
    },
  });
};

const handleDeleteWithAttachments = (group: Group) => {
  Dialog.warning({
    title: t(
      "core.attachment.group_list.operations.delete_with_attachments.title"
    ),
    description: t(
      "core.attachment.group_list.operations.delete_with_attachments.description"
    ),
    confirmType: "danger",
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
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

      Toast.success(
        t(
          "core.attachment.group_list.operations.delete_with_attachments.toast_success",
          { total: data.total }
        )
      );
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
          v-tooltip="$t('core.common.status.deleting')"
          state="warning"
          animate
        />
      </div>
      <VDropdown v-if="!readonly" v-permission="['system:attachments:manage']">
        <IconMore @click.stop />
        <template #popper>
          <VDropdownItem @click="handleOpenEditingModal(group)">
            {{ $t("core.attachment.group_list.operations.rename.button") }}
          </VDropdownItem>
          <VDropdown placement="right" :triggers="['click']">
            <VDropdownItem type="danger">
              {{ $t("core.common.buttons.delete") }}
            </VDropdownItem>
            <template #popper>
              <VDropdownItem
                v-close-popper.all
                type="danger"
                @click="handleDelete(group)"
              >
                {{ $t("core.attachment.group_list.operations.delete.button") }}
              </VDropdownItem>
              <VDropdownItem
                v-close-popper.all
                type="danger"
                @click="handleDeleteWithAttachments(group)"
              >
                {{
                  $t(
                    "core.attachment.group_list.operations.delete_with_attachments.button"
                  )
                }}
              </VDropdownItem>
            </template>
          </VDropdown>
        </template>
      </VDropdown>
    </div>
    <div
      v-if="!loading && !readonly"
      v-permission="['system:attachments:manage']"
      class="flex cursor-pointer items-center rounded-base bg-gray-100 p-2 text-gray-500 transition-all hover:bg-gray-200 hover:text-gray-900 hover:shadow-sm"
      @click="editingModal = true"
    >
      <div class="flex flex-1 items-center truncate">
        <span class="truncate text-sm">
          {{ $t("core.common.buttons.new") }}
        </span>
      </div>
      <IconAddCircle />
    </div>
  </div>
</template>
