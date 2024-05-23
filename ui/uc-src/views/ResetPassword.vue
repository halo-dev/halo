<script lang="ts" setup>
import { apiClient } from "@/utils/api-client";
import { Toast, VButton } from "@halo-dev/components";
import { useRouteParams, useRouteQuery } from "@vueuse/router";
import { ref } from "vue";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const username = useRouteParams<string>("username");
const token = useRouteQuery<string>("reset_password_token");

interface ResetPasswordForm {
  username: string;
  password: string;
}

const loading = ref(false);

async function onSubmit(data: ResetPasswordForm) {
  try {
    loading.value = true;

    await apiClient.common.user.resetPasswordByToken({
      name: data.username,
      resetPasswordRequest: {
        newPassword: data.password,
        token: token.value,
      },
    });

    Toast.success(t("core.uc_reset_password.operations.reset.toast_success"));

    window.location.href = "/console/login";
  } catch (error) {
    console.error("Failed to reset password", error);
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
        :model-value="username"
        :placeholder="$t('core.uc_reset_password.fields.username.label')"
        :validation-label="$t('core.uc_reset_password.fields.username.label')"
        :autofocus="true"
        type="text"
        disabled
        validation="required"
      ></FormKit>
      <FormKit
        :classes="inputClasses"
        name="password"
        type="password"
        :placeholder="$t('core.uc_reset_password.fields.password.label')"
        :validation-label="$t('core.uc_reset_password.fields.password.label')"
        validation="required:trim|length:5,100|matches:/^\S.*\S$/"
        :validation-messages="{
          matches: $t('core.formkit.validation.trim'),
        }"
      ></FormKit>
      <FormKit
        :classes="inputClasses"
        name="password_confirm"
        type="password"
        :placeholder="
          $t('core.uc_reset_password.fields.password_confirm.label')
        "
        :validation-label="
          $t('core.uc_reset_password.fields.password_confirm.label')
        "
        validation="confirm|required:trim|length:5,100|matches:/^\S.*\S$/"
        :validation-messages="{
          matches: $t('core.formkit.validation.trim'),
        }"
      ></FormKit>
    </FormKit>
    <VButton
      class="mt-8"
      block
      :loading="loading"
      type="secondary"
      @click="$formkit.submit('reset-password-form')"
    >
      {{ $t("core.uc_reset_password.operations.reset.button") }}
    </VButton>
  </div>
</template>
