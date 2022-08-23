<script lang="ts" setup>
import { VButton, VModal, VSpace, VTabItem, VTabs } from "@halo-dev/components";
import { computed, ref, watch, watchEffect } from "vue";
import { rbacAnnotations } from "@/constants/annotations";
import type { Role } from "@halo-dev/api-client";
import {
  useRoleForm,
  useRoleTemplateSelection,
} from "@/modules/system/roles/composables/use-role";
import cloneDeep from "lodash.clonedeep";
import { reset, submitForm } from "@formkit/core";
import { useMagicKeys } from "@vueuse/core";
import { v4 as uuid } from "uuid";

const props = withDefaults(
  defineProps<{
    visible: boolean;
    role: Role | null;
  }>(),
  {
    visible: false,
    role: null,
  }
);

const emit = defineEmits<{
  (event: "update:visible", visible: boolean): void;
  (event: "close"): void;
}>();

const { roleTemplateGroups, handleRoleTemplateSelect, selectedRoleTemplates } =
  useRoleTemplateSelection();

const {
  formState,
  isUpdateMode,
  initialFormState,
  saving,
  handleCreateOrUpdate,
} = useRoleForm();

watch(
  () => selectedRoleTemplates.value,
  (newValue) => {
    if (formState.value.metadata.annotations) {
      formState.value.metadata.annotations[rbacAnnotations.DEPENDENCIES] =
        JSON.stringify(Array.from(newValue));
    }
  }
);

const { Command_Enter } = useMagicKeys();

watchEffect(() => {
  if (Command_Enter.value && props.visible) {
    submitForm("role-form");
  }
});

watch(
  () => props.visible,
  (visible) => {
    if (!visible) {
      handleResetForm();
    }
  }
);

watch(
  () => props.role,
  (role) => {
    if (role) {
      formState.value = cloneDeep(role);
      const dependencies =
        role.metadata.annotations?.[rbacAnnotations.DEPENDENCIES];
      if (dependencies) {
        selectedRoleTemplates.value = new Set(JSON.parse(dependencies));
      }
    } else {
      handleResetForm();
    }
  }
);

const tabActiveId = ref("general");

const editingModalTitle = computed(() => {
  return isUpdateMode.value ? "编辑角色" : "创建角色";
});

const handleCreateOrUpdateRole = async () => {
  try {
    await handleCreateOrUpdate();
    onVisibleChange(false);
  } catch (e) {
    console.error(e);
  }
};

const onVisibleChange = (visible: boolean) => {
  emit("update:visible", visible);
  if (!visible) {
    emit("close");
  }
};

const handleResetForm = () => {
  formState.value = cloneDeep(initialFormState);
  formState.value.metadata.name = uuid();
  reset("role-form");
};
</script>
<template>
  <VModal
    :title="editingModalTitle"
    :visible="visible"
    :width="700"
    @update:visible="onVisibleChange"
  >
    <VTabs v-model:active-id="tabActiveId" type="outline">
      <VTabItem id="general" label="基础信息">
        <FormKit
          v-if="formState.metadata.annotations"
          id="role-form"
          :actions="false"
          type="form"
          @submit="handleCreateOrUpdateRole"
        >
          <FormKit
            v-model="
              formState.metadata.annotations[rbacAnnotations.DISPLAY_NAME]
            "
            label="名称"
            type="text"
            validation="required"
          ></FormKit>
          <FormKit
            v-model="formState.metadata.name"
            help="角色别名，用于区分角色，不能重复，创建之后不能修改"
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
                {{ group.module }}
              </dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
                <ul class="space-y-2">
                  <li v-for="(role, index) in group.roles" :key="index">
                    <label
                      class="inline-flex w-full cursor-pointer flex-row items-center gap-4 rounded-base border p-5 hover:border-primary"
                    >
                      <input
                        v-model="selectedRoleTemplates"
                        :value="role.metadata.name"
                        class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                        type="checkbox"
                        @change="handleRoleTemplateSelect"
                      />
                      <div class="flex flex-1 flex-col gap-y-3">
                        <span class="font-medium text-gray-900">
                          {{
                            role.metadata.annotations?.[
                              rbacAnnotations.DISPLAY_NAME
                            ]
                          }}
                        </span>
                        <span
                          v-if="
                            role.metadata.annotations?.[
                              rbacAnnotations.DEPENDENCIES
                            ]
                          "
                          class="text-xs text-gray-400"
                        >
                          依赖于
                          {{
                            JSON.parse(
                              role.metadata.annotations?.[
                                rbacAnnotations.DEPENDENCIES
                              ]
                            ).join(", ")
                          }}
                        </span>
                      </div>
                    </label>
                  </li>
                </ul>
              </dd>
            </div>
          </dl>
        </div>
      </VTabItem>
    </VTabs>
    <template #footer>
      <VSpace>
        <VButton
          :loading="saving"
          type="secondary"
          @click="$formkit.submit('role-form')"
        >
          提交 ⌘ + ↵
        </VButton>
        <VButton @click="onVisibleChange(false)">取消 Esc</VButton>
      </VSpace>
    </template>
  </VModal>
</template>
