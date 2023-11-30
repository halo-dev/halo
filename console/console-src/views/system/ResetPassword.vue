<script lang="ts" setup>
import { apiClient } from "@/utils/api-client";
import { Toast, VButton } from "@halo-dev/components";
import { ref } from "vue";
import IconLogo from "~icons/core/logo?width=5rem&height=2rem";

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
          placeholder="用户名"
          validation-label="用户名"
          :autofocus="true"
          type="text"
          validation="required"
        ></FormKit>
        <FormKit
          :classes="inputClasses"
          name="email"
          placeholder="邮箱地址"
          validation-label="邮箱地址"
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
        发送验证邮件
      </VButton>
    </div>
  </div>
</template>
