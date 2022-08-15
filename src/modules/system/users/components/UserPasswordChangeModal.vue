<script lang="ts" setup>
import { IconSave, VButton, VModal } from "@halo-dev/components";
import { inject, ref } from "vue";
import type { User } from "@halo-dev/api-client";
import { apiClient } from "@halo-dev/admin-shared";
import cloneDeep from "lodash.clonedeep";

const props = withDefaults(
  defineProps<{
    visible: boolean;
    user: User | null;
  }>(),
  {
    visible: false,
    user: null,
  }
);

const emit = defineEmits<{
  (event: "update:visible", visible: boolean): void;
  (event: "close"): void;
}>();

const currentUser = inject<User>("currentUser");

interface PasswordChangeFormState {
  state: {
    password: string;
    password_confirm?: string;
  };
  submitting: boolean;
}

const formState = ref<PasswordChangeFormState>({
  state: {
    password: "",
    password_confirm: "",
  },
  submitting: false,
});

const handleVisibleChange = (visible: boolean) => {
  emit("update:visible", visible);
  if (!visible) {
    formState.value.state.password = "";
    formState.value.state.password_confirm = "";
    emit("close");
  }
};

const handleChangePassword = async () => {
  try {
    formState.value.submitting = true;

    const changePasswordRequest = cloneDeep(formState.value.state);
    delete changePasswordRequest.password_confirm;

    if (props.user?.metadata.name === currentUser?.metadata.name) {
      await apiClient.user.changePassword("-", changePasswordRequest);
    } else {
      await apiClient.user.changePassword(
        props.user?.metadata.name || "",
        changePasswordRequest
      );
    }

    handleVisibleChange(false);
  } catch (e) {
    console.error(e);
  } finally {
    formState.value.submitting = false;
  }
};
</script>

<template>
  <VModal
    :visible="visible"
    :width="500"
    title="密码修改"
    @update:visible="handleVisibleChange"
  >
    <FormKit
      id="password-form"
      v-model="formState.state"
      :actions="false"
      type="form"
      @submit="handleChangePassword"
    >
      <FormKit
        label="新密码"
        name="password"
        type="password"
        validation="required"
      ></FormKit>
      <FormKit
        label="确认密码"
        name="password_confirm"
        type="password"
        validation="required|confirm"
      ></FormKit>
    </FormKit>
    <template #footer>
      <VButton
        :loading="formState.submitting"
        type="secondary"
        @click="$formkit.submit('password-form')"
      >
        <template #icon>
          <IconSave class="h-full w-full" />
        </template>
        保存
      </VButton>
    </template>
  </VModal>
</template>
