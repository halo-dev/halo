<script lang="ts" setup>
import { apiClient } from "@/utils/api-client";
import { Toast, VButton } from "@halo-dev/components";
import { ref } from "vue";
import { useRouter } from "vue-router";
import type { SystemInitializationRequest } from "@halo-dev/api-client";
import { useI18n } from "vue-i18n";
import { useGlobalInfoStore } from "@/stores/global-info";
import LocaleChange from "@/components/common/LocaleChange.vue";

const router = useRouter();
const { t } = useI18n();

const loading = ref(false);

const formState = ref<SystemInitializationRequest>({
  siteTitle: "",
  username: "",
  password: "",
  email: "",
});

const handleSubmit = async () => {
  loading.value = true;

  await apiClient.system.initialize({
    systemInitializationRequest: formState.value,
  });

  const globalInfoStore = useGlobalInfoStore();
  await globalInfoStore.fetchGlobalInfo();

  loading.value = false;

  router.push({ name: "Login" });

  Toast.success(t("core.setup.operations.submit.toast_success"));
};

const inputClasses = {
  outer: "!py-3 first:!pt-0 last:!pb-0",
};
</script>

<template>
  <div class="flex w-72 flex-col">
    <FormKit
      id="setup-form"
      v-model="formState"
      name="setup-form"
      :actions="false"
      :classes="{
        form: '!divide-none',
      }"
      :config="{ validationVisibility: 'submit' }"
      type="form"
      @submit="handleSubmit"
      @keyup.enter="$formkit.submit('setup-form')"
    >
      <FormKit
        name="siteTitle"
        :classes="inputClasses"
        :autofocus="true"
        type="text"
        :validation-label="$t('core.setup.fields.site_title.label')"
        :placeholder="$t('core.setup.fields.site_title.label')"
        validation="required:trim|length:0,100"
      ></FormKit>
      <FormKit
        name="email"
        :classes="inputClasses"
        type="text"
        :validation-label="$t('core.setup.fields.email.label')"
        :placeholder="$t('core.setup.fields.email.label')"
        validation="required|email|length:0,100"
      ></FormKit>
      <FormKit
        name="username"
        :classes="inputClasses"
        type="text"
        :validation-label="$t('core.setup.fields.username.label')"
        :placeholder="$t('core.setup.fields.username.label')"
        :validation="[
          ['required'],
          ['length:0,63'],
          [
            'matches',
            /^[a-z0-9]([-a-z0-9]*[a-z0-9])?(\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*$/,
          ],
        ]"
      ></FormKit>
      <FormKit
        name="password"
        :classes="inputClasses"
        type="password"
        :validation-label="$t('core.setup.fields.password.label')"
        :placeholder="$t('core.setup.fields.password.label')"
        validation="required:trim|length:5,100|matches:/^\S.*\S$/"
        autocomplete="current-password"
      ></FormKit>
      <FormKit
        name="password_confirm"
        :classes="inputClasses"
        type="password"
        :validation-label="$t('core.setup.fields.confirm_password.label')"
        :placeholder="$t('core.setup.fields.confirm_password.label')"
        validation="confirm|required:trim|length:5,100|matches:/^\S.*\S$/"
        autocomplete="current-password"
      ></FormKit>
    </FormKit>
    <VButton
      block
      class="mt-8"
      type="secondary"
      :loading="loading"
      @click="$formkit.submit('setup-form')"
    >
      {{ $t("core.setup.operations.submit.button") }}
    </VButton>
  </div>
  <div class="bottom-0 mb-10 mt-auto flex items-center justify-center gap-2.5">
    <LocaleChange />
  </div>
</template>
