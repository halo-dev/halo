<script lang="ts" setup>
import { onMounted, ref, toRaw } from "vue";
import type { SecretFormState } from "../types";

const props = withDefaults(
  defineProps<{
    formState?: SecretFormState;
  }>(),
  {
    formState: undefined,
  }
);

const defaultValue = ref({});

onMounted(() => {
  if (props.formState) {
    defaultValue.value = toRaw(props.formState);
  }
});

const emit = defineEmits<{
  (event: "submit", data: SecretFormState): void;
}>();

function onSubmit(data: SecretFormState) {
  emit("submit", data);
}
</script>

<template>
  <FormKit
    id="secret-form"
    type="form"
    :model-value="defaultValue"
    name="secret-form"
    ignore
    :config="{ validationVisibility: 'submit' }"
    @submit="onSubmit"
  >
    <FormKit
      :label="$t('core.formkit.secret.form.fields.description')"
      name="description"
    ></FormKit>
    <FormKit
      type="repeater"
      name="stringDataArray"
      :label="$t('core.formkit.secret.form.fields.string_data')"
    >
      <FormKit validation="required" name="key" label="Key"></FormKit>
      <FormKit
        type="code"
        validation="required"
        name="value"
        label="Value"
      ></FormKit>
    </FormKit>
  </FormKit>
</template>
