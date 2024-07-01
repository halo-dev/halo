<script lang="ts" setup>
import { setFocus } from "@/formkit/utils/focus";
import { submitForm } from "@formkit/core";
import { Toast, VButton } from "@halo-dev/components";
import axios from "axios";
import qs from "qs";
import { onMounted, ref } from "vue";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const emit = defineEmits<{
  (event: "succeed"): void;
}>();

const loading = ref(false);

async function onSubmit({ code }: { code: string }) {
  try {
    loading.value = true;

    const _csrf = document.cookie
      .split("; ")
      .find((row) => row.startsWith("XSRF-TOKEN"))
      ?.split("=")[1];

    if (!_csrf) {
      Toast.warning("CSRF token not found");
      return;
    }
    await axios.post(
      `/login/2fa/totp`,
      qs.stringify({
        code,
        _csrf,
      }),
      {
        withCredentials: true,
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
          "X-Requested-With": "XMLHttpRequest",
        },
      }
    );

    emit("succeed");
  } catch (error) {
    Toast.error(t("core.common.toast.validation_failed"));
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  setFocus("code");
});
</script>

<template>
  <FormKit
    id="mfa-form"
    name="mfa-form"
    type="form"
    :classes="{
      form: '!divide-none',
    }"
    :config="{ validationVisibility: 'submit' }"
    @submit="onSubmit"
    @keyup.enter="submitForm('mfa-form')"
  >
    <FormKit
      id="code"
      :classes="{
        outer: '!py-0',
      }"
      name="code"
      :placeholder="$t('core.login.2fa.fields.code.placeholder')"
      :validation-label="$t('core.login.2fa.fields.code.label')"
      type="text"
      validation="required"
    >
    </FormKit>
  </FormKit>
  <VButton
    :loading="loading"
    class="mt-8"
    block
    type="secondary"
    @click="submitForm('mfa-form')"
  >
    {{ $t("core.common.buttons.verify") }}
  </VButton>
</template>
