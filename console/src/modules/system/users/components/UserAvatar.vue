<script lang="ts" setup>
import { apiClient } from "@/utils/api-client";
import {
  IconRiPencilFill,
  VButton,
  VAvatar,
  VDropdown,
  VDropdownItem,
  VModal,
  VSpace,
  Toast,
  Dialog,
} from "@halo-dev/components";
import { ref, defineAsyncComponent, type ComputedRef, type Ref } from "vue";
import type { DetailedUser } from "@halo-dev/api-client";
import { usePermission } from "@/utils/permission";
import { useQueryClient } from "@tanstack/vue-query";
import { useI18n } from "vue-i18n";
import { useFileDialog } from "@vueuse/core";
import { inject } from "vue";

const queryClient = useQueryClient();
const { currentUserHasPermission } = usePermission();
const { t } = useI18n();

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

const user = inject<Ref<DetailedUser | undefined>>("user");
const isCurrentUser = inject<ComputedRef<boolean>>("isCurrentUser");

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
    if (!user?.value) {
      return;
    }

    uploadSaving.value = true;

    apiClient.user
      .uploadUserAvatar({
        name: user.value.user.metadata.name,
        file: file,
      })
      .then(() => {
        queryClient.invalidateQueries({ queryKey: ["user-detail"] });
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
      if (!user?.value) {
        return;
      }

      apiClient.user
        .deleteUserAvatar({
          name: user.value.user.metadata.name,
        })
        .then(() => {
          queryClient.invalidateQueries({ queryKey: ["user-detail"] });
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
  <div
    class="h-full w-full"
    @mouseover="showAvatarEditor = true"
    @mouseout="showAvatarEditor = false"
  >
    <VAvatar
      :src="user?.user.spec.avatar"
      :alt="user?.user.spec.displayName"
      circle
      width="100%"
      height="100%"
      class="ring-4 ring-white drop-shadow-md"
    />
    <VDropdown
      v-if="currentUserHasPermission(['system:users:manage']) || isCurrentUser"
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

  <VModal
    :visible="visibleCropperModal"
    :width="1200"
    :title="$t('core.user.detail.avatar.cropper_modal.title')"
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
