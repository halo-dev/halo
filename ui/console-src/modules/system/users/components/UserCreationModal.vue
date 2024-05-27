<script lang="ts" setup>
// core libs
import { onMounted, ref } from "vue";
import { apiClient } from "@/utils/api-client";
import type { CreateUserRequest } from "@halo-dev/api-client";

// components
import { Toast, VButton, VModal, VSpace } from "@halo-dev/components";
import SubmitButton from "@/components/button/SubmitButton.vue";

// hooks
import { setFocus } from "@/formkit/utils/focus";
import { useI18n } from "vue-i18n";
import { useQueryClient } from "@tanstack/vue-query";

const { t } = useI18n();
const queryClient = useQueryClient();

const emit = defineEmits<{
  (event: "close"): void;
}>();

const modal = ref<InstanceType<typeof VModal> | null>(null);
const formState = ref<CreateUserRequest>({
  avatar: "",
  bio: "",
  displayName: "",
  email: "",
  name: "",
  password: "",
  phone: "",
  roles: [],
});
const selectedRole = ref("");
const isSubmitting = ref(false);

onMounted(() => {
  setFocus("creationUserNameInput");
});

const handleCreateUser = async () => {
  try {
    isSubmitting.value = true;

    if (selectedRole.value) {
      formState.value.roles = [selectedRole.value];
    }

    await apiClient.user.createUser({
      createUserRequest: formState.value,
    });

    modal.value?.close();

    Toast.success(t("core.common.toast.save_success"));

    queryClient.invalidateQueries({ queryKey: ["users"] });
  } catch (e) {
    console.error("Failed to create or update user", e);
  } finally {
    isSubmitting.value = false;
  }
};
</script>
<template>
  <VModal
    ref="modal"
    :title="$t('core.user.editing_modal.titles.create')"
    :width="650"
    @close="emit('close')"
  >
    <FormKit
      id="user-creation-form"
      name="user-creation-form"
      :config="{ validationVisibility: 'submit' }"
      type="form"
      @submit="handleCreateUser"
    >
      <FormKit
        id="creationUserNameInput"
        v-model="formState.name"
        :label="$t('core.user.editing_modal.fields.username.label')"
        type="text"
        name="name"
        :validation="[
          ['required'],
          ['length:0,63'],
          [
            'matches',
            /^[a-z0-9]([-a-z0-9]*[a-z0-9])?(\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*$/,
          ],
        ]"
        :validation-messages="{
          matches: $t('core.user.editing_modal.fields.username.validation'),
        }"
      ></FormKit>
      <FormKit
        v-model="formState.displayName"
        :label="$t('core.user.editing_modal.fields.display_name.label')"
        type="text"
        name="displayName"
        validation="required|length:0,50"
      ></FormKit>
      <FormKit
        v-model="formState.email"
        :label="$t('core.user.editing_modal.fields.email.label')"
        type="email"
        name="email"
        validation="required|email|length:0,100"
      ></FormKit>
      <FormKit
        v-model="formState.phone"
        :label="$t('core.user.editing_modal.fields.phone.label')"
        type="text"
        name="phone"
        validation="length:0,20"
      ></FormKit>
      <FormKit
        v-model="formState.password"
        :label="$t('core.user.change_password_modal.fields.new_password.label')"
        type="password"
        name="password"
        validation="required:trim|length:5,100|matches:/^\S.*\S$/"
        :validation-messages="{
          matches: $t('core.formkit.validation.trim'),
        }"
      ></FormKit>
      <FormKit
        v-model="selectedRole"
        :label="$t('core.user.grant_permission_modal.fields.role.label')"
        type="roleSelect"
        validation="required"
      ></FormKit>
      <FormKit
        v-model="formState.bio"
        :label="$t('core.user.editing_modal.fields.bio.label')"
        type="textarea"
        name="bio"
        validation="length:0,2048"
      ></FormKit>
    </FormKit>

    <template #footer>
      <VSpace>
        <SubmitButton
          :loading="isSubmitting"
          type="secondary"
          :text="$t('core.common.buttons.submit')"
          @submit="$formkit.submit('user-creation-form')"
        >
        </SubmitButton>
        <VButton @click="modal?.close()">
          {{ $t("core.common.buttons.cancel_and_shortcut") }}
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>
