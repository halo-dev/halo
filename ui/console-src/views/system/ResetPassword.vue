<script lang="ts" setup>
import { apiClient } from "@/utils/api-client";
import { Toast, VButton } from "@halo-dev/components";
import { ref } from "vue";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

interface ResetPasswordForm {
  email: string;
  username: string;
}

const loading = ref(false);

async function onSubmit(data: ResetPasswordForm) {
  try {
    loading.value = true;
    await apiClient.common.user.sendPasswordResetEmail({
      passwordResetEmailRequest: {
        email: data.email,
        username: data.username,
      },
    });

    Toast.success(t("core.reset_password.operations.send.toast_success"));
  } catch (error) {
    console.error("Failed to send password reset email", error);
  } finally {
    loading.value = false;
  }
}

const inputClasses = {
  outer: "!py-3 first:!pt-0 last:!pb-0",
};
</script>

<template>
  <div class="flex w-72 flex-col">
    <FormKit
      id="reset-password-form"
      name="reset-password-form"
      type="form"
      :classes="{
        form: '!divide-none',
      }"
      :config="{ validationVisibility: 'submit' }"
      @submit="onSubmit"
      @keyup.enter="$formkit.submit('reset-password-form')"
    >
      <FormKit
        :classes="inputClasses"
        name="username"
        :placeholder="$t('core.reset_password.fields.username.label')"
        :validation-label="$t('core.reset_password.fields.username.label')"
        :autofocus="true"
        type="text"
        validation="required"
      ></FormKit>
      <FormKit
        :classes="inputClasses"
        name="email"
        :placeholder="$t('core.reset_password.fields.email.label')"
        :validation-label="$t('core.reset_password.fields.email.label')"
        :autofocus="true"
        type="text"
        validation="required"
      ></FormKit>
    </FormKit>
    <VButton
      class="mt-8"
      block
      :loading="loading"
      type="secondary"
      @click="$formkit.submit('reset-password-form')"
    >
      {{ $t("core.reset_password.operations.send.label") }}
    </VButton>
  </div>
</template>
