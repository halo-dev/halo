<script lang="ts" setup>
import BasicLayout from "@/layouts/BasicLayout.vue";
import { apiClient } from "@/utils/api-client";
import { VButton, VSpace, VTabbar, VAvatar } from "@halo-dev/components";
import { computed, onMounted, provide, ref, watch, type Ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import type { DetailedUser } from "@halo-dev/api-client";
import UserEditingModal from "../components/UserEditingModal.vue";
import UserPasswordChangeModal from "../components/UserPasswordChangeModal.vue";
import { usePermission } from "@/utils/permission";
import { useUserStore } from "@/stores/user";

const { currentUserHasPermission } = usePermission();
const userStore = useUserStore();

const tabs = [
  {
    id: "detail",
    label: "详情",
    routeName: "UserDetail",
  },
  // {
  //   id: "tokens",
  //   label: "个人令牌",
  //   routeName: "PersonalAccessTokens",
  // },
];

const user = ref<DetailedUser>();
const loading = ref();
const editingModal = ref(false);
const passwordChangeModal = ref(false);

const { params } = useRoute();

const handleFetchUser = async () => {
  try {
    loading.value = true;
    if (params.name === "-") {
      const { data } = await apiClient.user.getCurrentUserDetail();
      user.value = data;
    } else {
      const { data } = await apiClient.user.getUserDetail({
        name: params.name as string,
      });
      user.value = data;
    }
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
};

const isCurrentUser = computed(() => {
  if (params.name === "-") {
    return true;
  }
  return (
    user.value?.user.metadata.name === userStore.currentUser?.metadata.name
  );
});

provide<Ref<DetailedUser | undefined>>("user", user);

const activeTab = ref();

const route = useRoute();
const router = useRouter();

// set default active tab
onMounted(() => {
  handleFetchUser();
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
    <UserEditingModal
      v-model:visible="editingModal"
      :user="user?.user"
      @close="handleFetchUser"
    />
    <UserPasswordChangeModal
      v-model:visible="passwordChangeModal"
      :user="user?.user"
      @close="handleFetchUser"
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
              <span v-if="!loading" class="text-sm text-gray-600">
                @{{ user?.user.metadata.name }}
              </span>
            </div>
          </div>
          <div
            v-if="
              currentUserHasPermission(['system:users:manage']) || isCurrentUser
            "
          >
            <FloatingDropdown>
              <VButton type="default">编辑</VButton>
              <template #popper>
                <div class="w-48 p-2">
                  <VSpace class="w-full" direction="column">
                    <VButton
                      v-close-popper
                      block
                      type="secondary"
                      @click="editingModal = true"
                    >
                      修改资料
                    </VButton>
                    <VButton
                      v-close-popper
                      block
                      @click="passwordChangeModal = true"
                    >
                      修改密码
                    </VButton>
                  </VSpace>
                </div>
              </template>
            </FloatingDropdown>
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
