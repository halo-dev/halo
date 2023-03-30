<script lang="ts" setup>
import { ref, onMounted } from "vue";
import { submitForm } from "@formkit/core";
import { Toast, VButton } from "@halo-dev/components";
import { apiClient } from "@/utils/api-client";
import { useRouteQuery } from "@vueuse/router";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

withDefaults(
  defineProps<{
    buttonText?: string;
  }>(),
  {
    buttonText: "core.signup.operations.submit.button",
  }
);

const formState = ref({
  password: "",
  user: {
    apiVersion: "v1alpha1",
    kind: "User",
    metadata: {
      name: "",
    },
    spec: {
      avatar: "",
      displayName: "",
      email: "",
    },
  },
});
const loading = ref(false);

const emit = defineEmits<{
  (event: "succeed"): void;
}>();

const login = useRouteQuery<string>("login");
const name = useRouteQuery<string>("name");

onMounted(() => {
  if (login.value) {
    formState.value.user.metadata.name = login.value;
  }
  if (name.value) {
    formState.value.user.spec.displayName = name.value;
  }
});

const handleSignup = async () => {
  try {
    loading.value = true;

    await apiClient.common.user.signUp({
      signUpRequest: formState.value,
    });

    Toast.success(t("core.signup.operations.submit.toast_success"));

    emit("succeed");
  } catch (error) {
    console.error("Failed to sign up", error);
  } finally {
    loading.value = false;
  }
};

const inputClasses = {
  outer: "!py-3 first:!pt-0 last:!pb-0",
};
</script>

<template>
  <FormKit
    id="signup-form"
    name="signup-form"
    :actions="false"
    :classes="{
      form: '!divide-none',
    }"
    type="form"
    :config="{ validationVisibility: 'submit' }"
    @submit="handleSignup"
    @keyup.enter="submitForm('signup-form')"
  >
    <FormKit
      v-model="formState.user.metadata.name"
      name="username"
      :placeholder="$t('core.signup.fields.username.placeholder')"
      :validation-label="$t('core.signup.fields.username.placeholder')"
      :classes="inputClasses"
      :autofocus="true"
      type="text"
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
    >
    </FormKit>
    <FormKit
      v-model="formState.user.spec.displayName"
      name="displayName"
      :placeholder="$t('core.signup.fields.display_name.placeholder')"
      :validation-label="$t('core.signup.fields.display_name.placeholder')"
      :classes="inputClasses"
      :autofocus="true"
      type="text"
      validation="required"
    >
    </FormKit>
    <FormKit
      v-model="formState.password"
      name="password"
      :placeholder="$t('core.signup.fields.password.placeholder')"
      :validation-label="$t('core.signup.fields.password.placeholder')"
      :classes="inputClasses"
      type="password"
      validation="required|length:0,100"
    >
    </FormKit>
    <FormKit
      name="password_confirm"
      :placeholder="$t('core.signup.fields.password_confirm.placeholder')"
      :validation-label="$t('core.signup.fields.password_confirm.placeholder')"
      :classes="inputClasses"
      type="password"
      validation="required|confirm|length:0,100"
    >
    </FormKit>
  </FormKit>
  <VButton
    class="mt-8"
    block
    type="secondary"
    :loading="loading"
    @click="submitForm('signup-form')"
  >
    {{ $t(buttonText) }}
  </VButton>
</template>
