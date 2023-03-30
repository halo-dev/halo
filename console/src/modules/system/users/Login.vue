<script lang="ts" setup>
import { onBeforeMount, computed } from "vue";
import router from "@/router";
import IconLogo from "~icons/core/logo?width=5rem&height=2rem";
import { useUserStore } from "@/stores/user";
import LoginForm from "@/components/login/LoginForm.vue";
import { useRouteQuery } from "@vueuse/router";
import SignupForm from "@/components/signup/SignupForm.vue";
import SocialAuthProviders from "@/components/login/SocialAuthProviders.vue";
import { useGlobalInfoFetch } from "@/composables/use-global-info";

const userStore = useUserStore();
const { globalInfo } = useGlobalInfoFetch();

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

const isLoginType = computed(() => type.value !== "signup");
</script>
<template>
  <div class="flex h-screen flex-col items-center bg-white/90 pt-[30vh]">
    <IconLogo class="mb-8" />
    <div class="flex w-72 flex-col">
      <SignupForm v-if="type === 'signup'" @succeed="onLoginSucceed" />
      <LoginForm v-else @succeed="onLoginSucceed" />
      <SocialAuthProviders />
      <div
        v-if="globalInfo?.allowRegistration"
        class="flex justify-center gap-1 pt-3.5 text-xs"
      >
        <span class="text-slate-500">
          {{
            isLoginType
              ? $t("core.login.operations.signup.label")
              : $t("core.login.operations.return_login.label")
          }}
        </span>
        <span
          class="cursor-pointer text-secondary hover:text-gray-600"
          @click="handleChangeType"
        >
          {{
            isLoginType
              ? $t("core.login.operations.signup.button")
              : $t("core.login.operations.return_login.button")
          }}
        </span>
      </div>
    </div>
  </div>
</template>
