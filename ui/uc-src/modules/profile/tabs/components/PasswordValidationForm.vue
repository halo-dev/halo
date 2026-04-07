<script lang="ts" setup>
const props = defineProps<{
  totpConfigured?: boolean;
}>();

const emit = defineEmits<{
  (event: "submit", data: { password: string; totpCode?: string }): void;
}>();

function onSubmit(data: { password: string; totpCode?: string }) {
  emit("submit", data);
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
    <FormKit
      v-if="props.totpConfigured"
      type="number"
      :label="
        $t(
          'core.uc_profile.2fa.password_validation_form.fields.totp_code.label'
        )
      "
      validation="required"
      name="totpCode"
      :help="
        $t(
          'core.uc_profile.2fa.password_validation_form.fields.totp_code.help'
        )
      "
    ></FormKit>
  </FormKit>
</template>
