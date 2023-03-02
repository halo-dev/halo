<script lang="ts" setup>
import { onBeforeMount } from "vue";
import router from "@/router";
import IconLogo from "~icons/core/logo?width=5rem&height=2rem";
import { useUserStore } from "@/stores/user";
import LoginForm from "@/components/login/LoginForm.vue";

const userStore = useUserStore();

onBeforeMount(() => {
  if (!userStore.isAnonymous) {
    router.push({ name: "Dashboard" });
  }
});

function onLoginSucceed() {
  window.location.reload();
}
</script>
<template>
  <div class="flex h-screen flex-col items-center justify-center">
    <IconLogo class="mb-8" />
    <div class="login-form flex w-72 flex-col">
      <LoginForm @succeed="onLoginSucceed" />
    </div>
  </div>
</template>
