<script lang="ts" setup>
import {
  IconShieldUser,
  IconUserLine,
  VButton,
  Toast,
} from "@halo-dev/components";
import qs from "qs";
import { onBeforeMount, onMounted, ref } from "vue";
import { submitForm } from "@formkit/vue";
import router from "@/router";
import axios from "axios";
import { setFocus } from "@/formkit/utils/focus";
import IconLogo from "~icons/core/logo?width=5rem&height=2rem";
import { randomUUID } from "@/utils/id";
import { useUserStore } from "@/stores/user";

interface LoginForm {
  _csrf: string;
  username: string;
  password: string;
}

const userStore = useUserStore();

const loginForm = ref<LoginForm>({
  _csrf: "",
  username: "",
  password: "",
});

const loading = ref(false);

const handleGenerateToken = async () => {
  const token = randomUUID();
  loginForm.value._csrf = token;
  document.cookie = `XSRF-TOKEN=${token}; Path=/;`;
};

const handleLogin = async () => {
  try {
    loading.value = true;
    await axios.post(
      `${import.meta.env.VITE_API_URL}/login`,
      qs.stringify(loginForm.value),
      {
        withCredentials: true,
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
        },
      }
    );

    await userStore.fetchCurrentUser();

    localStorage.setItem("logged_in", "true");
    router.go(0);
  } catch (e) {
    console.error("Failed to login", e);
    Toast.error("登录失败，用户名或密码错误");
    loginForm.value.password = "";
    setFocus("passwordInput");
  } finally {
    loading.value = false;
  }
};

onBeforeMount(() => {
  if (!userStore.isAnonymous) {
    router.push({ name: "Dashboard" });
  }
});

onMounted(() => {
  handleGenerateToken();
});
</script>
<template>
  <div class="flex h-screen flex-col items-center justify-center">
    <IconLogo class="mb-8" />
    <div class="login-form flex w-72 flex-col gap-4">
      <FormKit
        id="login-form"
        v-model="loginForm"
        name="login-form"
        :actions="false"
        type="form"
        :config="{ validationVisibility: 'submit' }"
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
          id="passwordInput"
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
      <VButton
        block
        :loading="loading"
        type="secondary"
        @click="submitForm('login-form')"
      >
        登录
      </VButton>
    </div>
  </div>
</template>
