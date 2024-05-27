<script lang="ts" setup>
import SubmitButton from "@/components/button/SubmitButton.vue";
import { patAnnotations, rbacAnnotations } from "@/constants/annotations";
import { apiClient } from "@/utils/api-client";
import { toISOString } from "@/utils/date";
import { Dialog, Toast, VButton, VModal, VSpace } from "@halo-dev/components";
import { useMutation, useQueryClient } from "@tanstack/vue-query";
import { useClipboard } from "@vueuse/core";
import type { PatSpec, PersonalAccessToken } from "@halo-dev/api-client";
import { computed, ref } from "vue";
import { useRoleTemplateSelection } from "@/composables/use-role";
import { useRoleStore } from "@/stores/role";
import { useI18n } from "vue-i18n";
import { roleLabels } from "@/constants/labels";

const queryClient = useQueryClient();
const { t } = useI18n();

const emit = defineEmits<{
  (event: "close"): void;
}>();

const modal = ref<InstanceType<typeof VModal> | null>(null);

const formState = ref<
  Omit<PersonalAccessToken, "spec"> & {
    spec: PatSpec;
  }
>({
  kind: "PersonalAccessToken",
  apiVersion: "security.halo.run/v1alpha1",
  metadata: {
    generateName: "pat-",
    name: "",
  },
  spec: {
    description: "",
    expiresAt: "",
    name: "",
    roles: [],
    tokenId: "",
    username: "",
  },
});

const { permissions } = useRoleStore();

const availableRoleTemplates = computed(() => {
  return permissions.permissions.filter((role) => {
    return (
      role.metadata.labels?.[roleLabels.TEMPLATE] === "true" &&
      role.metadata.labels?.[roleLabels.HIDDEN] !== "true"
    );
  });
});

const { roleTemplateGroups, handleRoleTemplateSelect, selectedRoleTemplates } =
  useRoleTemplateSelection(availableRoleTemplates);

const { copy } = useClipboard({
  legacy: true,
});

const { mutate, isLoading } = useMutation({
  mutationKey: ["pat-creation"],
  mutationFn: async () => {
    if (formState.value.spec?.expiresAt) {
      formState.value.spec.expiresAt = toISOString(
        formState.value.spec.expiresAt
      );
    }
    formState.value.spec = {
      ...formState.value.spec,
      roles: Array.from(selectedRoleTemplates.value),
    };
    const { data } = await apiClient.pat.generatePat({
      personalAccessToken: formState.value,
    });
    return data;
  },
  onSuccess(data) {
    queryClient.invalidateQueries({ queryKey: ["personal-access-tokens"] });
    emit("close");

    const token = data.metadata.annotations?.[patAnnotations.ACCESS_TOKEN];

    setTimeout(() => {
      Dialog.info({
        title: t("core.uc_profile.pat.operations.copy.title"),
        description: token,
        confirmType: "secondary",
        confirmText: t("core.common.buttons.copy"),
        showCancel: false,
        onConfirm: () => {
          copy(token || "");
          Toast.success(t("core.common.toast.copy_success"));
        },
      });
    });
  },
});
</script>

<template>
  <VModal
    ref="modal"
    :width="700"
    :title="$t('core.uc_profile.pat.creation_modal.title')"
    @close="emit('close')"
  >
    <div>
      <div class="md:grid md:grid-cols-4 md:gap-6">
        <div class="md:col-span-1">
          <div class="sticky top-0">
            <span class="text-base font-medium text-gray-900">
              {{ $t("core.uc_profile.pat.creation_modal.groups.general") }}
            </span>
          </div>
        </div>
        <div class="mt-5 divide-y divide-gray-100 md:col-span-3 md:mt-0">
          <FormKit
            id="pat-creation-form"
            v-model="formState.spec"
            type="form"
            name="pat-creation-form"
            @submit="mutate()"
          >
            <FormKit
              validation="required"
              type="text"
              name="name"
              :label="
                $t('core.uc_profile.pat.creation_modal.fields.name.label')
              "
            ></FormKit>
            <FormKit
              type="datetime-local"
              name="expiresAt"
              :label="
                $t('core.uc_profile.pat.creation_modal.fields.expiresAt.label')
              "
              :help="
                $t('core.uc_profile.pat.creation_modal.fields.expiresAt.help')
              "
            ></FormKit>
            <FormKit
              type="textarea"
              name="description"
              :label="
                $t(
                  'core.uc_profile.pat.creation_modal.fields.description.label'
                )
              "
            ></FormKit>
          </FormKit>
        </div>
      </div>
      <div v-if="roleTemplateGroups.length" class="py-5">
        <div class="border-t border-gray-200"></div>
      </div>
      <div
        v-if="roleTemplateGroups.length"
        class="md:grid md:grid-cols-4 md:gap-6"
      >
        <div class="md:col-span-1">
          <div class="sticky top-0">
            <span class="text-base font-medium text-gray-900">
              {{ $t("core.uc_profile.pat.creation_modal.groups.permissions") }}
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
          :loading="isLoading"
          type="secondary"
          :text="$t('core.common.buttons.submit')"
          @submit="$formkit.submit('pat-creation-form')"
        />
        <VButton @click="modal?.close()">
          {{ $t("core.common.buttons.cancel_and_shortcut") }}
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>
