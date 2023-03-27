<script lang="ts" setup>
// core libs
import { computed, ref, watch } from "vue";
import type { Role } from "@halo-dev/api-client";

// components
import {
  IconAddCircle,
  IconShieldUser,
  Dialog,
  VButton,
  VCard,
  VPageHeader,
  VTag,
  VStatusDot,
  VEntity,
  VEntityField,
  VLoading,
  Toast,
  VDropdownItem,
} from "@halo-dev/components";
import RoleEditingModal from "./components/RoleEditingModal.vue";

// constants
import { rbacAnnotations } from "@/constants/annotations";
import { formatDatetime } from "@/utils/date";

// hooks
import { useFetchRole } from "@/modules/system/roles/composables/use-role";

// libs
import cloneDeep from "lodash.clonedeep";
import { apiClient } from "@/utils/api-client";
import Fuse from "fuse.js";
import { usePermission } from "@/utils/permission";
import { roleLabels } from "@/constants/labels";
import { SUPER_ROLE_NAME } from "@/constants/constants";
import { useI18n } from "vue-i18n";

const { currentUserHasPermission } = usePermission();
const { t } = useI18n();

const editingModal = ref<boolean>(false);
const selectedRole = ref<Role>();

const { roles, handleFetchRoles, loading } = useFetchRole();

let fuse: Fuse<Role> | undefined = undefined;

watch(
  () => roles.value,
  (value) => {
    fuse = new Fuse(value, {
      keys: ["spec.displayName", "metadata.name"],
      useExtendedSearch: true,
      threshold: 0.2,
    });
  }
);

const keyword = ref("");

const searchResults = computed(() => {
  if (!fuse || !keyword.value) {
    return roles.value;
  }

  return fuse?.search(keyword.value).map((item) => item.item);
});

const isSystemReserved = (role: Role) => {
  return role.metadata.labels?.[roleLabels.SYSTEM_RESERVED] === "true";
};

const getRoleCountText = (role: Role) => {
  if (role.metadata.name === SUPER_ROLE_NAME) {
    return t("core.role.common.text.contains_all_permissions");
  }

  const dependenciesCount = JSON.parse(
    role.metadata.annotations?.[rbacAnnotations.DEPENDENCIES] || "[]"
  ).length;

  return t("core.role.common.text.contains_n_permissions", {
    count: dependenciesCount,
  });
};

const handleOpenEditingModal = (role: Role) => {
  selectedRole.value = role;
  editingModal.value = true;
};

const onEditingModalClose = () => {
  selectedRole.value = undefined;
  handleFetchRoles();
};

const handleCloneRole = async (role: Role) => {
  const roleToCreate = cloneDeep<Role>(role);
  roleToCreate.metadata.name = "";
  roleToCreate.metadata.generateName = "role-";
  roleToCreate.metadata.creationTimestamp = undefined;

  // 移除系统保留标识
  delete roleToCreate.metadata.labels?.[roleLabels.SYSTEM_RESERVED];

  // 如果是超级管理员角色，那么需要获取到所有角色模板并填充到表单
  if (role.metadata.name === SUPER_ROLE_NAME) {
    const { data } = await apiClient.extension.role.listv1alpha1Role({
      page: 0,
      size: 0,
      labelSelector: [`${roleLabels.TEMPLATE}=true`, "!halo.run/hidden"],
    });
    const roleTemplateNames = data.items.map((item) => item.metadata.name);
    if (roleToCreate.metadata.annotations) {
      roleToCreate.metadata.annotations[rbacAnnotations.DEPENDENCIES] =
        JSON.stringify(roleTemplateNames);
    } else {
      roleToCreate.metadata.annotations = {
        [rbacAnnotations.DEPENDENCIES]: JSON.stringify(roleTemplateNames),
      };
    }
  }

  selectedRole.value = roleToCreate;
  editingModal.value = true;
};

const handleDelete = async (role: Role) => {
  Dialog.warning({
    title: t("core.role.operations.delete.title"),
    description: t("core.role.operations.delete.description"),
    confirmType: "danger",
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    onConfirm: async () => {
      try {
        await apiClient.extension.role.deletev1alpha1Role({
          name: role.metadata.name,
        });

        Toast.success(t("core.common.toast.delete_success"));
      } catch (e) {
        console.error("Failed to delete role", e);
      } finally {
        handleFetchRoles();
      }
    },
  });
};
</script>
<template>
  <RoleEditingModal
    v-model:visible="editingModal"
    :role="selectedRole"
    @close="onEditingModalClose"
  />

  <VPageHeader :title="$t('core.role.title')">
    <template #icon>
      <IconShieldUser class="mr-2 self-center" />
    </template>
    <template #actions>
      <VButton
        v-permission="['system:roles:manage']"
        type="secondary"
        @click="editingModal = true"
      >
        <template #icon>
          <IconAddCircle class="h-full w-full" />
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
              <FormKit
                v-model="keyword"
                :placeholder="$t('core.common.placeholder.search')"
                type="text"
              ></FormKit>
            </div>
          </div>
        </div>
      </template>
      <VLoading v-if="loading" />
      <Transition v-else appear name="fade">
        <ul
          class="box-border h-full w-full divide-y divide-gray-100"
          role="list"
        >
          <li v-for="(role, index) in searchResults" :key="index">
            <VEntity>
              <template #start>
                <VEntityField
                  :title="
                    role.metadata.annotations?.[rbacAnnotations.DISPLAY_NAME] ||
                    role.metadata.name
                  "
                  :description="getRoleCountText(role)"
                  :route="{
                    name: 'RoleDetail',
                    params: {
                      name: role.metadata.name,
                    },
                  }"
                ></VEntityField>
              </template>
              <template #end>
                <!-- TODO: 支持显示用户数量 -->
                <VEntityField v-if="false" description="0 个用户" />
                <VEntityField>
                  <template #description>
                    <VTag>
                      {{
                        isSystemReserved(role)
                          ? t("core.role.common.text.system_reserved")
                          : t("core.role.common.text.custom")
                      }}
                    </VTag>
                  </template>
                </VEntityField>
                <VEntityField v-if="role.metadata.deletionTimestamp">
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
                      {{ formatDatetime(role.metadata.creationTimestamp) }}
                    </span>
                  </template>
                </VEntityField>
              </template>
              <template
                v-if="currentUserHasPermission(['system:roles:manage'])"
                #dropdownItems
              >
                <VDropdownItem
                  v-if="!isSystemReserved(role)"
                  @click="handleOpenEditingModal(role)"
                >
                  {{ $t("core.common.buttons.edit") }}
                </VDropdownItem>
                <VDropdownItem
                  v-if="!isSystemReserved(role)"
                  type="danger"
                  @click="handleDelete(role)"
                >
                  {{ $t("core.common.buttons.delete") }}
                </VDropdownItem>
                <VDropdownItem @click="handleCloneRole(role)">
                  {{
                    $t("core.role.operations.create_based_on_this_role.button")
                  }}
                </VDropdownItem>
              </template>
            </VEntity>
          </li>
        </ul>
      </Transition>
    </VCard>
  </div>
</template>
