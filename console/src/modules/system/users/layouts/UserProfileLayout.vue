<script lang="ts" setup>
import BasicLayout from "@/layouts/BasicLayout.vue";
import { apiClient } from "@/utils/api-client";
import {
  VButton,
  VTabbar,
  VAvatar,
  VDropdown,
  VDropdownItem,
} from "@halo-dev/components";
import {
  computed,
  onMounted,
  provide,
  ref,
  watch,
  type ComputedRef,
  type Ref,
} from "vue";
import { useRoute, useRouter } from "vue-router";
import type { DetailedUser } from "@halo-dev/api-client";
import UserEditingModal from "../components/UserEditingModal.vue";
import UserPasswordChangeModal from "../components/UserPasswordChangeModal.vue";
import { usePermission } from "@/utils/permission";
import { useUserStore } from "@/stores/user";
import { useQuery } from "@tanstack/vue-query";
import { useI18n } from "vue-i18n";

const { currentUserHasPermission } = usePermission();
const userStore = useUserStore();
const { t } = useI18n();

const tabs = [
  {
    id: "detail",
    label: t("core.user.detail.tabs.detail"),
    routeName: "UserDetail",
  },
  // {
  //   id: "tokens",
  //   label: "个人令牌",
  //   routeName: "PersonalAccessTokens",
  // },
];

const editingModal = ref(false);
const passwordChangeModal = ref(false);

const { params } = useRoute();

const {
  data: user,
  isLoading,
  refetch,
} = useQuery({
  queryKey: ["user-detail", params.name],
  queryFn: async () => {
    if (params.name === "-") {
      const { data } = await apiClient.user.getCurrentUserDetail();
      return data;
    } else {
      const { data } = await apiClient.user.getUserDetail({
        name: params.name as string,
      });
      return data;
    }
  },
});

const isCurrentUser = computed(() => {
  if (params.name === "-") {
    return true;
  }
  return (
    user.value?.user.metadata.name === userStore.currentUser?.metadata.name
  );
});

provide<Ref<DetailedUser | undefined>>("user", user);
provide<ComputedRef<boolean>>("isCurrentUser", isCurrentUser);

const activeTab = ref();

const route = useRoute();
const router = useRouter();

// set default active tab
onMounted(() => {
  const tab = tabs.find((tab) => tab.routeName === route.name);
  activeTab.value = tab ? tab.id : tabs[0].id;
});

watch(
  () => route.name,
  async (newRouteName) => {
    const tab = tabs.find((tab) => tab.routeName === newRouteName);
    activeTab.value = tab ? tab.id : tabs[0].id;
  }
);

const handleTabChange = (id: string) => {
  const tab = tabs.find((tab) => tab.id === id);
  if (tab) {
    router.push({ name: tab.routeName });
  }
};
</script>
<template>
  <BasicLayout>
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
            <div class="h-20 w-20">
              <VAvatar
                v-if="user"
                :src="user.user.spec.avatar"
                :alt="user.user.spec.displayName"
                circle
                width="100%"
                height="100%"
                class="ring-4 ring-white drop-shadow-md"
              />
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
        :items="tabs"
        class="w-full"
        type="outline"
        @change="handleTabChange"
      ></VTabbar>
      <div class="mt-2">
        <RouterView></RouterView>
      </div>
    </section>
  </BasicLayout>
</template>
