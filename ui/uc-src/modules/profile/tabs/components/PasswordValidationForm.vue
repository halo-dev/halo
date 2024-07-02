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
      :label="
        $t('core.uc_profile.2fa.password_validation_form.fields.password.label')
      "
      validation="required"
      name="password"
      :help="
        $t('core.uc_profile.2fa.password_validation_form.fields.password.help')
      "
      autocomplete="current-password"
    ></FormKit>
  </FormKit>
</template>
