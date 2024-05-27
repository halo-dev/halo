<script lang="ts" setup>
import { VButton, VModal, VSpace } from "@halo-dev/components";
import SubmitButton from "@/components/button/SubmitButton.vue";
import { onMounted, ref } from "vue";
import type { User } from "@halo-dev/api-client";
import { apiClient } from "@/utils/api-client";
import { cloneDeep } from "lodash-es";
import { setFocus } from "@/formkit/utils/focus";

const props = withDefaults(
  defineProps<{
    user?: User;
  }>(),
  {
    user: undefined,
  }
);

const emit = defineEmits<{
  (event: "close"): void;
}>();

interface PasswordChangeFormState {
  password: string;
  password_confirm?: string;
}

const initialFormState: PasswordChangeFormState = {
  password: "",
  password_confirm: "",
};

const modal = ref<InstanceType<typeof VModal> | null>(null);
const formState = ref<PasswordChangeFormState>(cloneDeep(initialFormState));
const isSubmitting = ref(false);

onMounted(() => {
  setFocus("passwordInput");
});

const handleChangePassword = async () => {
  try {
    isSubmitting.value = true;

    const changePasswordRequest = cloneDeep(formState.value);
    delete changePasswordRequest.password_confirm;

    await apiClient.user.changeAnyonePassword({
      name: props.user?.metadata.name || "",
      changePasswordRequest,
    });

    modal.value?.close();
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
    :title="$t('core.user.change_password_modal.title')"
    @close="emit('close')"
  >
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
        :label="$t('core.user.change_password_modal.fields.new_password.label')"
        name="password"
        type="password"
        validation="required:trim|length:5,100|matches:/^\S.*\S$/"
        :validation-messages="{
          matches: $t('core.formkit.validation.trim'),
        }"
      ></FormKit>
      <FormKit
        :label="
          $t('core.user.change_password_modal.fields.confirm_password.label')
        "
        name="password_confirm"
        type="password"
        validation="confirm|required:trim|length:5,100|matches:/^\S.*\S$/"
        :validation-messages="{
          matches: $t('core.formkit.validation.trim'),
        }"
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
