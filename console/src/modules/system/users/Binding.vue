<script lang="ts" setup>
import { onBeforeMount } from "vue";
import router from "@/router";
import IconLogo from "~icons/core/logo?width=5rem&height=2rem";
import { useUserStore } from "@/stores/user";
import LoginForm from "@/components/login/LoginForm.vue";
import { useRoute } from "vue-router";
import type { GlobalInfo, SocialAuthProvider } from "../actuator/types";
import { useQuery } from "@tanstack/vue-query";
import axios from "axios";
import { Toast } from "@halo-dev/components";
import { useRouteQuery } from "@vueuse/router";
import SignupForm from "@/components/signup/SignupForm.vue";

const userStore = useUserStore();
const route = useRoute();

onBeforeMount(() => {
  if (!userStore.isAnonymous) {
    router.push({ name: "Dashboard" });
  }
});

const { data: socialAuthProviders } = useQuery<SocialAuthProvider[]>({
  queryKey: ["social-auth-providers"],
  queryFn: async () => {
    const { data } = await axios.get<GlobalInfo>(
      `${import.meta.env.VITE_API_URL}/actuator/globalinfo`,
      {
        withCredentials: true,
      }
    );

    return data.socialAuthProviders;
  },
  refetchOnWindowFocus: false,
});

function handleBinding() {
  const authProvider = socialAuthProviders.value?.find(
    (p) => p.name === route.params.provider
  );

  if (!authProvider?.bindingUrl) {
    Toast.error("绑定失败，没有找到已启用的登录方式");
    return;
  }

  window.location.href = authProvider?.bindingUrl;

  Toast.success("绑定成功");
}

const type = useRouteQuery<string>("type", "");

function handleChangeType() {
  type.value = type.value === "signup" ? "" : "signup";
}
</script>
<template>
  <div class="flex h-screen flex-col items-center justify-center">
    <div class="mb-4">
      <IconLogo />
    </div>
    <div class="login-form flex w-72 flex-col">
      <div class="mb-4 flex">
        <h1 class="text-sm">{{ $t("core.binding.title") }}</h1>
      </div>
      <SignupForm v-if="type === 'signup'" @succeed="handleBinding" />
      <LoginForm v-else @succeed="handleBinding" />
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
