<script lang="ts" setup>
import type { ListedAuthProvider } from "@halo-dev/api-client";
import { consoleApiClient } from "@halo-dev/api-client";
import {
  Dialog,
  VButton,
  VEntity,
  VEntityContainer,
  VEntityField,
} from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import axios from "axios";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const { data: authProviders } = useQuery<ListedAuthProvider[]>({
  queryKey: ["user-auth-providers"],
  queryFn: async () => {
    const { data } =
      await consoleApiClient.auth.authProvider.listAuthProviders();
    return data.filter(
      (authProvider) => authProvider.enabled && authProvider.supportsBinding
    );
  },
});

const handleUnbindAuth = (authProvider: ListedAuthProvider) => {
  Dialog.warning({
    title: t("core.uc_profile.auth_providers.operations.unbind.title", {
      display_name: authProvider.displayName,
    }),
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    confirmType: "danger",
    onConfirm: async () => {
      await axios.put(`${authProvider.unbindingUrl}`, {
        withCredentials: true,
      });

      window.location.reload();
    },
  });
};

const handleBindAuth = (authProvider: ListedAuthProvider) => {
  if (!authProvider.bindingUrl) {
    return;
  }
  window.location.href = `${
    authProvider.bindingUrl
  }?redirect_uri=${encodeURIComponent(window.location.href)}`;
};
</script>

<template>
  <Transition v-if="authProviders?.length" appear name="fade">
    <div class="overflow-hidden rounded-base border">
      <div class="flex items-center justify-between bg-gray-50 px-4 py-3">
        <span class="text-sm font-semibold text-gray-900">
          {{ $t("core.uc_profile.auth_providers.title") }}
        </span>
      </div>
      <VEntityContainer>
        <VEntity v-for="authProvider in authProviders" :key="authProvider.name">
          <template #start>
            <VEntityField v-if="authProvider.logo">
              <template #description>
                <div class="size-8 overflow-hidden rounded-lg">
                  <img
                    class="size-full object-cover"
                    :src="authProvider.logo"
                    :alt="authProvider.displayName"
                  />
                </div>
              </template>
            </VEntityField>
            <VEntityField
              :title="authProvider.displayName"
              :description="authProvider.description"
            />
          </template>
          <template #end>
            <VEntityField>
              <template #description>
                <VButton
                  v-if="authProvider.isBound"
                  size="sm"
                  @click="handleUnbindAuth(authProvider)"
                >
                  {{
                    $t(
                      "core.uc_profile.auth_providers.operations.unbind.button"
                    )
                  }}
                </VButton>
                <VButton
                  v-else
                  size="sm"
                  type="secondary"
                  @click="handleBindAuth(authProvider)"
                >
                  {{
                    $t("core.uc_profile.auth_providers.operations.bind.button")
                  }}
                </VButton>
              </template>
            </VEntityField>
          </template>
        </VEntity>
      </VEntityContainer>
    </div>
  </Transition>
</template>
