<script lang="ts" setup>
import { onBeforeMount, computed, watch } from "vue";
import router from "@/router";
import IconLogo from "~icons/core/logo?width=5rem&height=2rem";
import { useUserStore } from "@/stores/user";
import LoginForm from "@/components/login/LoginForm.vue";
import { useRouteQuery } from "@vueuse/router";
import SignupForm from "@/components/signup/SignupForm.vue";
import SocialAuthProviders from "@/components/login/SocialAuthProviders.vue";
import { useGlobalInfoFetch } from "@/composables/use-global-info";
import { useTitle } from "@vueuse/core";
import { useI18n } from "vue-i18n";
import { AppName } from "@/constants/app";
import { locales, getBrowserLanguage, i18n } from "@/locales";
import MdiTranslate from "~icons/mdi/translate";
import { useLocalStorage } from "@vueuse/core";

const userStore = useUserStore();
const { globalInfo } = useGlobalInfoFetch();
const { t } = useI18n();

const SIGNUP_TYPE = "signup";

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

// setup locale
const currentLocale = useLocalStorage(
  "locale",
  getBrowserLanguage() || locales[0].code
);

watch(
  () => currentLocale.value,
  (value) => {
    i18n.global.locale.value = value;
  },
  {
    immediate: true,
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
    </div>
    <div
      class="bottom-0 mb-10 mt-auto flex items-center justify-center gap-2.5"
    >
      <label
        for="locale"
        class="block flex-shrink-0 text-sm font-medium text-gray-600"
      >
        <MdiTranslate />
      </label>
      <select
        id="locale"
        v-model="currentLocale"
        class="block appearance-none rounded-md border-0 py-1.5 pl-3 pr-10 text-sm text-gray-800 outline-none ring-1 ring-inset ring-gray-200 focus:ring-primary"
      >
        <template v-for="locale in locales">
          <option v-if="locale.name" :key="locale.code" :value="locale.code">
            {{ locale.name }}
          </option>
        </template>
      </select>
    </div>
  </div>
</template>
