<script lang="ts" setup>
import {
  Dialog,
  IconUserSettings,
  VAlert,
  VButton,
  VDescription,
  VDescriptionItem,
  VTag,
} from "@halo-dev/components";
import { computed, ref } from "vue";
import type { DetailedUser, ListedAuthProvider } from "@halo-dev/api-client";
import { rbacAnnotations } from "@/constants/annotations";
import { formatDatetime } from "@/utils/date";
import { useQuery } from "@tanstack/vue-query";
import { apiClient } from "@/utils/api-client";
import axios from "axios";
import { useI18n } from "vue-i18n";
import EmailVerifyModal from "../components/EmailVerifyModal.vue";
import RiVerifiedBadgeLine from "~icons/ri/verified-badge-line";

withDefaults(defineProps<{ user?: DetailedUser }>(), { user: undefined });

const { t } = useI18n();

const { data: authProviders, isFetching } = useQuery<ListedAuthProvider[]>({
  queryKey: ["user-auth-providers"],
  queryFn: async () => {
    const { data } = await apiClient.authProvider.listAuthProviders();
    return data;
  },
});

const availableAuthProviders = computed(() => {
  return authProviders.value?.filter(
    (authProvider) => authProvider.enabled && authProvider.supportsBinding
  );
});

const handleUnbindAuth = (authProvider: ListedAuthProvider) => {
  Dialog.warning({
    title: t("core.uc_profile.detail.operations.unbind.title", {
      display_name: authProvider.displayName,
    }),
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
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

// verify email
const emailVerifyModal = ref(false);
</script>
<template>
  <div class="border-t border-gray-100">
    <VDescription>
      <VDescriptionItem
        :label="$t('core.uc_profile.detail.fields.display_name')"
        :content="user?.user.spec.displayName"
        class="!px-2"
      />
      <VDescriptionItem
        :label="$t('core.uc_profile.detail.fields.username')"
        :content="user?.user.metadata.name"
        class="!px-2"
      />
      <VDescriptionItem
        :label="$t('core.uc_profile.detail.fields.email')"
        class="!px-2"
      >
        <div v-if="user" class="w-full xl:w-1/2">
          <VAlert
            v-if="!user.user.spec.email"
            :title="$t('core.uc_profile.detail.email_not_set.title')"
            :description="
              $t('core.uc_profile.detail.email_not_set.description')
            "
            type="warning"
            :closable="false"
          >
            <template #actions>
              <VButton size="sm" @click="emailVerifyModal = true">
                {{ $t("core.common.buttons.setting") }}
              </VButton>
            </template>
          </VAlert>

          <div v-else>
            <div class="flex items-center space-x-2">
              <span>{{ user.user.spec.email }}</span>
              <RiVerifiedBadgeLine
                v-if="user.user.spec.emailVerified"
                v-tooltip="$t('core.uc_profile.detail.email_verified.tooltip')"
                class="text-xs text-blue-600"
              />
            </div>
            <div v-if="!user.user.spec.emailVerified" class="mt-3">
              <VAlert
                :title="$t('core.uc_profile.detail.email_not_verified.title')"
                :description="
                  $t('core.uc_profile.detail.email_not_verified.description')
                "
                type="warning"
                :closable="false"
              >
                <template #actions>
                  <VButton size="sm" @click="emailVerifyModal = true">
                    {{ $t("core.common.buttons.verify") }}
                  </VButton>
                </template>
              </VAlert>
            </div>
          </div>
        </div>
      </VDescriptionItem>
      <VDescriptionItem
        :label="$t('core.uc_profile.detail.fields.roles')"
        class="!px-2"
      >
        <VTag v-for="role in user?.roles" :key="role.metadata.name">
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
        :label="$t('core.uc_profile.detail.fields.bio')"
        :content="user?.user.spec?.bio || $t('core.common.text.none')"
        class="!px-2"
      />
      <VDescriptionItem
        :label="$t('core.uc_profile.detail.fields.creation_time')"
        :content="formatDatetime(user?.user.metadata?.creationTimestamp)"
        class="!px-2"
      />
      <VDescriptionItem
        v-if="!isFetching && availableAuthProviders?.length"
        :label="$t('core.uc_profile.detail.fields.identity_authentication')"
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
                    {{ $t("core.uc_profile.detail.operations.unbind.button") }}
                  </VButton>
                  <VButton
                    v-else
                    size="sm"
                    type="secondary"
                    @click="handleBindAuth(authProvider)"
                  >
                    {{ $t("core.uc_profile.detail.operations.bind.button") }}
                  </VButton>
                </div>
              </div>
            </li>
          </template>
        </ul>
      </VDescriptionItem>
    </VDescription>

    <EmailVerifyModal
      v-if="emailVerifyModal"
      @close="emailVerifyModal = false"
    />
  </div>
</template>
