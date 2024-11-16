<script lang="ts" setup>
import SubmitButton from "@/components/button/SubmitButton.vue";
import { PASSWORD_REGEX } from "@/constants/regex";
import { setFocus } from "@/formkit/utils/focus";
import { consoleApiClient } from "@halo-dev/api-client";
import { VButton, VModal, VSpace } from "@halo-dev/components";
import { cloneDeep } from "lodash-es";
import { onMounted, ref } from "vue";

const emit = defineEmits<{
  (event: "close"): void;
}>();

interface PasswordChangeFormState {
  oldPassword: string;
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
  setFocus("passwordInput");
});

const handleChangePassword = async () => {
  try {
    isSubmitting.value = true;

    const changeOwnPasswordRequest = cloneDeep(formState.value);
    delete changeOwnPasswordRequest.password_confirm;

    await consoleApiClient.user.changeOwnPassword({
      changeOwnPasswordRequest,
    });

    window.location.reload();
  } catch (e) {
    console.error(e);
  } finally {
    isSubmitting.value = false;
  }
};
</script>

<template>
  <VModal
    ref="modal"
    :width="500"
    :title="$t('core.uc_profile.change_password_modal.title')"
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
        id="passwordInput"
        :label="
          $t('core.uc_profile.change_password_modal.fields.old_password.label')
        "
        name="oldPassword"
        type="password"
        validation="required:trim"
      ></FormKit>
      <FormKit
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
