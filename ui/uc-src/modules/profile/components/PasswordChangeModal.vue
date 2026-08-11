<script lang="ts" setup>
import { ucApiClient } from "@halo-dev/api-client";
import { VButton, VModal, VSpace } from "@halo-dev/components";
import { cloneDeep } from "es-toolkit";
import { onMounted, ref } from "vue";
import SubmitButton from "@/components/button/SubmitButton.vue";
import { PASSWORD_REGEX } from "@/constants/regex";
import { setFocus } from "@/formkit/utils/focus";

const props = withDefaults(defineProps<{ hasPassword?: boolean }>(), {
  hasPassword: true,
});

const emit = defineEmits<{
  (event: "close"): void;
}>();

interface PasswordChangeFormState {
  oldPassword?: string;
  password: string;
  password_confirm?: string;
}

const modal = ref<InstanceType<typeof VModal> | null>(null);

const formState = ref<PasswordChangeFormState>({
  oldPassword: "",
  password: "",
  password_confirm: "",
});
const isSubmitting = ref(false);

onMounted(() => {
  setFocus(props.hasPassword ? "passwordInput" : "newPasswordInput");
});

const handleChangePassword = async () => {
  try {
    isSubmitting.value = true;

    const updatePasswordRequest = cloneDeep(formState.value);
    delete updatePasswordRequest.password_confirm;
    if (!props.hasPassword) {
      delete updatePasswordRequest.oldPassword;
    }

    await ucApiClient.user.currentUser.changeMyPassword({
      changeMyPasswordRequest: updatePasswordRequest,
    });

    window.location.reload();
  } catch (e) {
    console.error(e);
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const redirectURI = (e as any)?.response?.data?.redirectURI;
    if (redirectURI) {
      window.location.href = redirectURI;
    }
  } finally {
    isSubmitting.value = false;
  }
};
</script>

<template>
  <VModal
    ref="modal"
    :width="500"
    :title="
      $t(
        hasPassword
          ? 'core.uc_profile.change_password_modal.title'
          : 'core.uc_profile.change_password_modal.title_set'
      )
    "
    @close="emit('close')"
  >
    <!-- @vue-ignore -->
    <FormKit
      id="password-form"
      v-model="formState"
      name="password-form"
      :actions="false"
      type="form"
      :config="{ validationVisibility: 'submit' }"
      @submit="handleChangePassword"
    >
      <FormKit
        v-if="hasPassword"
        id="passwordInput"
        :label="
          $t('core.uc_profile.change_password_modal.fields.old_password.label')
        "
        name="oldPassword"
        type="password"
        validation="required:trim"
      ></FormKit>
      <FormKit
        id="newPasswordInput"
        :label="
          $t('core.uc_profile.change_password_modal.fields.new_password.label')
        "
        name="password"
        type="password"
        :validation="[
          ['required'],
          ['length', 5, 257],
          ['matches', PASSWORD_REGEX],
        ]"
        :validation-messages="{
          matches: $t('core.formkit.validation.password'),
        }"
      ></FormKit>
      <FormKit
        :label="
          $t(
            'core.uc_profile.change_password_modal.fields.confirm_password.label'
          )
        "
        name="password_confirm"
        type="password"
        validation="confirm|required"
      ></FormKit>
    </FormKit>
    <template #footer>
      <VSpace>
        <SubmitButton
          :loading="isSubmitting"
          type="secondary"
          :text="$t('core.common.buttons.submit')"
          @submit="$formkit.submit('password-form')"
        >
        </SubmitButton>
        <VButton @click="modal?.close()">
          {{ $t("core.common.buttons.cancel_and_shortcut") }}
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>
