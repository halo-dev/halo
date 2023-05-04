<script lang="ts" setup>
// core libs
import { ref, watch } from "vue";
import { apiClient } from "@/utils/api-client";
import type { CreateUserRequest } from "@halo-dev/api-client";

// components
import { Toast, VButton, VModal, VSpace } from "@halo-dev/components";
import SubmitButton from "@/components/button/SubmitButton.vue";

// libs
import cloneDeep from "lodash.clonedeep";
import { reset } from "@formkit/core";

// hooks
import { setFocus } from "@/formkit/utils/focus";
import { useI18n } from "vue-i18n";
import { useQueryClient } from "@tanstack/vue-query";

const { t } = useI18n();
const queryClient = useQueryClient();

const props = withDefaults(
  defineProps<{
    visible: boolean;
  }>(),
  {
    visible: false,
  }
);

const emit = defineEmits<{
  (event: "update:visible", visible: boolean): void;
  (event: "close"): void;
}>();

const initialFormState: CreateUserRequest = {
  avatar: "",
  bio: "",
  displayName: "",
  email: "",
  name: "",
  password: "",
  phone: "",
  roles: [],
};

const formState = ref<CreateUserRequest>(cloneDeep(initialFormState));
const selectedRole = ref("");
const saving = ref(false);

const handleResetForm = () => {
  formState.value = cloneDeep(initialFormState);
  reset("user-creation-form");
};

watch(
  () => props.visible,
  (visible) => {
    if (visible) {
      setFocus("creationUserNameInput");
    } else {
      handleResetForm();
    }
  }
);

const onVisibleChange = (visible: boolean) => {
  emit("update:visible", visible);
  if (!visible) {
    emit("close");
  }
};

const handleCreateUser = async () => {
  try {
    saving.value = true;

    if (selectedRole.value) {
      formState.value.roles = [selectedRole.value];
    }

    await apiClient.user.createUser({
      createUserRequest: formState.value,
    });

    onVisibleChange(false);

    Toast.success(t("core.common.toast.save_success"));

    queryClient.invalidateQueries({ queryKey: ["users"] });
  } catch (e) {
    console.error("Failed to create or update user", e);
  } finally {
    saving.value = false;
  }
};
</script>
<template>
  <VModal
    :title="$t('core.user.editing_modal.titles.create')"
    :visible="visible"
    :width="650"
    @update:visible="onVisibleChange"
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
        v-model="formState.avatar"
        :label="$t('core.user.editing_modal.fields.avatar.label')"
        type="attachment"
        name="avatar"
        validation="length:0,1024"
      ></FormKit>
      <FormKit
        v-model="formState.password"
        :label="$t('core.user.change_password_modal.fields.new_password.label')"
        type="password"
        name="password"
        validation="required|length:0,100"
      ></FormKit>
      <FormKit
        v-model="selectedRole"
        :label="$t('core.user.grant_permission_modal.fields.role.label')"
        type="roleSelect"
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
          v-if="visible"
          :loading="saving"
          type="secondary"
          :text="$t('core.common.buttons.submit')"
          @submit="$formkit.submit('user-creation-form')"
        >
        </SubmitButton>
        <VButton @click="onVisibleChange(false)">
          {{ $t("core.common.buttons.cancel_and_shortcut") }}
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>
