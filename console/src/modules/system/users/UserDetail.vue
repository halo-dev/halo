<script lang="ts" setup>
import { apiClient } from "@/utils/api-client";
import {
  VButton,
  VTabbar,
  VDropdown,
  VDropdownItem,
  VLoading,
} from "@halo-dev/components";
import {
  computed,
  onMounted,
  provide,
  ref,
  type ComputedRef,
  type Ref,
} from "vue";
import { useRoute } from "vue-router";
import type { DetailedUser } from "@halo-dev/api-client";
import UserEditingModal from "./components/UserEditingModal.vue";
import UserPasswordChangeModal from "./components/UserPasswordChangeModal.vue";
import { usePermission } from "@/utils/permission";
import { useUserStore } from "@/stores/user";
import { useQuery } from "@tanstack/vue-query";
import { useI18n } from "vue-i18n";
import { rbacAnnotations } from "@/constants/annotations";
import { onBeforeRouteUpdate } from "vue-router";
import UserAvatar from "./components/UserAvatar.vue";
import type { Raw } from "vue";
import type { Component } from "vue";
import { markRaw } from "vue";
import DetailTab from "./tabs/Detail.vue";
import PersonalAccessTokensTab from "./tabs/PersonalAccessTokens.vue";
import { useRouteQuery } from "@vueuse/router";

const { currentUserHasPermission } = usePermission();
const userStore = useUserStore();
const { t } = useI18n();

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
const name = ref();

onMounted(() => {
  name.value = params.name;
});

// Update name when route change
onBeforeRouteUpdate((to, _, next) => {
  name.value = to.params.name;
  next();
});

const {
  data: user,
  isFetching,
  isLoading,
  refetch,
} = useQuery({
  queryKey: ["user-detail", name],
  queryFn: async () => {
    if (name.value === "-") {
      const { data } = await apiClient.user.getCurrentUserDetail();
      return data;
    } else {
      const { data } = await apiClient.user.getUserDetail({
        name: name.value,
      });
      return data;
    }
  },
  refetchInterval: (data) => {
    const annotations = data?.user.metadata.annotations;
    return annotations?.[rbacAnnotations.AVATAR_ATTACHMENT_NAME] !==
      annotations?.[rbacAnnotations.LAST_AVATAR_ATTACHMENT_NAME]
      ? 1000
      : false;
  },
  enabled: computed(() => !!name.value),
});

const isCurrentUser = computed(() => {
  if (name.value === "-") {
    return true;
  }
  return (
    user.value?.user.metadata.name === userStore.currentUser?.metadata.name
  );
});

provide<Ref<DetailedUser | undefined>>("user", user);
provide<ComputedRef<boolean>>("isCurrentUser", isCurrentUser);

const tabs = computed((): UserTab[] => {
  return [
    {
      id: "detail",
      label: t("core.user.detail.tabs.detail"),
      component: markRaw(DetailTab),
      priority: 10,
    },
    {
      id: "pat",
      label: t("core.user.detail.tabs.pat"),
      component: markRaw(PersonalAccessTokensTab),
      priority: 20,
      hidden: !isCurrentUser.value,
    },
  ];
});

const activeTab = useRouteQuery<string>("tab", tabs.value[0].id, {
  mode: "push",
});
provide<Ref<string>>("activeTab", activeTab);

const tabbarItems = computed(() => {
  return tabs.value
    .filter((tab) => !tab.hidden)
    .map((tab) => ({ id: tab.id, label: tab.label }));
});
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
        <div
          v-if="
            currentUserHasPermission(['system:users:manage']) || isCurrentUser
          "
        >
          <VDropdown>
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
