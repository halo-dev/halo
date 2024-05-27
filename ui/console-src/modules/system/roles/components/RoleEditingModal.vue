<script lang="ts" setup>
import { VButton, VModal, VSpace } from "@halo-dev/components";
import SubmitButton from "@/components/button/SubmitButton.vue";
import { onMounted, ref, watch } from "vue";
import { rbacAnnotations } from "@/constants/annotations";
import type { Role } from "@halo-dev/api-client";
import { useRoleForm, useRoleTemplateSelection } from "@/composables/use-role";
import { cloneDeep } from "lodash-es";
import { setFocus } from "@/formkit/utils/focus";
import { pluginLabels, roleLabels } from "@/constants/labels";
import { useI18n } from "vue-i18n";
import { apiClient } from "@/utils/api-client";
import { useQuery } from "@tanstack/vue-query";

const { t } = useI18n();

const props = withDefaults(
  defineProps<{
    role?: Role;
  }>(),
  {
    role: undefined,
  }
);

const emit = defineEmits<{
  (event: "close"): void;
}>();

const modal = ref<InstanceType<typeof VModal> | null>(null);

const { data: roleTemplates } = useQuery({
  queryKey: ["role-templates"],
  queryFn: async () => {
    const { data } = await apiClient.extension.role.listV1alpha1Role({
      page: 0,
      size: 0,
      labelSelector: [`${roleLabels.TEMPLATE}=true`, "!halo.run/hidden"],
    });
    return data.items;
  },
});

const { roleTemplateGroups, handleRoleTemplateSelect, selectedRoleTemplates } =
  useRoleTemplateSelection(roleTemplates);

const { formState, isSubmitting, handleCreateOrUpdate } = useRoleForm();

watch(
  () => selectedRoleTemplates.value,
  (newValue) => {
    if (formState.value.metadata.annotations) {
      formState.value.metadata.annotations[rbacAnnotations.DEPENDENCIES] =
        JSON.stringify(Array.from(newValue));
    }
  }
);

onMounted(() => {
  setFocus("displayNameInput");

  if (props.role) {
    formState.value = cloneDeep(props.role);
    const dependencies =
      props.role.metadata.annotations?.[rbacAnnotations.DEPENDENCIES];
    if (dependencies) {
      selectedRoleTemplates.value = new Set(JSON.parse(dependencies));
    }
  }
});

const editingModalTitle = props.role
  ? t("core.role.editing_modal.titles.update")
  : t("core.role.editing_modal.titles.create");

const handleCreateOrUpdateRole = async () => {
  try {
    await handleCreateOrUpdate();

    modal.value?.close();
  } catch (e) {
    console.error(e);
  }
};
</script>
<template>
  <VModal
    ref="modal"
    :title="editingModalTitle"
    :width="700"
    @close="emit('close')"
  >
    <div>
      <div class="md:grid md:grid-cols-4 md:gap-6">
        <div class="md:col-span-1">
          <div class="sticky top-0">
            <span class="text-base font-medium text-gray-900">
              {{ $t("core.role.editing_modal.groups.general") }}
            </span>
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
              :label="$t('core.role.editing_modal.fields.display_name')"
              type="text"
              validation="required|length:0,50"
            ></FormKit>
            <FormKit
              v-model="
                formState.metadata.annotations[
                  rbacAnnotations.REDIRECT_ON_LOGIN
                ]
              "
              type="text"
              :label="$t('core.role.editing_modal.fields.redirect_on_login')"
            ></FormKit>
            <FormKit
              v-model="
                formState.metadata.annotations[
                  rbacAnnotations.DISALLOW_ACCESS_CONSOLE
                ]
              "
              on-value="true"
              off-value="false"
              type="checkbox"
              :label="
                $t(
                  'core.role.editing_modal.fields.disallow_access_console.label'
                )
              "
              :help="
                $t(
                  'core.role.editing_modal.fields.disallow_access_console.help'
                )
              "
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
            <span class="text-base font-medium text-gray-900">
              {{ $t("core.role.editing_modal.groups.permissions") }}
            </span>
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
                  {{ $t(`core.rbac.${group.module}`, group.module as string) }}
                </div>
                <div
                  v-if="
                    group.roles.length &&
                    group.roles[0].metadata.labels?.[pluginLabels.NAME]
                  "
                  class="mt-3 text-xs text-gray-500"
                >
                  <i18n-t
                    keypath="core.role.common.text.provided_by_plugin"
                    tag="div"
                  >
                    <template #plugin>
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
                        {{
                          group.roles[0].metadata.labels?.[pluginLabels.NAME]
                        }}
                      </RouterLink>
                    </template>
                  </i18n-t>
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
                        type="checkbox"
                        @change="handleRoleTemplateSelect"
                      />
                      <div class="flex flex-1 flex-col gap-y-3">
                        <span class="font-medium text-gray-900">
                          {{
                            $t(
                              `core.rbac.${
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
                          {{
                            $t("core.role.common.text.dependent_on", {
                              roles: JSON.parse(
                                roleTemplate.metadata.annotations?.[
                                  rbacAnnotations.DEPENDENCIES
                                ]
                              )
                                .map((item: string) =>
                                  $t(`core.rbac.${item}`, item as string)
                                )
                                .join("，"),
                            })
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
          :loading="isSubmitting"
          type="secondary"
          :text="$t('core.common.buttons.submit')"
          @submit="$formkit.submit('role-form')"
        >
        </SubmitButton>
        <VButton @click="modal?.close()">
          {{ $t("core.common.buttons.cancel_and_shortcut") }}
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>
