<script lang="ts" setup>
import SubmitButton from "@/components/button/SubmitButton.vue";
import { PASSWORD_REGEX } from "@/constants/regex";
import { setFocus } from "@/formkit/utils/focus";
import type { User } from "@halo-dev/api-client";
import { consoleApiClient } from "@halo-dev/api-client";
import { VButton, VModal, VSpace } from "@halo-dev/components";
import { cloneDeep } from "lodash-es";
import { onMounted, ref } from "vue";

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

    await consoleApiClient.user.changeAnyonePassword({
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
        :label="$t('core.user.change_password_modal.fields.new_password.label')"
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
          $t('core.user.change_password_modal.fields.confirm_password.label')
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
