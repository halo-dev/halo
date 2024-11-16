<script lang="ts" setup>
import { secretAnnotations } from "@/constants/annotations";
import { coreApiClient } from "@halo-dev/api-client";
import { Toast, VButton, VModal, VSpace } from "@halo-dev/components";
import { useMutation, useQueryClient } from "@tanstack/vue-query";
import { ref } from "vue";
import { useI18n } from "vue-i18n";
import { Q_KEY } from "../composables/use-secrets-fetch";
import type { SecretFormState } from "../types";
import SecretForm from "./SecretForm.vue";

const { t } = useI18n();
const queryClient = useQueryClient();

withDefaults(
  defineProps<{
    formState?: SecretFormState;
  }>(),
  { formState: undefined }
);

const emit = defineEmits<{
  (event: "close"): void;
}>();

const modal = ref<InstanceType<typeof VModal> | null>(null);

const { mutate, isLoading } = useMutation({
  mutationKey: ["create-secret"],
  mutationFn: async ({ data }: { data: SecretFormState }) => {
    const stringData = data.stringDataArray.reduce((acc, { key, value }) => {
      acc[key] = value;
      return acc;
    }, {} as Record<string, string>);

    return await coreApiClient.secret.createSecret({
      secret: {
        metadata: {
          generateName: "secret-",
          name: "",
          annotations: {
            [secretAnnotations.DESCRIPTION]: data.description + "",
          },
        },
        kind: "Secret",
        apiVersion: "v1alpha1",
        type: "Opaque",
        stringData: stringData,
      },
    });
  },
  onSuccess() {
    queryClient.invalidateQueries({ queryKey: Q_KEY() });
    Toast.success(t("core.common.toast.save_success"));
    modal.value?.close();
  },
});

function onSubmit(data: SecretFormState) {
  mutate({ data });
}
</script>

<template>
  <VModal
    ref="modal"
    :title="$t('core.formkit.secret.creation_modal.title')"
    :width="600"
    @close="emit('close')"
  >
    <SecretForm :form-state="formState" @submit="onSubmit" />

    <template #footer>
      <VSpace>
        <VButton
          :loading="isLoading"
          type="secondary"
          @click="$formkit.submit('secret-form')"
        >
          {{ $t("core.common.buttons.save") }}
        </VButton>
        <VButton @click="modal?.close()">
          {{ $t("core.common.buttons.close") }}
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>
