<script lang="ts" setup>
import type { ListedAuthProvider } from "@halo-dev/api-client";
import {
  AuthProviderSpecAuthTypeEnum,
  consoleApiClient,
} from "@halo-dev/api-client";
import {
  IconLockPasswordLine,
  VLoading,
  VPageHeader,
} from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import { ref } from "vue";
import AuthProvidersSection from "./components/AuthProvidersSection.vue";

const SYSTEM_CONFIGMAP_AUTH_PROVIDER = "authProvider";

const formAuthProviders = ref<ListedAuthProvider[]>([]);
const oauth2AuthProviders = ref<ListedAuthProvider[]>([]);

const { isLoading, refetch } = useQuery<ListedAuthProvider[]>({
  queryKey: ["auth-providers"],
  queryFn: async () => {
    const { data } =
      await consoleApiClient.auth.authProvider.listAuthProviders();
    return data;
  },
  onSuccess(data) {
    formAuthProviders.value = data.filter(
      (authProvider) =>
        authProvider.authType === AuthProviderSpecAuthTypeEnum.Form
    );
    oauth2AuthProviders.value = data.filter(
      (authProvider) =>
        authProvider.authType === AuthProviderSpecAuthTypeEnum.Oauth2
    );
  },
});

// Drag and drop
const updating = ref(false);

async function onSortUpdate() {
  try {
    updating.value = true;

    const allAuthProviders = [
      ...formAuthProviders.value,
      ...oauth2AuthProviders.value,
    ].filter(Boolean);

    await consoleApiClient.configMap.system.updateSystemConfigByGroup({
      group: SYSTEM_CONFIGMAP_AUTH_PROVIDER,
      body: {
        states: allAuthProviders.map((authProvider, index) => {
          return {
            name: authProvider.name,
            enabled: authProvider.enabled,
            priority: index,
          };
        }),
      },
    });
  } finally {
    await refetch();
    updating.value = false;
  }
}
</script>

<template>
  <VPageHeader :title="$t('core.identity_authentication.title')">
    <template #icon>
      <IconLockPasswordLine class="mr-2 self-center" />
    </template>
  </VPageHeader>

  <div class="m-0 space-y-5 md:m-4">
    <VLoading v-if="isLoading" />
    <TransitionGroup v-else appear name="fade">
      <AuthProvidersSection
        :key="AuthProviderSpecAuthTypeEnum.Form"
        v-model="formAuthProviders"
        :title="$t('core.identity_authentication.list.types.form')"
        :loading="updating"
        @update="onSortUpdate"
      />

      <AuthProvidersSection
        v-if="oauth2AuthProviders.length"
        :key="AuthProviderSpecAuthTypeEnum.Oauth2"
        v-model="oauth2AuthProviders"
        :title="$t('core.identity_authentication.list.types.oauth2')"
        :loading="updating"
        @update="onSortUpdate"
      />
    </TransitionGroup>
  </div>
</template>
