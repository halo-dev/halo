<script lang="ts" setup>
import { computed, watch } from "vue";
import IconLogo from "~icons/core/logo?width=5rem&height=2rem";
import LoginForm from "@/components/login/LoginForm.vue";
import { useRouteQuery } from "@vueuse/router";
import SignupForm from "@/components/signup/SignupForm.vue";
import SocialAuthProviders from "@/components/login/SocialAuthProviders.vue";
import { useGlobalInfoFetch } from "@/composables/use-global-info";
import { useTitle } from "@vueuse/core";
import { useI18n } from "vue-i18n";
import { AppName } from "@/constants/app";
import MdiKeyboardBackspace from "~icons/mdi/keyboard-backspace";
import LocaleChange from "@/components/common/LocaleChange.vue";

const { globalInfo } = useGlobalInfoFetch();
const { t } = useI18n();

const SIGNUP_TYPE = "signup";

function onLoginSucceed() {
  window.location.reload();
}

const type = useRouteQuery<string>("type", "");

function handleChangeType() {
  type.value = type.value === SIGNUP_TYPE ? "" : SIGNUP_TYPE;
}

const isLoginType = computed(() => type.value !== SIGNUP_TYPE);

// page title
const title = useTitle();
watch(
  () => type.value,
  (value) => {
    const routeTitle = t(
      `core.${value === SIGNUP_TYPE ? SIGNUP_TYPE : "login"}.title`
    );
    title.value = [routeTitle, AppName].join(" - ");
  }
);
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
      <div class="flex justify-center pt-3.5">
        <a
          class="inline-flex items-center gap-0.5 text-xs text-gray-600 hover:text-gray-900"
          href="/"
        >
          <MdiKeyboardBackspace class="!h-3.5 !w-3.5" />
          <span> {{ $t("core.login.operations.return_site") }} </span>
        </a>
      </div>
    </div>
    <div
      class="bottom-0 mb-10 mt-auto flex items-center justify-center gap-2.5"
    >
      <LocaleChange />
    </div>
  </div>
</template>
