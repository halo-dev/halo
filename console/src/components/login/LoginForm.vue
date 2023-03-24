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
import { useQuery } from "@tanstack/vue-query";
import type {
  GlobalInfo,
  SocialAuthProvider,
} from "@/modules/system/actuator/types";

const { t } = useI18n();

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

// auth providers
// fixme: Needs to be saved in Pinia.
const { data: socialAuthProviders } = useQuery<SocialAuthProvider[]>({
  queryKey: ["social-auth-providers"],
  queryFn: async () => {
    const { data } = await axios.get<GlobalInfo>(
      `${import.meta.env.VITE_API_URL}/actuator/globalinfo`,
      {
        withCredentials: true,
      }
    );

    return data.socialAuthProviders;
  },
  refetchOnWindowFocus: false,
});
</script>

<template>
  <FormKit
    id="login-form"
    v-model="loginForm"
    name="login-form"
    :actions="false"
    type="form"
    :config="{ validationVisibility: 'submit' }"
    @submit="handleLogin"
    @keyup.enter="submitForm('login-form')"
  >
    <FormKit
      :validation-messages="{
        required: $t('core.login.fields.username.validation'),
      }"
      name="username"
      :placeholder="$t('core.login.fields.username.placeholder')"
      :autofocus="true"
      type="text"
      validation="required"
    >
    </FormKit>
    <FormKit
      id="passwordInput"
      :validation-messages="{
        required: $t('core.login.fields.password.validation'),
      }"
      name="password"
      :placeholder="$t('core.login.fields.password.placeholder')"
      type="password"
      validation="required"
      autocomplete="current-password"
    >
    </FormKit>
  </FormKit>
  <VButton
    class="mt-6"
    block
    :loading="loading"
    type="secondary"
    @click="submitForm('login-form')"
  >
    {{ $t("core.login.button") }}
  </VButton>

  <div v-if="socialAuthProviders?.length" class="mt-3 flex items-center">
    <span class="text-sm text-slate-600">
      {{ $t("core.login.other_login") }}
    </span>
    <ul class="flex items-center">
      <li
        v-for="(socialAuthProvider, index) in socialAuthProviders"
        :key="index"
      >
        <a
          :href="socialAuthProvider.authenticationUrl"
          class="block h-6 w-6 rounded-full bg-gray-200 p-1"
        >
          <img class="rounded-full" :src="socialAuthProvider.logo" />
        </a>
      </li>
    </ul>
  </div>
</template>
