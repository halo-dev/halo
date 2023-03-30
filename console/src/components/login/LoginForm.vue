<script lang="ts" setup>
import { setFocus } from "@/formkit/utils/focus";
import { useUserStore } from "@/stores/user";
import { randomUUID } from "@/utils/id";
import axios from "axios";
import { AxiosError } from "axios";
import { Toast, VButton } from "@halo-dev/components";
import { onMounted, ref } from "vue";
import qs from "qs";
import { submitForm } from "@formkit/core";
import { JSEncrypt } from "jsencrypt";
import { apiClient } from "@/utils/api-client";
import { useI18n } from "vue-i18n";

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

interface LoginForm {
  _csrf: string;
  username: string;
  password: string;
}

const userStore = useUserStore();

const loginForm = ref<LoginForm>({
  _csrf: "",
  username: "",
  password: "",
});

const loading = ref(false);

const handleGenerateToken = async () => {
  const token = randomUUID();
  loginForm.value._csrf = token;
  document.cookie = `XSRF-TOKEN=${token}; Path=/;`;
};

const handleLogin = async () => {
  try {
    loading.value = true;

    const { data: publicKey } = await apiClient.login.getPublicKey();

    const encrypt = new JSEncrypt();
    encrypt.setPublicKey(publicKey.base64Format as string);

    await axios.post(
      `${import.meta.env.VITE_API_URL}/login`,
      qs.stringify({
        ...loginForm.value,
        password: encrypt.encrypt(loginForm.value.password),
      }),
      {
        withCredentials: true,
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
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

      Toast.error(t("core.login.operations.submit.toast_failed"));
    } else {
      Toast.error(t("core.common.toast.unknown_error"));
    }

    loginForm.value.password = "";
    setFocus("passwordInput");
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  handleGenerateToken();
});

const inputClasses = {
  outer: "!py-3 first:!pt-0 last:!pb-0",
};
</script>

<template>
  <FormKit
    id="login-form"
    v-model="loginForm"
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
  </FormKit>
  <VButton
    class="mt-8"
    block
    :loading="loading"
    type="secondary"
    @click="submitForm('login-form')"
  >
    {{ $t(buttonText) }}
  </VButton>
</template>
