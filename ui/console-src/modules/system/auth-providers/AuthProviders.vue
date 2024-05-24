<script lang="ts" setup>
import {
  VPageHeader,
  IconLockPasswordLine,
  VCard,
  VLoading,
} from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import { apiClient } from "@/utils/api-client";
import type { AuthProvider, ListedAuthProvider } from "@halo-dev/api-client";
import AuthProviderListItem from "./components/AuthProviderListItem.vue";
import { computed, ref } from "vue";
import Fuse from "fuse.js";
import { VueDraggable } from "vue-draggable-plus";

const authProviders = ref<ListedAuthProvider[]>([]);

const { isLoading, refetch } = useQuery<ListedAuthProvider[]>({
  queryKey: ["auth-providers"],
  queryFn: async () => {
    const { data } = await apiClient.authProvider.listAuthProviders();
    return data;
  },
  onSuccess(data) {
    authProviders.value = data;
    fuse = new Fuse(data, {
      keys: ["name", "displayName"],
      useExtendedSearch: true,
      threshold: 0.2,
    });
  },
});

const keyword = ref("");
let fuse: Fuse<ListedAuthProvider> | undefined = undefined;

const searchResults = computed(() => {
  if (!fuse || !keyword.value) {
    return authProviders.value;
  }

  return fuse?.search(keyword.value).map((item) => item.item);
});

// Drag and drop
const updating = ref(false);

async function onSortUpdate() {
  try {
    updating.value = true;

    const { data: rawAuthProviders } =
      await apiClient.extension.authProvider.listAuthHaloRunV1alpha1AuthProvider();

    const authProviderNames = authProviders.value.map((item) => item.name);

    const sortedAuthProviders = authProviderNames
      .map((name) => {
        const authProvider = rawAuthProviders.items.find(
          (item) => item.metadata.name === name
        );
        if (authProvider) {
          authProvider.spec.priority = authProviderNames.indexOf(name);
        }
        return authProvider;
      })
      .filter(Boolean) as AuthProvider[];

    for (const authProvider of sortedAuthProviders) {
      await apiClient.extension.authProvider.updateAuthHaloRunV1alpha1AuthProvider(
        {
          name: authProvider.metadata.name,
          authProvider: authProvider,
        }
      );
    }
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

  <div class="m-0 md:m-4">
    <VCard :body-class="['!p-0']">
      <template #header>
        <div class="block w-full bg-gray-50 px-4 py-3">
          <div
            class="relative flex flex-col items-start sm:flex-row sm:items-center"
          >
            <div class="flex w-full flex-1 sm:w-auto">
              <FormKit
                v-model="keyword"
                :placeholder="$t('core.common.placeholder.search')"
                type="text"
              ></FormKit>
            </div>
          </div>
        </div>
      </template>
      <VLoading v-if="isLoading" />
      <Transition v-else appear name="fade">
        <VueDraggable
          v-model="authProviders"
          ghost-class="opacity-50"
          handle=".drag-element"
          class="box-border h-full w-full divide-y divide-gray-100"
          :class="{
            'cursor-progress opacity-60': updating,
          }"
          role="list"
          tag="ul"
          :disabled="updating"
          @update="onSortUpdate"
        >
          <li v-for="(authProvider, index) in searchResults" :key="index">
            <AuthProviderListItem
              :auth-provider="authProvider"
              @reload="refetch"
            />
          </li>
        </VueDraggable>
      </Transition>
    </VCard>
  </div>
</template>
