<script lang="ts" setup>
import { VButton, VModal, VSpace } from "@halo-dev/components";
import SubmitButton from "@/components/button/SubmitButton.vue";
import { computed, watch } from "vue";
import { rbacAnnotations } from "@/constants/annotations";
import type { Role } from "@halo-dev/api-client";
import {
  useRoleForm,
  useRoleTemplateSelection,
} from "@/modules/system/roles/composables/use-role";
import cloneDeep from "lodash.clonedeep";
import { reset } from "@formkit/core";
import { setFocus } from "@/formkit/utils/focus";
import { pluginLabels } from "@/constants/labels";

const props = withDefaults(
  defineProps<{
    visible: boolean;
    role?: Role;
  }>(),
  {
    visible: false,
    role: undefined,
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

watch(
  () => props.visible,
  (visible) => {
    if (visible) {
      setFocus("displayNameInput");
    } else {
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
  selectedRoleTemplates.value.clear();
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
    <div>
      <div class="md:grid md:grid-cols-4 md:gap-6">
        <div class="md:col-span-1">
          <div class="sticky top-0">
            <span class="text-base font-medium text-gray-900"> 基本信息 </span>
          </div>
        </div>
        <div class="mt-5 divide-y divide-gray-100 md:col-span-3 md:mt-0">
          <FormKit
            v-if="formState.metadata.annotations"
            id="role-form"
            name="role-form"
            :actions="false"
            type="form"
            :config="{ validationVisibility: 'submit' }"
            @submit="handleCreateOrUpdateRole"
          >
            <FormKit
              id="displayNameInput"
              v-model="
                formState.metadata.annotations[rbacAnnotations.DISPLAY_NAME]
              "
              label="名称"
              type="text"
              validation="required|length:0,50"
            ></FormKit>
          </FormKit>
        </div>
      </div>
      <div class="py-5">
        <div class="border-t border-gray-200"></div>
      </div>
      <div class="md:grid md:grid-cols-4 md:gap-6">
        <div class="md:col-span-1">
          <div class="sticky top-0">
            <span class="text-base font-medium text-gray-900"> 权限 </span>
          </div>
        </div>
        <div class="mt-5 divide-y divide-gray-100 md:col-span-3 md:mt-0">
          <dl class="divide-y divide-gray-100">
            <div
              v-for="(group, groupIndex) in roleTemplateGroups"
              :key="groupIndex"
              class="flex flex-col gap-3 bg-white py-5 first:pt-0"
            >
              <dt class="text-sm font-medium text-gray-900">
                <div>
                  {{ $t(`rbac.${group.module}`, group.module as string) }}
                </div>
                <div
                  v-if="
                    group.roles.length &&
                    group.roles[0].metadata.labels?.[pluginLabels.NAME]
                  "
                  class="mt-3 text-xs text-gray-500"
                >
                  由
                  <RouterLink
                    :to="{
                      name: 'PluginDetail',
                      params: {
                        name: group.roles[0].metadata.labels?.[
                          pluginLabels.NAME
                        ],
                      },
                    }"
                    class="hover:text-blue-600"
                  >
                    {{ group.roles[0].metadata.labels?.[pluginLabels.NAME] }}
                  </RouterLink>
                  插件提供
                </div>
              </dt>
              <dd class="text-sm text-gray-900">
                <ul class="space-y-2">
                  <li v-for="(roleTemplate, index) in group.roles" :key="index">
                    <label
                      class="inline-flex w-full cursor-pointer flex-row items-center gap-4 rounded-base border p-5 hover:border-primary"
                    >
                      <input
                        v-model="selectedRoleTemplates"
                        :value="roleTemplate.metadata.name"
                        class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                        type="checkbox"
                        @change="handleRoleTemplateSelect"
                      />
                      <div class="flex flex-1 flex-col gap-y-3">
                        <span class="font-medium text-gray-900">
                          {{
                            $t(
                              `rbac.${
                                roleTemplate.metadata.annotations?.[
                                  rbacAnnotations.DISPLAY_NAME
                                ]
                              }`,
                              roleTemplate.metadata.annotations?.[
                                rbacAnnotations.DISPLAY_NAME
                              ] as string
                            )
                          }}
                        </span>
                        <span
                          v-if="
                            roleTemplate.metadata.annotations?.[
                              rbacAnnotations.DEPENDENCIES
                            ]
                          "
                          class="text-xs text-gray-400"
                        >
                          依赖于
                          {{
                            JSON.parse(
                              roleTemplate.metadata.annotations?.[
                                rbacAnnotations.DEPENDENCIES
                              ]
                            )
                              .map((item: string) =>
                                $t(`rbac.${item}`, item as string)
                              )
                              .join("，")
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
      </div>
    </div>
    <template #footer>
      <VSpace>
        <SubmitButton
          v-if="visible"
          :loading="saving"
          type="secondary"
          @submit="$formkit.submit('role-form')"
        >
        </SubmitButton>
        <VButton @click="onVisibleChange(false)">取消 Esc</VButton>
      </VSpace>
    </template>
  </VModal>
</template>
