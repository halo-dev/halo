<script lang="ts" setup>
import { setFocus } from "@/formkit/utils/focus";
import { useUserStore } from "@/stores/user";
import { randomUUID } from "@/utils/id";
import axios from "axios";
import { AxiosError } from "axios";
import { Toast, VButton } from "@halo-dev/components";
import { onMounted, ref } from "vue";
import qs from "qs";
import { submitForm, reset } from "@formkit/core";
import { JSEncrypt } from "jsencrypt";
import { apiClient } from "@/utils/api-client";
import { useI18n } from "vue-i18n";
import { ERROR_MFA_REQUIRED_TYPE } from "@/constants/error-types";
import MfaForm from "./MfaForm.vue";

const { t } = useI18n();

withDefaults(
  defineProps<{
    buttonText?: string;
  }>(),
  {
    buttonText: "core.login.button",
  }
);

const emit = defineEmits<{
  (event: "succeed"): void;
}>();

const userStore = useUserStore();

const _csrf = ref("");

const loading = ref(false);

const handleGenerateToken = async () => {
  const token = randomUUID();
  _csrf.value = token;
  const expires = new Date();
  expires.setFullYear(expires.getFullYear() + 1);
  document.cookie = `XSRF-TOKEN=${token}; Path=/; SameSite=Lax; expires=${expires.toUTCString()}`;
};

async function handleLogin(data: {
  username: string;
  password: string;
  rememberMe: boolean;
}) {
  try {
    loading.value = true;

    const { data: publicKey } = await apiClient.login.getPublicKey();

    const encrypt = new JSEncrypt();
    encrypt.setPublicKey(publicKey.base64Format as string);

    await axios.post(
      `/login?remember-me=${data.rememberMe}`,
      qs.stringify({
        _csrf: _csrf.value,
        username: data.username,
        password: encrypt.encrypt(data.password),
      }),
      {
        withCredentials: true,
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
          "X-Requested-With": "XMLHttpRequest",
        },
      }
    );

    await userStore.fetchCurrentUser();

    emit("succeed");
  } catch (e: unknown) {
    console.error("Failed to login", e);

    if (e instanceof AxiosError) {
      if (/Network Error/.test(e.message)) {
        Toast.error(t("core.common.toast.network_error"));
        return;
      }

      if (e.response?.status === 403) {
        Toast.warning(t("core.login.operations.submit.toast_csrf"), {
          duration: 5000,
        });
        await handleGenerateToken();
        return;
      }

      const {
        title: errorTitle,
        detail: errorDetail,
        type: errorType,
      } = e.response?.data || {};

      if (errorType === ERROR_MFA_REQUIRED_TYPE) {
        mfaRequired.value = true;
        return;
      }

      if (errorTitle || errorDetail) {
        Toast.error(errorDetail || errorTitle);
      } else {
        Toast.error(t("core.common.toast.unknown_error"));
      }
    } else {
      Toast.error(t("core.common.toast.unknown_error"));
    }

    reset("passwordInput");
    setFocus("passwordInput");
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  handleGenerateToken();
});

const inputClasses = {
  outer: "!py-3 first:!pt-0 last:!pb-0",
};

// mfa
const mfaRequired = ref(false);
</script>

<template>
  <template v-if="!mfaRequired">
    <FormKit
      id="login-form"
      name="login-form"
      :actions="false"
      type="form"
      :classes="{
        form: '!divide-none',
      }"
      :config="{ validationVisibility: 'submit' }"
      @submit="handleLogin"
      @keyup.enter="submitForm('login-form')"
    >
      <FormKit
        :classes="inputClasses"
        name="username"
        :placeholder="$t('core.login.fields.username.placeholder')"
        :validation-label="$t('core.login.fields.username.placeholder')"
        :autofocus="true"
        type="text"
        validation="required"
      >
      </FormKit>
      <FormKit
        id="passwordInput"
        :classes="inputClasses"
        name="password"
        :placeholder="$t('core.login.fields.password.placeholder')"
        :validation-label="$t('core.login.fields.password.placeholder')"
        type="password"
        validation="required"
        autocomplete="current-password"
      >
      </FormKit>

      <FormKit
        type="checkbox"
        :label="$t('core.login.fields.remember_me.label')"
        name="rememberMe"
        :value="false"
        :classes="inputClasses"
      ></FormKit>
    </FormKit>
    <VButton
      class="mt-6"
      block
      :loading="loading"
      type="secondary"
      @click="submitForm('login-form')"
    >
      {{ $t(buttonText) }}
    </VButton>
  </template>
  <MfaForm v-else @succeed="$emit('succeed')" />
</template>
