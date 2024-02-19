<script lang="ts" setup>
import { apiClient } from "@/utils/api-client";
import {
  IconRiPencilFill,
  IconAddCircle,
  VButton,
  VAvatar,
  VDropdown,
  VDropdownItem,
  VModal,
  VSpace,
  Toast,
  Dialog,
  VLoading,
} from "@halo-dev/components";
import { ref, defineAsyncComponent, type Ref, toRefs } from "vue";
import { usePermission } from "@/utils/permission";
import { useQuery, useQueryClient } from "@tanstack/vue-query";
import { useI18n } from "vue-i18n";
import { useFileDialog } from "@vueuse/core";
import { computed } from "vue";
import { rbacAnnotations } from "@/constants/annotations";

const props = withDefaults(
  defineProps<{
    name?: string;
    isCurrentUser?: boolean;
  }>(),
  {
    name: "-",
    isCurrentUser: false,
  }
);

const { isCurrentUser, name } = toRefs(props);

const queryClient = useQueryClient();
const { currentUserHasPermission } = usePermission();
const { t } = useI18n();

const { data: avatar, isFetching } = useQuery({
  queryKey: ["user-avatar", name, isCurrentUser],
  queryFn: async () => {
    const { data } = props.isCurrentUser
      ? await apiClient.user.getCurrentUserDetail()
      : await apiClient.user.getUserDetail({
          name: props.name,
        });

    const annotations = data?.user.metadata.annotations;

    // Check avatar has been updated. if not, we need retry.
    if (
      annotations?.[rbacAnnotations.AVATAR_ATTACHMENT_NAME] !==
      annotations?.[rbacAnnotations.LAST_AVATAR_ATTACHMENT_NAME]
    ) {
      throw new Error("Avatar is not updated");
    }

    return data.user.spec.avatar || "";
  },
  retry: 5,
  retryDelay: 1000,
});

const UserAvatarCropper = defineAsyncComponent(
  () => import("./UserAvatarCropper.vue")
);

interface IUserAvatarCropperType
  extends Ref<InstanceType<typeof UserAvatarCropper>> {
  getCropperFile(): Promise<File>;
}

const { open, reset, onChange } = useFileDialog({
  accept: ".jpg, .jpeg, .png",
  multiple: false,
});

const userAvatarCropper = ref<IUserAvatarCropperType>();
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
        name: props.isCurrentUser ? "-" : props.name,
        file: file,
      })
      .then(() => {
        queryClient.invalidateQueries({ queryKey: ["user-avatar"] });
        queryClient.invalidateQueries({ queryKey: ["user-detail"] });
        handleCloseCropperModal();
      })
      .catch(() => {
        Toast.error(t("core.components.user_avatar.toast_upload_failed"));
      })
      .finally(() => {
        uploadSaving.value = false;
      });
  });
};

const handleRemoveCurrentAvatar = () => {
  Dialog.warning({
    title: t("core.components.user_avatar.remove.title"),
    description: t("core.common.dialog.descriptions.cannot_be_recovered"),
    confirmType: "danger",
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    onConfirm: async () => {
      apiClient.user
        .deleteUserAvatar({
          name: props.isCurrentUser ? "-" : props.name,
        })
        .then(() => {
          queryClient.invalidateQueries({ queryKey: ["user-avatar"] });
          queryClient.invalidateQueries({ queryKey: ["user-detail"] });
        })
        .catch(() => {
          Toast.error(t("core.components.user_avatar.toast_remove_failed"));
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

const hasAvatar = computed(() => {
  return !!avatar.value;
});
</script>

<template>
  <VLoading v-if="isFetching" class="h-full w-full" />
  <div v-else class="group h-full w-full">
    <VAvatar
      :src="avatar"
      :alt="props.name"
      circle
      width="100%"
      height="100%"
      class="ring-4 ring-white drop-shadow-md"
    />
    <VDropdown
      v-if="currentUserHasPermission(['system:users:manage']) || isCurrentUser"
    >
      <div
        class="absolute left-0 right-0 top-0 hidden h-full w-full cursor-pointer items-center rounded-full border-0 bg-black/60 text-center transition-all duration-300 group-hover:flex"
        :class="{
          '!flex': !hasAvatar,
        }"
      >
        <IconAddCircle
          v-if="!hasAvatar"
          class="inline-block w-full self-center text-2xl text-white"
        />
        <IconRiPencilFill
          v-else
          class="inline-block w-full self-center text-2xl text-white"
        />
      </div>
      <template #popper>
        <VDropdownItem @click="open()">
          {{ $t("core.common.buttons.upload") }}
        </VDropdownItem>
        <VDropdownItem v-if="hasAvatar" @click="handleRemoveCurrentAvatar">
          {{ $t("core.common.buttons.delete") }}
        </VDropdownItem>
      </template>
    </VDropdown>
  </div>

  <VModal
    :visible="visibleCropperModal"
    :width="1200"
    :title="$t('core.components.user_avatar.cropper_modal.title')"
    mount-to-body
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
</template>
