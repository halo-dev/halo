<script lang="ts" setup>
// auth providers

import { useAuthProvidersFetch } from "./composables/use-auth-provider";
import SocialAuthProviderItem from "./SocialAuthProviderItem.vue";

// fixme: Needs to be saved in Pinia.
const { socialAuthProviders } = useAuthProvidersFetch();
</script>

<template>
  <ul
    v-if="socialAuthProviders?.length"
    class="flex flex-row flex-wrap justify-center gap-2"
  >
    <li v-for="(socialAuthProvider, index) in socialAuthProviders" :key="index">
      <SocialAuthProviderItem :auth-provider="socialAuthProvider" />
    </li>
  </ul>
</template>
