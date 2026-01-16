<script lang="ts" setup>
import { VAlert, VButton } from "@halo-dev/components";
import { computed, onMounted, ref } from "vue";
import type { RequiredKey, SecretFormState } from "../types";

const props = withDefaults(
  defineProps<{
    formState?: SecretFormState;
    requiredKeys?: RequiredKey[];
  }>(),
  {
    formState: undefined,
    requiredKeys: () => [],
  }
);

const emit = defineEmits<{
  (event: "submit", data: SecretFormState): void;
}>();

function onSubmit(data: SecretFormState) {
  emit("submit", data);
}

const stringDataArray = ref<{ key: string; value: string }[]>([]);

onMounted(() => {
  if (props.formState) {
    return;
  }

  for (const key of props.requiredKeys) {
    stringDataArray.value.push({
      key: key.key,
      value: "",
    });
  }
});

function getHelpOfRequiredKey(key: string) {
  const requiredKey = props.requiredKeys.find((k) => k.key === key);
  return requiredKey?.help;
}

const missingKeys = computed(() => {
  return props.requiredKeys.filter(
    (k) => !stringDataArray.value.some((s) => s.key === k.key)
  );
});

function handleAddMissingKeys() {
  for (const key of missingKeys.value) {
    stringDataArray.value.push({
      key: key.key,
      value: "",
    });
  }
}
</script>

<template>
  <div v-if="missingKeys.length > 0" class="mb-4">
    <VAlert :title="$t('core.common.text.tip')">
      <template #description>
        {{ $t("core.formkit.secret.required_keys_tip") }}
        <ul class="mt-2">
          <li v-for="key in requiredKeys" :key="key.key">
            {{ key.key }} : {{ key.help }}
          </li>
        </ul>
      </template>
      <template #actions>
        <VButton size="sm" @click="handleAddMissingKeys">
          {{ $t("core.common.buttons.add") }}
        </VButton>
      </template>
    </VAlert>
  </div>
  <FormKit
    id="secret-form"
    type="form"
    :model-value="formState as unknown as Record<string, unknown>"
    name="secret-form"
    ignore
    :config="{ validationVisibility: 'submit' }"
    @submit="onSubmit"
  >
    <FormKit
      :label="$t('core.formkit.secret.form.fields.description')"
      name="description"
    ></FormKit>
    <!-- @vue-ignore -->
    <FormKit
      v-slot="{ value }"
      v-model="stringDataArray"
      type="repeater"
      name="stringDataArray"
      :label="$t('core.formkit.secret.form.fields.string_data')"
    >
      <FormKit
        validation="required"
        name="key"
        label="Key"
        :help="getHelpOfRequiredKey(value.key)"
      ></FormKit>
      <FormKit
        type="code"
        validation="required"
        name="value"
        label="Value"
      ></FormKit>
    </FormKit>
  </FormKit>
</template>
