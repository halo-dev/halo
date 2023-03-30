<script lang="ts" setup>
import { computed, onBeforeMount, onMounted } from "vue";
import router from "@/router";
import IconLogo from "~icons/core/logo?width=5rem&height=2rem";
import { useUserStore } from "@/stores/user";
import LoginForm from "@/components/login/LoginForm.vue";
import { useRoute } from "vue-router";
import { Toast } from "@halo-dev/components";
import { useRouteQuery } from "@vueuse/router";
import SignupForm from "@/components/signup/SignupForm.vue";
import { useGlobalInfoFetch } from "@/composables/use-global-info";
import { useI18n } from "vue-i18n";

const userStore = useUserStore();
const route = useRoute();
const { t } = useI18n();

onBeforeMount(() => {
  if (!userStore.isAnonymous) {
    router.push({ name: "Dashboard" });
  }
});

const { globalInfo } = useGlobalInfoFetch();

onMounted(() => {
  Toast.warning(t("core.binding.common.toast.mounted"));
});

function handleBinding() {
  const authProvider = globalInfo.value?.socialAuthProviders.find(
    (p) => p.name === route.params.provider
  );

  if (!authProvider?.bindingUrl) {
    Toast.error(t("core.binding.operations.bind.toast_failed"));
    return;
  }

  window.location.href = authProvider?.bindingUrl;

  Toast.success(t("core.binding.operations.bind.toast_success"));
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
      <SignupForm
        v-if="type === 'signup'"
        button-text="core.binding.operations.signup_and_bind.button"
        @succeed="handleBinding"
      />
      <LoginForm
        v-else
        button-text="core.binding.operations.login_and_bind.button"
        @succeed="handleBinding"
      />
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
