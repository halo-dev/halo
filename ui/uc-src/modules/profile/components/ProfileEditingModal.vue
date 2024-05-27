<script lang="ts" setup>
// core libs
import { onMounted, ref } from "vue";
import { apiClient } from "@/utils/api-client";
import type { User } from "@halo-dev/api-client";

// components
import { Toast, VButton, VModal, VSpace } from "@halo-dev/components";
import SubmitButton from "@/components/button/SubmitButton.vue";

// libs
import { cloneDeep } from "lodash-es";

// hooks
import { setFocus } from "@/formkit/utils/focus";
import { useI18n } from "vue-i18n";
import { useQueryClient } from "@tanstack/vue-query";
import { useUserStore } from "@/stores/user";
import EmailVerifyModal from "./EmailVerifyModal.vue";

const { t } = useI18n();
const queryClient = useQueryClient();
const userStore = useUserStore();

const emit = defineEmits<{
  (event: "close"): void;
}>();

const modal = ref<InstanceType<typeof VModal> | null>(null);
const formState = ref<User>(
  cloneDeep(userStore.currentUser) || {
    spec: {
      displayName: "",
      email: "",
      phone: "",
      password: "",
      bio: "",
      disabled: false,
      loginHistoryLimit: 0,
    },
    apiVersion: "v1alpha1",
    kind: "User",
    metadata: {
      name: "",
    },
  }
);
const isSubmitting = ref(false);

onMounted(() => {
  setFocus("displayNameInput");
});

const handleUpdateUser = async () => {
  try {
    isSubmitting.value = true;

    await apiClient.user.updateCurrentUser({
      user: formState.value,
    });

    modal.value?.close();

    queryClient.invalidateQueries({ queryKey: ["user-detail"] });

    Toast.success(t("core.common.toast.save_success"));
  } catch (e) {
    console.error("Failed to update profile", e);
  } finally {
    isSubmitting.value = false;
    userStore.fetchCurrentUser();
  }
};

// verify email
const emailVerifyModal = ref(false);

async function onEmailVerifyModalClose() {
  emailVerifyModal.value = false;
  await userStore.fetchCurrentUser();
  if (userStore.currentUser) formState.value = cloneDeep(userStore.currentUser);
}
</script>
<template>
  <VModal
    ref="modal"
    :title="$t('core.uc_profile.editing_modal.title')"
    :width="700"
    @close="emit('close')"
  >
    <FormKit
      id="user-form"
      name="user-form"
      :config="{ validationVisibility: 'submit' }"
      type="form"
      @submit="handleUpdateUser"
    >
      <div>
        <div class="md:grid md:grid-cols-4 md:gap-6">
          <div class="md:col-span-1">
            <div class="sticky top-0">
              <span class="text-base font-medium text-gray-900">
                {{ $t("core.uc_profile.editing_modal.groups.general") }}
              </span>
            </div>
          </div>
          <div class="mt-5 divide-y divide-gray-100 md:col-span-3 md:mt-0">
            <FormKit
              id="userNameInput"
              v-model="formState.metadata.name"
              :disabled="true"
              :label="$t('core.uc_profile.editing_modal.fields.username.label')"
              type="text"
              name="name"
            ></FormKit>
            <FormKit
              id="displayNameInput"
              v-model="formState.spec.displayName"
              :label="
                $t('core.uc_profile.editing_modal.fields.display_name.label')
              "
              type="text"
              name="displayName"
              validation="required|length:0,50"
            ></FormKit>
            <FormKit
              v-model="formState.spec.email"
              :label="$t('core.uc_profile.editing_modal.fields.email.label')"
              type="email"
              name="email"
              readonly
              validation="required|email|length:0,100"
            >
              <template #suffix>
                <VButton
                  class="rounded-none border-y-0 border-l border-r-0"
                  @click="emailVerifyModal = true"
                >
                  {{ $t("core.common.buttons.modify") }}
                </VButton>
              </template>
            </FormKit>
            <FormKit
              v-model="formState.spec.phone"
              :label="$t('core.uc_profile.editing_modal.fields.phone.label')"
              type="text"
              name="phone"
              validation="length:0,20"
            ></FormKit>
            <FormKit
              v-model="formState.spec.bio"
              :label="$t('core.uc_profile.editing_modal.fields.bio.label')"
              type="textarea"
              name="bio"
              validation="length:0,2048"
            ></FormKit>
          </div>
        </div>
      </div>
    </FormKit>

    <template #footer>
      <VSpace>
        <SubmitButton
          :loading="isSubmitting"
          type="secondary"
          :text="$t('core.common.buttons.submit')"
          @submit="$formkit.submit('user-form')"
        >
        </SubmitButton>
        <VButton @click="modal?.close()">
          {{ $t("core.common.buttons.cancel_and_shortcut") }}
        </VButton>
      </VSpace>
    </template>
  </VModal>

  <EmailVerifyModal v-if="emailVerifyModal" @close="onEmailVerifyModalClose" />
</template>
