<script lang="ts" setup>
import { submitForm } from "@formkit/core";
import { Toast, VButton } from "@halo-dev/components";
import qs from "qs";
import axios from "axios";

const emit = defineEmits<{
  (event: "succeed"): void;
}>();

async function onSubmit({ code }: { code: string }) {
  try {
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
    Toast.error("验证失败");
  }
}
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
      :classes="{
        outer: '!py-0',
      }"
      name="code"
      placeholder="请输入两步验证码"
      validation-label="两步验证码"
      :autofocus="true"
      type="text"
      validation="required"
    >
    </FormKit>
  </FormKit>
  <VButton class="mt-8" block type="secondary" @click="submitForm('mfa-form')">
    验证
  </VButton>
</template>
