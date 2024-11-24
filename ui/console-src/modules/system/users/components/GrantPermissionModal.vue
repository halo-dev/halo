<script lang="ts" setup>
import SubmitButton from "@/components/button/SubmitButton.vue";
import { rbacAnnotations } from "@/constants/annotations";
import { SUPER_ROLE_NAME } from "@/constants/constants";
import { roleLabels } from "@/constants/labels";
import type { User } from "@halo-dev/api-client";
import { consoleApiClient, coreApiClient } from "@halo-dev/api-client";
import { Toast, VButton, VModal, VSpace } from "@halo-dev/components";
import { useMutation, useQuery } from "@tanstack/vue-query";
import { computed, onMounted, ref } from "vue";
import { useI18n } from "vue-i18n";
import RolesView from "./RolesView.vue";

const { t } = useI18n();

const props = withDefaults(
  defineProps<{
    user?: User;
  }>(),
  {
    user: undefined,
  }
);

const emit = defineEmits<{
  (event: "close"): void;
}>();

const modal = ref<InstanceType<typeof VModal> | null>(null);

const selectedRoleNames = ref<string[]>([]);

onMounted(() => {
  if (!props.user) {
    return;
  }
  selectedRoleNames.value = JSON.parse(
    props.user.metadata.annotations?.[rbacAnnotations.ROLE_NAMES] || "[]"
  );
});

const { mutate, isLoading } = useMutation({
  mutationKey: ["core:user:grant-permissions"],
  mutationFn: async ({ roles }: { roles: string[] }) => {
    return await consoleApiClient.user.grantPermission({
      name: props.user?.metadata.name as string,
      grantRequest: {
        roles: roles,
      },
    });
  },
  onSuccess() {
    Toast.success(t("core.common.toast.operation_success"));
    modal.value?.close();
  },
});

function onSubmit(data: { roles: string[] }) {
  mutate({ roles: data.roles });
}

const { data: allRoles } = useQuery({
  queryKey: ["core:roles"],
  queryFn: async () => {
    const { data } = await coreApiClient.role.listRole({
      page: 0,
      size: 0,
      labelSelector: [`!${roleLabels.TEMPLATE}`],
    });
    return data;
  },
});

const { data: allRoleTemplates } = useQuery({
  queryKey: ["core:role-templates"],
  queryFn: async () => {
    const { data } = await coreApiClient.role.listRole({
      page: 0,
      size: 0,
      labelSelector: [`${roleLabels.TEMPLATE}=true`, "!halo.run/hidden"],
    });
    return data.items;
  },
});

const currentRoleTemplates = computed(() => {
  if (!selectedRoleNames.value.length) {
    return [];
  }

  const selectedRoles = allRoles.value?.items.filter((role) =>
    selectedRoleNames.value.includes(role.metadata.name)
  );

  let allDependsRoleTemplates: string[] = [];

  selectedRoles?.forEach((role) => {
    allDependsRoleTemplates = allDependsRoleTemplates.concat(
      JSON.parse(
        role.metadata.annotations?.[rbacAnnotations.DEPENDENCIES] || "[]"
      )
    );
  });

  return allRoleTemplates.value?.filter((item) => {
    return allDependsRoleTemplates.includes(item.metadata.name);
  });
});
</script>

<template>
  <VModal
    ref="modal"
    :title="$t('core.user.grant_permission_modal.title')"
    :width="600"
    :centered="false"
    @close="emit('close')"
  >
    <div>
      <FormKit
        id="grant-permission-form"
        name="grant-permission-form"
        :config="{ validationVisibility: 'submit' }"
        type="form"
        @submit="onSubmit"
      >
        <!-- @vue-ignore -->
        <FormKit
          v-model="selectedRoleNames"
          multiple
          name="roles"
          :label="$t('core.user.grant_permission_modal.fields.role.label')"
          type="roleSelect"
          :placeholder="
            $t('core.user.grant_permission_modal.fields.role.placeholder')
          "
        ></FormKit>
      </FormKit>

      <div v-if="selectedRoleNames.length">
        <div
          v-if="selectedRoleNames.includes(SUPER_ROLE_NAME)"
          class="text-sm text-gray-600 mt-4"
        >
          {{ $t("core.user.grant_permission_modal.roles_preview.all") }}
        </div>

        <div v-else-if="currentRoleTemplates?.length" class="space-y-3 mt-4">
          <span class="text-sm text-gray-600">
            {{ $t("core.user.grant_permission_modal.roles_preview.includes") }}
          </span>
          <RolesView :role-templates="currentRoleTemplates" />
        </div>
      </div>
    </div>

    <template #footer>
      <VSpace>
        <SubmitButton
          :loading="isLoading"
          type="secondary"
          :text="$t('core.common.buttons.submit')"
          @submit="$formkit.submit('grant-permission-form')"
        >
        </SubmitButton>
        <VButton @click="modal?.close()">
          {{ $t("core.common.buttons.cancel_and_shortcut") }}
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>
