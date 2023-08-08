<script lang="ts" setup>
import IconLogo from "~icons/core/logo?width=5rem&height=2rem";
import { apiClient } from "@/utils/api-client";
import { Toast, VButton } from "@halo-dev/components";
import { ref } from "vue";
import { useRouter } from "vue-router";
import type { SystemInitializationRequest } from "@halo-dev/api-client";
import { useI18n } from "vue-i18n";
import { useGlobalInfoStore } from "@/stores/global-info";

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
  <div class="flex h-screen flex-col items-center bg-white/90 pt-[30vh]">
    <IconLogo class="mb-8" />
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
          :validation-messages="{
            required: $t('core.setup.fields.site_title.validation'),
          }"
          type="text"
          :placeholder="$t('core.setup.fields.site_title.placeholder')"
          validation="required|length:0,100"
        ></FormKit>
        <FormKit
          name="email"
          :classes="inputClasses"
          :validation-messages="{
            required: $t('core.setup.fields.email.validation'),
          }"
          type="text"
          :placeholder="$t('core.setup.fields.email.placeholder')"
          validation="required|email|length:0,100"
        ></FormKit>
        <FormKit
          name="username"
          :classes="inputClasses"
          :validation-messages="{
            required: $t('core.setup.fields.username.validation'),
          }"
          type="text"
          :placeholder="$t('core.setup.fields.username.placeholder')"
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
          :placeholder="$t('core.setup.fields.password.placeholder')"
          validation="required:trim|length:5,100|matches:/^\S.*\S$/"
          :validation-messages="{
            required: $t('core.setup.fields.password.validation'),
            matches: $t('core.formkit.validation.trim'),
          }"
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
  </div>
</template>
