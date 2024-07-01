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
  mutationKey: ["enable-two-factor"],
  mutationFn: async ({ password }: { password: string }) => {
    return await ucApiClient.security.twoFactor.enableTwoFactor({
      passwordRequest: {
        password: password,
      },
    });
  },
  onSuccess() {
    Toast.success(t("core.common.toast.enable_success"));
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
    :title="$t('core.uc_profile.2fa.operations.enable.title')"
    @close="emit('close')"
  >
    <PasswordValidationForm @submit="onSubmit" />
    <template #footer>
      <VSpace>
        <VButton
          :loading="isLoading"
          type="secondary"
          @click="$formkit.submit('password-validation-form')"
        >
          {{ $t("core.common.buttons.enable") }}
        </VButton>
        <VButton @click="modal?.close()">
          {{ $t("core.common.buttons.close") }}
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>
