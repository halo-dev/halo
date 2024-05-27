<script lang="ts" setup>
import { apiClient } from "@/utils/api-client";
import { Toast, VButton, VModal, VSpace } from "@halo-dev/components";
import { useMutation, useQueryClient } from "@tanstack/vue-query";
import { ref } from "vue";
import PasswordValidationForm from "./PasswordValidationForm.vue";

const queryClient = useQueryClient();

const emit = defineEmits<{
  (event: "close"): void;
}>();

const modal = ref<InstanceType<typeof VModal> | null>(null);

const { mutate, isLoading } = useMutation({
  mutationKey: ["totp-deletion"],
  mutationFn: async ({ password }: { password: string }) => {
    return await apiClient.twoFactor.deleteTotp({
      passwordRequest: {
        password: password,
      },
    });
  },
  onSuccess() {
    Toast.success("停用成功");
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
    title="停用 TOTP"
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
          停用
        </VButton>
        <VButton @click="modal?.close()">关闭</VButton>
      </VSpace>
    </template>
  </VModal>
</template>
