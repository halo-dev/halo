<script lang="ts" setup>
import { ucApiClient } from "@halo-dev/api-client";
import { Toast, VButton, VModal, VSpace } from "@halo-dev/components";
import { useMutation, useQueryClient } from "@tanstack/vue-query";
import { ref } from "vue";
import { useI18n } from "vue-i18n";
import PasswordValidationForm from "./PasswordValidationForm.vue";

const queryClient = useQueryClient();
const { t } = useI18n();

const emit = defineEmits<{
  (event: "close"): void;
}>();

const modal = ref<InstanceType<typeof VModal> | null>(null);

const { mutate, isLoading } = useMutation({
  mutationKey: ["totp-deletion"],
  mutationFn: async ({ password }: { password: string }) => {
    return await ucApiClient.security.twoFactor.deleteTotp({
      passwordRequest: {
        password: password,
      },
    });
  },
  onSuccess() {
    Toast.success(t("core.common.toast.disable_success"));
    queryClient.invalidateQueries({ queryKey: ["two-factor-settings"] });
    modal.value?.close();
  },
});

function onSubmit(password: string) {
  mutate({ password });
}
</script>

<template>
  <VModal
    ref="modal"
    :width="500"
    :centered="false"
    :title="$t('core.uc_profile.2fa.operations.disable_totp.title')"
    @close="emit('close')"
  >
    <PasswordValidationForm @submit="onSubmit" />
    <template #footer>
      <VSpace>
        <VButton
          :loading="isLoading"
          type="danger"
          @click="$formkit.submit('password-validation-form')"
        >
          {{ $t("core.common.buttons.disable") }}
        </VButton>
        <VButton @click="modal?.close()">
          {{ $t("core.common.buttons.close") }}
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>
