<script lang="ts" setup>
import { apiClient } from "@/utils/api-client";
import {
  VButton,
  VTabbar,
  VDropdown,
  VDropdownItem,
  VLoading,
} from "@halo-dev/components";
import { computed, provide, ref, type Ref } from "vue";
import { useRoute } from "vue-router";
import type { DetailedUser } from "@halo-dev/api-client";
import UserEditingModal from "./components/UserEditingModal.vue";
import UserPasswordChangeModal from "./components/UserPasswordChangeModal.vue";
import { usePermission } from "@/utils/permission";
import { useQuery } from "@tanstack/vue-query";
import { useI18n } from "vue-i18n";
import { rbacAnnotations } from "@/constants/annotations";
import UserAvatar from "@/components/user-avatar/UserAvatar.vue";
import type { Raw } from "vue";
import type { Component } from "vue";
import { markRaw } from "vue";
import DetailTab from "./tabs/Detail.vue";
import { useRouteQuery } from "@vueuse/router";
import { useUserStore } from "@/stores/user";

const { currentUserHasPermission } = usePermission();
const { t } = useI18n();
const { currentUser } = useUserStore();

interface UserTab {
  id: string;
  label: string;
  component: Raw<Component>;
  props?: Record<string, unknown>;
  permissions?: string[];
  priority: number;
  hidden?: boolean;
}

const editingModal = ref(false);
const passwordChangeModal = ref(false);

const { params } = useRoute();

const {
  data: user,
  isFetching,
  isLoading,
  refetch,
} = useQuery({
  queryKey: ["user-detail", params.name],
  queryFn: async () => {
    const { data } = await apiClient.user.getUserDetail({
      name: params.name as string,
    });
    return data;
  },
  refetchInterval: (data) => {
    const annotations = data?.user.metadata.annotations;
    return annotations?.[rbacAnnotations.AVATAR_ATTACHMENT_NAME] !==
      annotations?.[rbacAnnotations.LAST_AVATAR_ATTACHMENT_NAME]
      ? 1000
      : false;
  },
  enabled: computed(() => !!params.name),
});

provide<Ref<DetailedUser | undefined>>("user", user);

const tabs: UserTab[] = [
  {
    id: "detail",
    label: t("core.user.detail.tabs.detail"),
    component: markRaw(DetailTab),
    priority: 10,
  },
];

const activeTab = useRouteQuery<string>("tab", tabs[0].id, {
  mode: "push",
});

provide<Ref<string>>("activeTab", activeTab);

const tabbarItems = computed(() => {
  return tabs.map((tab) => ({ id: tab.id, label: tab.label }));
});

function handleRouteToUC() {
  window.location.href = "/uc";
}
</script>
<template>
  <UserEditingModal v-model:visible="editingModal" :user="user?.user" />

  <UserPasswordChangeModal
    v-model:visible="passwordChangeModal"
    :user="user?.user"
    @close="refetch"
  />

  <header class="bg-white">
    <div class="p-4">
      <div class="flex items-center justify-between">
        <div class="flex flex-row items-center gap-5">
          <div class="group relative h-20 w-20">
            <VLoading v-if="isFetching" class="h-full w-full" />
            <UserAvatar v-else />
          </div>
          <div class="block">
            <h1 class="truncate text-lg font-bold text-gray-900">
              {{ user?.user.spec.displayName }}
            </h1>
            <span v-if="!isLoading" class="text-sm text-gray-600">
              @{{ user?.user.metadata.name }}
            </span>
          </div>
        </div>
        <div class="inline-flex items-center gap-2">
          <VButton
            v-if="currentUser?.metadata.name === user?.user.metadata.name"
            type="primary"
            @click="handleRouteToUC"
          >
            个人中心
          </VButton>
          <VDropdown v-if="currentUserHasPermission(['system:users:manage'])">
            <VButton type="default">
              {{ $t("core.common.buttons.edit") }}
            </VButton>
            <template #popper>
              <VDropdownItem @click="editingModal = true">
                {{ $t("core.user.detail.actions.update_profile.title") }}
              </VDropdownItem>
              <VDropdownItem @click="passwordChangeModal = true">
                {{ $t("core.user.detail.actions.change_password.title") }}
              </VDropdownItem>
            </template>
          </VDropdown>
        </div>
      </div>
    </div>
  </header>
  <section class="bg-white p-4">
    <VTabbar
      v-model:active-id="activeTab"
      :items="tabbarItems"
      class="w-full"
      type="outline"
    ></VTabbar>
    <div class="mt-2">
      <template v-for="tab in tabs" :key="tab.id">
        <component
          :is="tab.component"
          v-if="activeTab === tab.id && !tab.hidden"
        />
      </template>
    </div>
  </section>
</template>
