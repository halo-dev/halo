<script lang="ts" setup>
import { IconShieldUser, IconUserLine, VButton } from "@halo-dev/components";
import { v4 as uuid } from "uuid";
import qs from "qs";
import logo from "@/assets/logo.svg";
import { onMounted, ref } from "vue";
import { submitForm } from "@formkit/vue";
import router from "@/router";

interface LoginForm {
  _csrf: string;
  username: string;
  password: string;
}

const loginForm = ref<LoginForm>({
  _csrf: "",
  username: "",
  password: "",
});
const loading = ref(false);

const handleGenerateToken = async () => {
  const token = uuid();
  loginForm.value._csrf = token;
  document.cookie = `XSRF-TOKEN=${token}; Path=/;`;
};

const handleLogin = async () => {
  try {
    loading.value = true;
    await fetch(`${import.meta.env.VITE_API_URL}/login`, {
      method: "POST",
      headers: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      credentials: "include",
      redirect: "manual",
      body: qs.stringify(loginForm.value),
    });
    await router.push({ name: "Dashboard" });
    await router.go(0);
  } catch (e) {
    console.error("Failed to login", e);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  handleGenerateToken();
});
</script>
<template>
  <div class="flex h-screen flex-col items-center justify-center">
    <img :src="logo" alt="Logo" class="mb-8 w-20" />
    <div class="login-form flex w-72 flex-col gap-4">
      <FormKit
        id="login-form"
        v-model="loginForm"
        :actions="false"
        type="form"
        :config="{ animation: 'none' }"
        @submit="handleLogin"
        @keyup.enter="submitForm('login-form')"
      >
        <FormKit
          :validation-messages="{
            required: '请输入用户名',
          }"
          name="username"
          placeholder="用户名"
          type="text"
          validation="required"
        >
          <template #prefixIcon>
            <IconUserLine />
          </template>
        </FormKit>
        <FormKit
          :validation-messages="{
            required: '请输入密码',
          }"
          name="password"
          placeholder="密码"
          type="password"
          validation="required"
        >
          <template #prefixIcon>
            <IconShieldUser />
          </template>
        </FormKit>
      </FormKit>
      <VButton block type="secondary" @click="submitForm('login-form')">
        登录
      </VButton>
    </div>
  </div>
</template>
