<script lang="ts" setup>
import { onBeforeMount, watch } from "vue";
import router from "@/router";
import IconLogo from "~icons/core/logo?width=5rem&height=2rem";
import { useUserStore } from "@/stores/user";
import LoginForm from "@/components/login/LoginForm.vue";
import { useRouteQuery } from "@vueuse/router";
import SignupForm from "@/components/signup/SignupForm.vue";
import { locales, getBrowserLanguage, i18n } from "@/locales";
import MdiTranslate from "~icons/mdi/translate";
import { useLocalStorage } from "@vueuse/core";

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
    <div
      class="absolute bottom-0 mb-10 flex items-center justify-center gap-2.5"
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
