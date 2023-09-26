<script lang="ts" setup>
import {
  Dialog,
  IconUserSettings,
  VButton,
  VDescription,
  VDescriptionItem,
  VTag,
} from "@halo-dev/components";
import type { ComputedRef, Ref } from "vue";
import { inject, computed } from "vue";
import { useRouter } from "vue-router";
import type { DetailedUser, ListedAuthProvider } from "@halo-dev/api-client";
import { rbacAnnotations } from "@/constants/annotations";
import { formatDatetime } from "@/utils/date";
import { useQuery } from "@tanstack/vue-query";
import { apiClient } from "@/utils/api-client";
import axios from "axios";
import { useI18n } from "vue-i18n";

const user = inject<Ref<DetailedUser | undefined>>("user");
const isCurrentUser = inject<ComputedRef<boolean>>("isCurrentUser");

const router = useRouter();
const { t } = useI18n();

const { data: authProviders, isFetching } = useQuery<ListedAuthProvider[]>({
  queryKey: ["user-auth-providers"],
  queryFn: async () => {
    const { data } = await apiClient.authProvider.listAuthProviders();
    return data;
  },
  enabled: isCurrentUser,
});

const availableAuthProviders = computed(() => {
  return authProviders.value?.filter(
    (authProvider) => authProvider.enabled && authProvider.supportsBinding
  );
});

const handleUnbindAuth = (authProvider: ListedAuthProvider) => {
  Dialog.warning({
    title: t("core.user.detail.operations.unbind.title", {
      display_name: authProvider.displayName,
    }),
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    onConfirm: async () => {
      await axios.put(
        `${import.meta.env.VITE_API_URL}${authProvider.unbindingUrl}`,
        {
          withCredentials: true,
        }
      );

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
  <div class="border-t border-gray-100">
    <VDescription>
      <VDescriptionItem
        :label="$t('core.user.detail.fields.display_name')"
        :content="user?.user.spec.displayName"
        class="!px-2"
      />
      <VDescriptionItem
        :label="$t('core.user.detail.fields.username')"
        :content="user?.user.metadata.name"
        class="!px-2"
      />
      <VDescriptionItem
        :label="$t('core.user.detail.fields.email')"
        :content="user?.user.spec.email || $t('core.common.text.none')"
        class="!px-2"
      />
      <VDescriptionItem
        :label="$t('core.user.detail.fields.roles')"
        class="!px-2"
      >
        <VTag
          v-for="(role, index) in user?.roles"
          :key="index"
          @click="
            router.push({
              name: 'RoleDetail',
              params: { name: role.metadata.name },
            })
          "
        >
          <template #leftIcon>
            <IconUserSettings />
          </template>
          {{
            role.metadata.annotations?.[rbacAnnotations.DISPLAY_NAME] ||
            role.metadata.name
          }}
        </VTag>
      </VDescriptionItem>
      <VDescriptionItem
        :label="$t('core.user.detail.fields.bio')"
        :content="user?.user.spec?.bio || $t('core.common.text.none')"
        class="!px-2"
      />
      <VDescriptionItem
        :label="$t('core.user.detail.fields.creation_time')"
        :content="formatDatetime(user?.user.metadata?.creationTimestamp)"
        class="!px-2"
      />
      <VDescriptionItem
        v-if="!isFetching && isCurrentUser && availableAuthProviders?.length"
        :label="$t('core.user.detail.fields.identity_authentication')"
        class="!px-2"
      >
        <ul class="space-y-2">
          <template v-for="(authProvider, index) in authProviders">
            <li
              v-if="authProvider.supportsBinding && authProvider.enabled"
              :key="index"
            >
              <div
                class="flex w-full cursor-pointer flex-wrap justify-between gap-y-3 rounded border p-5 hover:border-primary sm:w-1/2"
              >
                <div class="inline-flex items-center gap-3">
                  <div>
                    <img class="h-7 w-7 rounded" :src="authProvider.logo" />
                  </div>
                  <div class="text-sm font-medium text-gray-900">
                    {{ authProvider.displayName }}
                  </div>
                </div>
                <div class="inline-flex items-center">
                  <VButton
                    v-if="authProvider.isBound"
                    size="sm"
                    @click="handleUnbindAuth(authProvider)"
                  >
                    {{ $t("core.user.detail.operations.unbind.button") }}
                  </VButton>
                  <VButton
                    v-else
                    size="sm"
                    type="secondary"
                    @click="handleBindAuth(authProvider)"
                  >
                    {{ $t("core.user.detail.operations.bind.button") }}
                  </VButton>
                </div>
              </div>
            </li>
          </template>
        </ul>
      </VDescriptionItem>
    </VDescription>
  </div>
</template>
