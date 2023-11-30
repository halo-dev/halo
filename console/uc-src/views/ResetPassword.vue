<script lang="ts" setup>
import { apiClient } from "@/utils/api-client";
import { Toast, VButton } from "@halo-dev/components";
import { useRouteParams, useRouteQuery } from "@vueuse/router";
import { ref } from "vue";
import IconLogo from "~icons/core/logo?width=5rem&height=2rem";

const username = useRouteParams<string>("username");
const token = useRouteQuery<string>("token");

interface ResetPasswordForm {
  email: string;
  username: string;
}

const loading = ref(false);

async function onSubmit(data: ResetPasswordForm) {
  try {
    loading.value = true;

    await apiClient.common.user.resetPasswordByToken({
      passwordResetEmailRequest: {},
    });

    Toast.success(
      "如果你的用户名和邮箱地址匹配，我们将会发送一封邮件到你的邮箱。"
    );
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
  <div class="flex h-screen flex-col items-center bg-white/90 pt-[30vh]">
    <IconLogo class="mb-8" />
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
          placeholder="用户名"
          validation-label="用户名"
          :autofocus="true"
          type="text"
          disabled
          validation="required"
        ></FormKit>
        <FormKit
          :classes="inputClasses"
          name="password"
          type="password"
          placeholder="新密码"
          validation-label="新密码"
          validation="required:trim|length:5,100|matches:/^\S.*\S$/"
          :validation-messages="{
            matches: $t('core.formkit.validation.trim'),
          }"
        ></FormKit>
        <FormKit
          :classes="inputClasses"
          name="password_confirm"
          type="password"
          placeholder="确认密码"
          validation-label="确认密码"
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
        重置密码
      </VButton>
    </div>
  </div>
</template>
