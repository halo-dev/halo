<script lang="ts" setup>
import { ref } from "vue";
import IconLogo from "~icons/core/logo?width=5rem&height=2rem";
import { submitForm } from "@formkit/core";
import { Toast, VButton } from "@halo-dev/components";
import { apiClient } from "@/utils/api-client";

const formState = ref({
  password: "",
  user: {
    apiVersion: "v1alpha1",
    kind: "User",
    metadata: {
      name: "",
    },
    spec: {
      avatar: "",
      displayName: "",
      email: "",
    },
  },
});

const handleSignup = async () => {
  await apiClient.themeSide.user.signUp({
    signUpRequest: formState.value,
  });

  Toast.success("注册成功");
};
</script>
<template>
  <div class="flex h-screen flex-col items-center justify-center">
    <IconLogo class="mb-8" />
    <div class="login-form flex w-72 flex-col">
      <FormKit
        id="signup-form"
        name="signup-form"
        :actions="false"
        :classes="{
          form: '!divide-none',
        }"
        type="form"
        :config="{ validationVisibility: 'submit' }"
        @submit="handleSignup"
        @keyup.enter="submitForm('signup-form')"
      >
        <FormKit
          v-model="formState.user.metadata.name"
          name="username"
          placeholder="用户名"
          :autofocus="true"
          type="text"
          validation="required"
        >
        </FormKit>
        <FormKit
          v-model="formState.user.spec.displayName"
          name="displayName"
          placeholder="名称"
          :autofocus="true"
          type="text"
          validation="required"
        >
        </FormKit>
        <FormKit
          v-model="formState.user.spec.email"
          name="displayName"
          placeholder="邮箱"
          :autofocus="true"
          type="text"
          validation="required"
        >
        </FormKit>
        <FormKit
          v-model="formState.password"
          name="password"
          placeholder="密码"
          type="password"
          validation="required|length:0,100"
        >
        </FormKit>
        <FormKit
          name="password_confirm"
          placeholder="确认密码"
          type="password"
          validation="required|confirm|length:0,100"
        >
        </FormKit>
      </FormKit>
      <VButton
        class="mt-6"
        block
        type="secondary"
        @click="submitForm('signup-form')"
      >
        注册
      </VButton>
    </div>
  </div>
</template>
