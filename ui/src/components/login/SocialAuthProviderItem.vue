<script lang="ts" setup>
import type { SocialAuthProvider } from "@/types";
import { useRouteQuery } from "@vueuse/router";
import { inject, ref } from "vue";
import type { Ref } from "vue";

const props = withDefaults(
  defineProps<{
    authProvider: SocialAuthProvider;
  }>(),
  {}
);

const REDIRECT_URI_QUERY_PARAM = "login_redirect_uri";

const loading = ref(false);

const redirect_uri = useRouteQuery<string>("redirect_uri", "");
const disabled = inject<Ref<boolean>>("disabled");

function handleSocialLogin() {
  if (disabled) {
    disabled.value = true;
  }

  loading.value = true;

  let authenticationUrl = props.authProvider.authenticationUrl;

  if (redirect_uri.value) {
    authenticationUrl = `${authenticationUrl}?${REDIRECT_URI_QUERY_PARAM}=${redirect_uri.value}`;
  }

  window.location.href = authenticationUrl;
}
</script>

<template>
  <button
    class="group inline-flex select-none flex-row items-center gap-2 rounded bg-white px-2.5 py-1.5 ring-1 ring-gray-200 transition-all hover:bg-gray-100 hover:shadow hover:ring-gray-900"
    :class="{
      'cursor-not-allowed opacity-80 hover:shadow-none hover:ring-gray-200':
        disabled,
    }"
    :disabled="disabled"
    @click="handleSocialLogin"
  >
    <svg
      v-if="loading"
      class="h-4 w-4 animate-spin"
      fill="none"
      viewBox="0 0 24 24"
      xmlns="http://www.w3.org/2000/svg"
    >
      <circle
        class="opacity-25"
        cx="12"
        cy="12"
        r="10"
        stroke="currentColor"
        stroke-width="4"
      ></circle>
      <path
        class="opacity-75"
        d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
        fill="currentColor"
      ></path>
    </svg>

    <img
      v-else
      :alt="authProvider.displayName"
      class="h-4 w-4 rounded-full"
      :src="authProvider.logo"
    />

    <span class="text-xs text-gray-800 group-hover:text-gray-900">
      {{ authProvider.displayName }}
    </span>
  </button>
</template>
