<script lang="ts" setup>
import BasicLayout from "@/layouts/BasicLayout.vue";
import { apiClient } from "@/utils/api-client";
import {
  VButton,
  VTabbar,
  VAvatar,
  VDropdown,
  VDropdownItem,
  VModal,
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
import UserAvatarCropper from "../components/UserAvatarCropper.vue";
import { usePermission } from "@/utils/permission";
import { useUserStore } from "@/stores/user";
import { useQuery } from "@tanstack/vue-query";
import { useI18n } from "vue-i18n";
import { useFileDialog } from "@vueuse/core";

const { files, open } = useFileDialog({
  accept: ".jpg, .jpeg, .png",
  multiple: false,
});
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

const showAvatarEditor = ref(false);
const visibleCropperModal = ref(false);
const originalFile = ref<File>() as Ref<File>;
watch(
  () => files,
  (files) => {
    if (!files.value) {
      return;
    }
    if (files.value?.length > 0) {
      originalFile.value = files.value[0];
      visibleCropperModal.value = true;
    }
  },
  {
    deep: true,
  }
);
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
            <div
              class="group relative h-20 w-20"
              @mouseenter="showAvatarEditor = true"
              @mouseleave="showAvatarEditor = false"
            >
              <VAvatar
                v-if="user"
                :src="user.user.spec.avatar"
                :alt="user.user.spec.displayName"
                circle
                width="100%"
                height="100%"
                class="ring-4 ring-white drop-shadow-md"
              />
              <VDropdown>
                <div
                  class="absolute left-0 right-0 top-0 h-full w-full cursor-pointer rounded-full border-0 bg-black/60 text-center text-2xl font-bold leading-[5rem] text-white transition-opacity duration-300 group-hover:opacity-100"
                >
                  1
                </div>
                <template #popper>
                  <VDropdownItem @click="open"> 上传 </VDropdownItem>
                  <VDropdownItem> 移除 </VDropdownItem>
                </template>
              </VDropdown>
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
    <VModal
      :visible="visibleCropperModal"
      :width="800"
      :height="'800px'"
      title="裁剪图片"
      @update:visible="(visible) => (visibleCropperModal = visible)"
    >
      <UserAvatarCropper
        :file="originalFile"
        @close="visibleCropperModal = false"
      />
    </VModal>
  </BasicLayout>
</template>
