<script lang="ts" setup>
const emit = defineEmits<{
  (event: "submit", password: string): void;
}>();

function onSubmit({ password }: { password: string }) {
  emit("submit", password);
}
</script>

<template>
  <FormKit
    id="password-validation-form"
    type="form"
    name="password-validation-form"
    @submit="onSubmit"
  >
    <FormKit
      type="password"
      label="验证密码"
      validation="required"
      name="password"
      help="当前账号的登录密码"
      autocomplete="current-password"
    ></FormKit>
  </FormKit>
</template>
