<script lang="ts" setup>
import {
  IconShieldUser,
  IconUserSettings,
  VButton,
} from "@halo-dev/components";
import { v4 as uuid } from "uuid";
import qs from "qs";
import logo from "../../../assets/logo.svg";
import { onMounted, ref } from "vue";
import { submitForm } from "@formkit/vue";
import router from "@/router";

interface LoginForm {
  _csrf: string;
  username: string;
  password: string;
}

interface LoginFormState {
  logging: boolean;
  state: LoginForm;
}

const loginForm = ref<LoginFormState>({
  logging: false,
  state: {
    _csrf: "",
    username: "ryanwang",
    password: "12345678",
  },
});

const handleGenerateToken = async () => {
  const token = uuid();
  loginForm.value.state._csrf = token;
  document.cookie = `XSRF-TOKEN=${token}; Path=/;`;
};

const handleLogin = async () => {
  try {
    loginForm.value.logging = true;

    await fetch(`${import.meta.env.VITE_API_URL}/login`, {
      method: "POST",
      headers: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      credentials: "include",
      redirect: "manual",
      body: qs.stringify(loginForm.value.state),
    });
    await router.push({ name: "Dashboard" });
    await router.go(0);
  } catch (e) {
    console.error(e);
  } finally {
    loginForm.value.logging = false;
  }
};

onMounted(() => {
  handleGenerateToken();
});
</script>
<template>
  <div class="flex h-screen flex-col items-center justify-center">
    <img :src="logo" alt="Logo" class="mb-8 w-20" />
    <div class="login-form w-72">
      <FormKit
        id="login"
        v-model="loginForm.state"
        :actions="false"
        type="form"
        @submit="handleLogin"
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
            <IconUserSettings />
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
        <VButton block type="secondary" @click="submitForm('login')">
          登录
        </VButton>
      </FormKit>
    </div>
  </div>
</template>
