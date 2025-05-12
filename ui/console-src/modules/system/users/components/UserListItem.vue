<script setup lang="ts">
import { rbacAnnotations } from "@/constants/annotations";
import { useUserStore } from "@/stores/user";
import { formatDatetime } from "@/utils/date";
import { usePermission } from "@/utils/permission";
import { coreApiClient, type ListedUser } from "@halo-dev/api-client";
import {
  Dialog,
  IconShieldUser,
  Toast,
  VAvatar,
  VDropdownDivider,
  VDropdownItem,
  VEntity,
  VEntityField,
  VStatusDot,
  VTag,
} from "@halo-dev/components";
import { useQueryClient } from "@tanstack/vue-query";
import { storeToRefs } from "pinia";
import { ref } from "vue";
import { useI18n } from "vue-i18n";
import { useUserEnableDisable } from "../composables/use-user";
import GrantPermissionModal from "./GrantPermissionModal.vue";
import UserEditingModal from "./UserEditingModal.vue";
import UserPasswordChangeModal from "./UserPasswordChangeModal.vue";

const props = withDefaults(
  defineProps<{
    user: ListedUser;
    isSelected?: boolean;
  }>(),
  { isSelected: false }
);

const queryClient = useQueryClient();
const { currentUserHasPermission } = usePermission();
const { t } = useI18n();
const { currentUser } = storeToRefs(useUserStore());

const handleDelete = async () => {
  Dialog.warning({
    title: t("core.user.operations.delete.title"),
    description: t("core.common.dialog.descriptions.cannot_be_recovered"),
    confirmType: "danger",
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    onConfirm: async () => {
      try {
        await coreApiClient.user.deleteUser({
          name: props.user.user.metadata.name,
        });

        Toast.success(t("core.common.toast.delete_success"));
      } catch (e) {
        console.error("Failed to delete user", e);
      } finally {
        queryClient.invalidateQueries({
          queryKey: ["users"],
        });
      }
    },
  });
};

const grantPermissionModal = ref(false);

function onGrantPermissionModalClose() {
  grantPermissionModal.value = false;
  queryClient.invalidateQueries({
    queryKey: ["users"],
  });
}

const passwordChangeModal = ref<boolean>(false);

function onPasswordChangeModalClose() {
  passwordChangeModal.value = false;
  queryClient.invalidateQueries({
    queryKey: ["users"],
  });
}

const editingModal = ref<boolean>(false);

function onEditingModalClose() {
  editingModal.value = false;
  queryClient.invalidateQueries({
    queryKey: ["users"],
  });
}

const { handleEnableOrDisableUser } = useUserEnableDisable();
</script>

<template>
  <UserEditingModal
    v-if="editingModal"
    :user="user.user"
    @close="onEditingModalClose"
  />
  <GrantPermissionModal
    v-if="grantPermissionModal"
    :user="user.user"
    @close="onGrantPermissionModalClose"
  />
  <UserPasswordChangeModal
    v-if="passwordChangeModal"
    :user="user.user"
    @close="onPasswordChangeModalClose"
  />
  <VEntity :is-selected="isSelected">
    <template #checkbox>
      <slot name="checkbox" />
    </template>
    <template #start>
      <VEntityField>
        <template #description>
          <VAvatar
            :alt="user.user.spec.displayName"
            :src="user.user.spec.avatar"
            size="md"
          ></VAvatar>
        </template>
      </VEntityField>
      <VEntityField
        :title="user.user.spec.displayName"
        :description="user.user.metadata.name"
        :route="{
          name: 'UserDetail',
          params: { name: user.user.metadata.name },
        }"
      >
        <template v-if="user.user.spec.disabled" #extra>
          <VTag>
            {{ $t("core.user.fields.disabled") }}
          </VTag>
        </template>
      </VEntityField>
    </template>
    <template #end>
      <VEntityField>
        <template #description>
          <div class="flex flex-wrap gap-2">
            <VTag v-for="role in user.roles" :key="role.metadata.name">
              <template #leftIcon>
                <IconShieldUser />
              </template>
              {{
                role.metadata.annotations?.[rbacAnnotations.DISPLAY_NAME] ||
                role.metadata.name
              }}
            </VTag>
          </div>
        </template>
      </VEntityField>
      <VEntityField v-if="user.user.metadata.deletionTimestamp">
        <template #description>
          <VStatusDot
            v-tooltip="$t('core.common.status.deleting')"
            state="warning"
            animate
          />
        </template>
      </VEntityField>
      <VEntityField>
        <template #description>
          <span class="truncate text-xs tabular-nums text-gray-500">
            {{ formatDatetime(user.user.metadata.creationTimestamp) }}
          </span>
        </template>
      </VEntityField>
    </template>
    <template
      v-if="currentUserHasPermission(['system:users:manage'])"
      #dropdownItems
    >
      <VDropdownItem @click="editingModal = true">
        {{ $t("core.user.operations.update_profile.title") }}
      </VDropdownItem>
      <VDropdownItem @click="passwordChangeModal = true">
        {{ $t("core.user.operations.change_password.title") }}
      </VDropdownItem>
      <VDropdownItem
        v-if="currentUser?.metadata.name !== user.user.metadata.name"
        @click="grantPermissionModal = true"
      >
        {{ $t("core.user.operations.grant_permission.title") }}
      </VDropdownItem>
      <VDropdownDivider
        v-if="currentUser?.metadata.name !== user.user.metadata.name"
      />
      <VDropdownItem
        v-if="currentUser?.metadata.name !== user.user.metadata.name"
        type="danger"
        @click="
          handleEnableOrDisableUser({
            name: user.user.metadata.name,
            operation: user.user.spec.disabled ? 'enable' : 'disable',
            onSuccess: () => {
              queryClient.invalidateQueries({
                queryKey: ['users'],
              });
            },
          })
        "
      >
        {{
          user.user.spec.disabled
            ? $t("core.user.operations.enable.title")
            : $t("core.user.operations.disable.title")
        }}
      </VDropdownItem>
      <VDropdownItem
        v-if="currentUser?.metadata.name !== user.user.metadata.name"
        type="danger"
        @click="handleDelete"
      >
        {{ $t("core.common.buttons.delete") }}
      </VDropdownItem>
    </template>
  </VEntity>
</template>
