<script lang="ts" setup>
import SubmitButton from "@/components/button/SubmitButton.vue";
import { rbacAnnotations } from "@/constants/annotations";
import type { User } from "@halo-dev/api-client";
import { consoleApiClient } from "@halo-dev/api-client";
import { Toast, VButton, VModal, VSpace } from "@halo-dev/components";
import { useMutation } from "@tanstack/vue-query";
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";

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

const currentRoles = computed(() => {
  if (!props.user) {
    return [];
  }
  return JSON.parse(
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
</script>

<template>
  <VModal
    ref="modal"
    :title="$t('core.user.grant_permission_modal.title')"
    :width="500"
    @close="emit('close')"
  >
    <FormKit
      id="grant-permission-form"
      name="grant-permission-form"
      :config="{ validationVisibility: 'submit' }"
      type="form"
      @submit="onSubmit"
    >
      <FormKit
        multiple
        name="roles"
        :value="currentRoles"
        :label="$t('core.user.grant_permission_modal.fields.role.label')"
        type="roleSelect"
        :placeholder="
          $t('core.user.grant_permission_modal.fields.role.placeholder')
        "
      ></FormKit>
    </FormKit>
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
