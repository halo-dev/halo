<script lang="ts" setup>
import { VButton, VModal, VTabItem, VTabs } from "@halo-dev/components";
import { computed, ref, watch } from "vue";
import type { Role } from "@/types/extension";
import { axiosInstance } from "@halo-dev/admin-shared";

interface RoleTemplateGroup {
  name: string | null | undefined;
  roles: Role[];
}

interface CreationFormState {
  role: Role;
  selectedRoleTemplates: string[];
  saving: boolean;
}

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(["update:visible", "close"]);

const creationActiveId = ref("general");
const roles = ref<Role[]>([]);
const creationFormState = ref<CreationFormState>({
  role: {
    apiVersion: "v1alpha1",
    kind: "Role",
    metadata: {
      name: "",
      labels: {},
      annotations: {
        "plugin.halo.run/dependencies": "",
        "plugin.halo.run/display-name": "",
      }!,
    },
    rules: [],
  },
  selectedRoleTemplates: [],
  saving: false,
});

const roleTemplates = computed<Role[]>(() => {
  return roles.value.filter(
    (role) => role.metadata.labels?.["plugin.halo.run/role-template"] === "true"
  );
});

const roleTemplateGroups = computed<RoleTemplateGroup[]>(() => {
  const groups: RoleTemplateGroup[] = [];
  roleTemplates.value.forEach((role) => {
    const group = groups.find(
      (group) =>
        group.name === role.metadata.annotations?.["plugin.halo.run/module"]
    );
    if (group) {
      group.roles.push(role);
    } else {
      groups.push({
        name: role.metadata.annotations?.["plugin.halo.run/module"],
        roles: [role],
      });
    }
  });
  return groups;
});

const handleFetchRoles = async () => {
  try {
    const { data } = await axiosInstance.get("/api/v1alpha1/roles");
    roles.value = data;
  } catch (e) {
    console.error(e);
  }
};

const handleCreateRole = async () => {
  try {
    creationFormState.value.saving = true;
    if (creationFormState.value.role.metadata.annotations) {
      creationFormState.value.role.metadata.annotations[
        "plugin.halo.run/dependencies"
      ] = JSON.stringify(creationFormState.value.selectedRoleTemplates);
    }
    await axiosInstance.post<Role>(
      "/api/v1alpha1/roles",
      creationFormState.value.role
    );
    handleVisibleChange(false);
  } catch (e) {
    console.error(e);
  } finally {
    creationFormState.value.saving = false;
  }
};

const handleVisibleChange = (visible: boolean) => {
  emit("update:visible", visible);
  if (!visible) {
    emit("close");
  }
};

watch(
  () => props.visible,
  (visible) => {
    if (visible) {
      handleFetchRoles();
    }
  }
);
</script>
<template>
  <VModal
    :visible="visible"
    :width="650"
    title="创建角色"
    @update:visible="handleVisibleChange"
  >
    <VTabs v-model:active-id="creationActiveId" type="outline">
      <VTabItem id="general" label="基础信息">
        <FormKit
          v-if="creationFormState.role.metadata.annotations"
          id="role-form"
          :actions="false"
          type="form"
          @submit="handleCreateRole"
        >
          <FormKit
            v-model="
              creationFormState.role.metadata.annotations[
                'plugin.halo.run/display-name'
              ]
            "
            label="名称"
            type="text"
            validation="required"
          ></FormKit>
          <FormKit
            v-model="creationFormState.role.metadata.name"
            label="别名"
            type="text"
            validation="required"
          ></FormKit>
        </FormKit>
      </VTabItem>
      <VTabItem id="permissions" label="权限">
        <div>
          <dl class="divide-y divide-gray-100">
            <div
              v-for="(group, groupIndex) in roleTemplateGroups"
              :key="groupIndex"
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">
                {{ group.name }}
              </dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
                <ul class="space-y-2">
                  <li v-for="(role, index) in group.roles" :key="index">
                    <div
                      class="inline-flex w-72 cursor-pointer flex-row items-center gap-4 rounded border p-5 hover:border-themeable-primary"
                    >
                      <input
                        v-model="creationFormState.selectedRoleTemplates"
                        :value="role.metadata.name"
                        class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                        type="checkbox"
                      />
                      <div class="inline-flex flex-col gap-y-3">
                        <span class="font-medium text-gray-900">
                          {{
                            role.metadata.annotations?.[
                              "plugin.halo.run/display-name"
                            ]
                          }}
                        </span>
                        <span
                          v-if="
                            role.metadata.annotations?.[
                              'plugin.halo.run/dependencies'
                            ]
                          "
                          class="text-xs text-gray-400"
                        >
                          依赖于
                          {{
                            JSON.parse(
                              role.metadata.annotations?.[
                                "plugin.halo.run/dependencies"
                              ]
                            ).join(", ")
                          }}
                        </span>
                      </div>
                    </div>
                  </li>
                </ul>
              </dd>
            </div>
          </dl>
        </div>
      </VTabItem>
    </VTabs>
    <template #footer>
      <VButton
        :loading="creationFormState.saving"
        type="secondary"
        @click="$formkit.submit('role-form')"
        >创建
      </VButton>
    </template>
  </VModal>
</template>
