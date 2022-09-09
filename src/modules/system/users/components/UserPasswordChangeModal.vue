<script lang="ts" setup>
import { VButton, VModal, VSpace } from "@halo-dev/components";
import { inject, ref, watch, watchEffect } from "vue";
import type { User } from "@halo-dev/api-client";
import { apiClient } from "@halo-dev/admin-shared";
import cloneDeep from "lodash.clonedeep";
import { reset, submitForm } from "@formkit/core";
import { useMagicKeys } from "@vueuse/core";
import { setFocus } from "@/formkit/utils/focus";

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
  password: string;
  password_confirm?: string;
}

const initialFormState: PasswordChangeFormState = {
  password: "",
  password_confirm: "",
};

const formState = ref<PasswordChangeFormState>(cloneDeep(initialFormState));
const saving = ref(false);

const { Command_Enter } = useMagicKeys();

watchEffect(() => {
  if (Command_Enter.value && props.visible) {
    submitForm("password-form");
  }
});

watch(
  () => props.visible,
  (visible) => {
    if (visible) {
      setFocus("passwordInput");
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

const handleResetForm = () => {
  formState.value = cloneDeep(initialFormState);
  reset("password-form");
};

const handleChangePassword = async () => {
  try {
    saving.value = true;

    const changePasswordRequest = cloneDeep(formState.value);
    delete changePasswordRequest.password_confirm;

    if (props.user?.metadata.name === currentUser?.metadata.name) {
      await apiClient.user.changePassword({
        name: "-",
        changePasswordRequest,
      });
    } else {
      await apiClient.user.changePassword({
        name: props.user?.metadata.name || "",
        changePasswordRequest,
      });
    }

    onVisibleChange(false);
  } catch (e) {
    console.error(e);
  } finally {
    saving.value = false;
  }
};
</script>

<template>
  <VModal
    :visible="visible"
    :width="500"
    title="密码修改"
    @update:visible="onVisibleChange"
  >
    <FormKit
      id="password-form"
      v-model="formState"
      :actions="false"
      type="form"
      :config="{ validationVisibility: 'submit' }"
      @submit="handleChangePassword"
    >
      <FormKit
        id="passwordInput"
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
      <VSpace>
        <VButton
          :loading="saving"
          type="secondary"
          @click="$formkit.submit('password-form')"
        >
          提交 ⌘ + ↵
        </VButton>
        <VButton @click="onVisibleChange(false)">取消 Esc</VButton>
      </VSpace>
    </template>
  </VModal>
</template>
