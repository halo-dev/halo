<script lang="ts" setup>
// auth providers

import { useAuthProvidersFetch } from "./composables/use-auth-provider";
import SocialAuthProviderItem from "./SocialAuthProviderItem.vue";

// fixme: Needs to be saved in Pinia.
const { socialAuthProviders } = useAuthProvidersFetch();
</script>

<template>
  <Transition v-if="socialAuthProviders?.length" appear name="fade">
    <div>
      <div
        class="my-4 flex items-center before:ml-1 before:mt-0.5 before:flex-1 before:border-t before:border-gray-200 after:mr-1 after:mt-0.5 after:flex-1 after:border-t after:border-gray-200"
      >
        <p class="mx-4 mb-0 text-center text-xs dark:text-neutral-600">
          {{ $t("core.components.social_auth_providers.title") }}
        </p>
      </div>
      <ul class="flex flex-row flex-wrap justify-center gap-2">
        <li
          v-for="(socialAuthProvider, index) in socialAuthProviders"
          :key="index"
        >
          <SocialAuthProviderItem :auth-provider="socialAuthProvider" />
        </li>
      </ul>
    </div>
  </Transition>
</template>
