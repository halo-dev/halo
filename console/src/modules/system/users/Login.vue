<script lang="ts" setup>
import { onBeforeMount } from "vue";
import router from "@/router";
import IconLogo from "~icons/core/logo?width=5rem&height=2rem";
import { useUserStore } from "@/stores/user";
import LoginForm from "@/components/login/LoginForm.vue";
import { useRouteQuery } from "@vueuse/router";
import SignupForm from "@/components/signup/SignupForm.vue";

const userStore = useUserStore();

onBeforeMount(() => {
  if (!userStore.isAnonymous) {
    router.push({ name: "Dashboard" });
  }
});

function onLoginSucceed() {
  window.location.reload();
}

const type = useRouteQuery<string>("type", "");

function handleChangeType() {
  type.value = type.value === "signup" ? "" : "signup";
}
</script>
<template>
  <div class="flex h-screen flex-col items-center justify-center">
    <IconLogo class="mb-8" />
    <div class="login-form flex w-72 flex-col">
      <SignupForm v-if="type === 'signup'" @succeed="onLoginSucceed" />
      <LoginForm v-else @succeed="onLoginSucceed" />
      <div class="flex">
        <span class="mt-4 text-sm text-indigo-600" @click="handleChangeType">
          {{
            type === "signup" ? $t("core.login.title") : $t("core.signup.title")
          }}
        </span>
      </div>
    </div>
  </div>
</template>
