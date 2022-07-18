<script lang="ts" setup>
import { VButton } from "@halo-dev/components";
import { inject, ref } from "vue";
import type { Ref } from "vue";
import { apiClient } from "@halo-dev/admin-shared";
import type { User } from "@halo-dev/api-client";

interface FormState {
  state: {
    password: string;
    password_confirm?: string;
  };
  submitting: boolean;
}

const user = inject<Ref<User>>("user");
const currentUser = inject<User>("currentUser");

const formState = ref<FormState>({
  state: {
    password: "",
    password_confirm: "",
  },
  submitting: false,
});

const handleChangePassword = async () => {
  try {
    formState.value.submitting = true;
    if (user?.value.metadata.name === currentUser?.metadata.name) {
      await apiClient.user.changePassword("-", formState.value.state);
    }
  } catch (e) {
    console.error(e);
  } finally {
    formState.value.submitting = false;
  }
};
</script>
<template>
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

  <div class="pt-5">
    <div class="flex justify-start">
      <VButton
        :loading="formState.submitting"
        type="secondary"
        @click="$formkit.submit('password-form')"
        >修改密码
      </VButton>
    </div>
  </div>
</template>
