<script lang="ts" setup>
import { apiClient } from "@/utils/api-client";
import type { Group } from "@halo-dev/api-client";
import {
  Dialog,
  IconCheckboxCircle,
  IconMore,
  Toast,
  VDropdown,
  VDropdownItem,
  VStatusDot,
} from "@halo-dev/components";
import { useQueryClient } from "@tanstack/vue-query";
import { ref } from "vue";
import { useI18n } from "vue-i18n";
import AttachmentGroupEditingModal from "./AttachmentGroupEditingModal.vue";

const props = withDefaults(
  defineProps<{
    group?: Group;
    isSelected?: boolean;
    features?: { actions: boolean; checkIcon?: boolean };
  }>(),
  {
    group: undefined,
    isSelected: false,
    features: () => {
      return {
        actions: true,
        checkIcon: false,
      };
    },
  }
);

const { t } = useI18n();
const queryClient = useQueryClient();

const handleDelete = () => {
  Dialog.warning({
    title: t("core.attachment.group_list.operations.delete.title"),
    description: t("core.attachment.group_list.operations.delete.description"),
    confirmType: "danger",
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    onConfirm: async () => {
      if (!props.group) {
        return;
      }

      // TODO: 后续将修改为在后端进行批量操作处理
      const { data } = await apiClient.attachment.searchAttachments({
        fieldSelector: [`spec.groupName=${props.group.metadata.name}`],
        page: 0,
        size: 0,
      });

      await apiClient.extension.storage.group.deleteStorageHaloRunV1alpha1Group(
        { name: props.group.metadata.name }
      );

      // move attachments to none group
      const moveToUnGroupRequests = data.items.map((attachment) => {
        attachment.spec.groupName = undefined;
        return apiClient.extension.storage.attachment.updateStorageHaloRunV1alpha1Attachment(
          {
            name: attachment.metadata.name,
            attachment: attachment,
          }
        );
      });

      await Promise.all(moveToUnGroupRequests);

      queryClient.invalidateQueries({ queryKey: ["attachment-groups"] });
      queryClient.invalidateQueries({ queryKey: ["attachments"] });

      Toast.success(
        t("core.attachment.group_list.operations.delete.toast_success", {
          total: data.total,
        })
      );
    },
  });
};

const handleDeleteWithAttachments = () => {
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
      if (!props.group) {
        return;
      }

      // TODO: 后续将修改为在后端进行批量操作处理
      const { data } = await apiClient.attachment.searchAttachments({
        fieldSelector: [`spec.groupName=${props.group.metadata.name}`],
        page: 0,
        size: 0,
      });

      await apiClient.extension.storage.group.deleteStorageHaloRunV1alpha1Group(
        { name: props.group.metadata.name }
      );

      const deleteAttachmentRequests = data.items.map((attachment) => {
        return apiClient.extension.storage.attachment.deleteStorageHaloRunV1alpha1Attachment(
          { name: attachment.metadata.name }
        );
      });

      await Promise.all(deleteAttachmentRequests);

      queryClient.invalidateQueries({ queryKey: ["attachment-groups"] });
      queryClient.invalidateQueries({ queryKey: ["attachments"] });

      Toast.success(
        t(
          "core.attachment.group_list.operations.delete_with_attachments.toast_success",
          { total: data.total }
        )
      );
    },
  });
};

// Editing
const editingModalVisible = ref(false);

const onEditingModalClose = () => {
  queryClient.invalidateQueries({ queryKey: ["attachment-groups"] });
  editingModalVisible.value = false;
};
</script>

<template>
  <button
    type="button"
    class="inline-flex h-full w-full items-center gap-2 rounded-md border border-gray-200 bg-white px-3 py-2.5 text-sm font-medium text-gray-800 hover:bg-gray-50 hover:shadow-sm"
    :class="{ '!bg-gray-100 shadow-sm': isSelected }"
  >
    <div class="inline-flex w-full flex-1 gap-x-2 break-all text-left">
      <slot name="text">
        {{ group?.spec.displayName }}
      </slot>
      <VStatusDot
        v-if="group?.metadata.deletionTimestamp"
        v-tooltip="$t('core.common.status.deleting')"
        state="warning"
        animate
      />
    </div>
    <div class="flex-none">
      <HasPermission
        v-if="features.actions"
        :permissions="['system:attachments:manage']"
      >
        <VDropdown>
          <IconMore @click.stop />
          <template #popper>
            <VDropdownItem @click="editingModalVisible = true">
              {{ $t("core.attachment.group_list.operations.rename.button") }}
            </VDropdownItem>
            <VDropdown placement="right" :triggers="['click']">
              <VDropdownItem type="danger">
                {{ $t("core.common.buttons.delete") }}
              </VDropdownItem>
              <template #popper>
                <VDropdownItem type="danger" @click="handleDelete()">
                  {{
                    $t("core.attachment.group_list.operations.delete.button")
                  }}
                </VDropdownItem>
                <VDropdownItem
                  type="danger"
                  @click="handleDeleteWithAttachments()"
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
      </HasPermission>

      <IconCheckboxCircle
        v-if="isSelected && features.checkIcon"
        class="text-primary"
      />

      <slot name="actions" />
    </div>

    <AttachmentGroupEditingModal
      v-if="editingModalVisible"
      :group="group"
      @close="onEditingModalClose"
    />
  </button>
</template>
