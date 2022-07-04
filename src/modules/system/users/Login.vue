<script lang="ts" setup>
import { VButton, VInput, VSpace } from "@halo-dev/components";
import { v4 as uuid } from "uuid";
import { axiosInstance } from "@halo-dev/admin-shared";
import qs from "qs";
import logo from "../../../assets/logo.svg";
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";

interface LoginForm {
  _csrf: string;
  username: string;
  password: string;
}

interface LoginFormState {
  logging: boolean;
  state: LoginForm;
}

const router = useRouter();

const loginForm = ref<LoginFormState>({
  logging: false,
  state: {
    _csrf: "",
    username: "admin",
    password: "{no}123456",
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

    await axiosInstance.post(
      `http://localhost:8090/login`,
      qs.stringify(loginForm.value.state),
      {
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
        },
      }
    );
  } catch (e) {
    console.error(e);
  } finally {
    await router.replace({ name: "Dashboard" });
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
      <VSpace class="w-full" direction="column" spacing="lg">
        <VInput
          v-model="loginForm.state.username"
          placeholder="用户名"
        ></VInput>
        <VInput v-model="loginForm.state.password" placeholder="密码"></VInput>
        <VButton block type="secondary" @click="handleLogin">登录</VButton>
      </VSpace>
    </div>
  </div>
</template>
