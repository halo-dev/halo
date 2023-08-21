<script lang="ts" setup>
import BasicLayout from "@/layouts/BasicLayout.vue";
import { apiClient } from "@/utils/api-client";
import {
  IconRiPencilFill,
  VButton,
  VTabbar,
  VAvatar,
  VDropdown,
  VDropdownItem,
  VModal,
  VSpace,
  Toast,
  VLoading,
  Dialog,
} from "@halo-dev/components";
import {
  computed,
  onMounted,
  provide,
  ref,
  watch,
  defineAsyncComponent,
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
import { useFileDialog } from "@vueuse/core";
import { rbacAnnotations } from "@/constants/annotations";
import { onBeforeRouteUpdate } from "vue-router";

const UserAvatarCropper = defineAsyncComponent(
  () => import("../components/UserAvatarCropper.vue")
);

interface IUserAvatarCropperType
  extends Ref<InstanceType<typeof UserAvatarCropper>> {
  getCropperFile(): Promise<File>;
}

const { open, reset, onChange } = useFileDialog({
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

const userAvatarCropper = ref<IUserAvatarCropperType>();
const showAvatarEditor = ref(false);
const visibleCropperModal = ref(false);
const originalFile = ref<File>() as Ref<File>;
onChange((files) => {
  if (!files) {
    return;
  }
  if (files.length > 0) {
    originalFile.value = files[0];
    visibleCropperModal.value = true;
  }
});

const uploadSaving = ref(false);
const handleUploadAvatar = () => {
  userAvatarCropper.value?.getCropperFile().then((file) => {
    uploadSaving.value = true;
    apiClient.user
      .uploadUserAvatar({
        name: name.value,
        file: file,
      })
      .then(() => {
        refetch();
        handleCloseCropperModal();
      })
      .catch(() => {
        Toast.error(t("core.user.detail.avatar.toast_upload_failed"));
      })
      .finally(() => {
        uploadSaving.value = false;
      });
  });
};

const handleRemoveCurrentAvatar = () => {
  Dialog.warning({
    title: t("core.user.detail.avatar.remove.title"),
    description: t("core.common.dialog.descriptions.cannot_be_recovered"),
    confirmType: "danger",
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    onConfirm: async () => {
      apiClient.user
        .deleteUserAvatar({
          name: name.value as string,
        })
        .then(() => {
          refetch();
        })
        .catch(() => {
          Toast.error(t("core.user.detail.avatar.toast_remove_failed"));
        });
    },
  });
};

const handleCloseCropperModal = () => {
  visibleCropperModal.value = false;
  reset();
};

const changeUploadAvatar = () => {
  reset();
  open();
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
            <div class="group relative h-20 w-20">
              <VLoading v-if="isFetching" class="h-full w-full" />
              <div
                v-else
                class="h-full w-full"
                @mouseover="showAvatarEditor = true"
                @mouseout="showAvatarEditor = false"
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
                <VDropdown
                  v-if="
                    currentUserHasPermission(['system:users:manage']) ||
                    isCurrentUser
                  "
                >
                  <div
                    v-show="showAvatarEditor"
                    class="absolute left-0 right-0 top-0 h-full w-full cursor-pointer rounded-full border-0 bg-black/60 text-center leading-[5rem] transition-opacity duration-300 group-hover:opacity-100"
                  >
                    <IconRiPencilFill
                      class="inline-block w-full self-center text-2xl text-white"
                    />
                  </div>
                  <template #popper>
                    <VDropdownItem @click="open()">
                      {{ $t("core.common.buttons.upload") }}
                    </VDropdownItem>
                    <VDropdownItem @click="handleRemoveCurrentAvatar">
                      {{ $t("core.common.buttons.delete") }}
                    </VDropdownItem>
                  </template>
                </VDropdown>
              </div>
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
      :width="1200"
      :title="$t('core.user.detail.avatar.cropper_modal.title')"
      @update:visible="handleCloseCropperModal"
    >
      <UserAvatarCropper
        ref="userAvatarCropper"
        :file="originalFile"
        @change-file="changeUploadAvatar"
      />
      <template #footer>
        <VSpace>
          <VButton
            v-if="visibleCropperModal"
            :loading="uploadSaving"
            type="secondary"
            @click="handleUploadAvatar"
          >
            {{ $t("core.common.buttons.submit") }}
          </VButton>
          <VButton @click="handleCloseCropperModal">
            {{ $t("core.common.buttons.cancel") }}
          </VButton>
        </VSpace>
      </template>
    </VModal>
  </BasicLayout>
</template>
