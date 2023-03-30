<script lang="ts" setup>
import type { SocialAuthProvider } from "@/modules/system/actuator/types";
import { ref } from "vue";

const props = withDefaults(
  defineProps<{
    authProvider: SocialAuthProvider;
  }>(),
  {}
);

const loading = ref(false);

function handleSocialLogin() {
  loading.value = true;
  window.location.href = props.authProvider.authenticationUrl;
}
</script>

<template>
  <button
    class="group inline-flex select-none flex-row items-center gap-2 rounded bg-white px-2.5 py-1.5 ring-1 ring-gray-200 transition-all hover:bg-gray-100 hover:shadow hover:ring-gray-900"
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
