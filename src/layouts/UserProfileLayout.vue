<script lang="ts" setup>
import { BasicLayout } from "@/layouts";
import { IconUpload, VButton, VTabbar } from "@halo-dev/components";
import { onMounted, provide, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { users } from "@/modules/system/users/users-mock";
import { Starport } from "vue-starport";

const tabs = [
  {
    id: "detail",
    label: "详情",
    routeName: "UserDetail",
  },
  {
    id: "tokens",
    label: "个人令牌",
    routeName: "PersonalAccessTokens",
  },
  {
    id: "profile-modification",
    label: "资料修改",
    routeName: "ProfileModification",
  },
  {
    id: "password-change",
    label: "密码修改",
    routeName: "PasswordChange",
  },
];

const user = ref();

const route = useRoute();

if (route.params.username) {
  user.value = users.find((u) => u.username === route.params.username);
} else {
  user.value = users[0];
}

provide("user", user);

const activeTab = ref();

const { name: currentRouteName } = useRoute();
const router = useRouter();

// set default active tab
onMounted(() => {
  const tab = tabs.find((tab) => tab.routeName === currentRouteName);
  activeTab.value = tab ? tab.id : tabs[0].id;
});

const handleTabChange = (id: string) => {
  const tab = tabs.find((tab) => tab.id === id);
  if (tab) {
    router.push({ name: tab.routeName });
  }
};
</script>
<template>
  <BasicLayout>
    <header class="bg-white">
      <div :class="user.cover" class="h-48 bg-gradient-to-r"></div>
      <div class="px-4 sm:px-6 lg:px-8">
        <div class="-mt-12 flex items-end space-x-5 sm:-mt-16">
          <div class="flex">
            <Starport
              :port="`user-profile-${user.name}`"
              class="h-24 w-24 sm:h-32 sm:w-32"
              :duration="400"
            >
              <img
                :src="user.avatar"
                alt="Avatar"
                class="rounded-full ring-4 ring-white drop-shadow-lg"
              />
            </Starport>
          </div>
          <div
            class="mt-6 sm:flex sm:min-w-0 sm:flex-1 sm:items-center sm:justify-end sm:space-x-6 sm:pb-1"
          >
            <div class="mt-6 block min-w-0 flex-1">
              <h1 class="truncate text-xl font-bold text-gray-900">
                <span class="mr-1">{{ user.name }}</span>
              </h1>
            </div>
            <div
              class="justify-stretch mt-6 hidden flex-col space-y-3 sm:flex-row sm:space-y-0 sm:space-x-4 md:flex"
            >
              <VButton type="default">退出登录</VButton>
            </div>
          </div>
        </div>
      </div>

      <div class="absolute top-6 right-6">
        <div class="">
          <IconUpload class="text-white" />
        </div>
      </div>
    </header>
    <section class="bg-white p-4 sm:px-6 lg:px-8">
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
