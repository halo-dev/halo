<script lang="ts" setup>
import { secretAnnotations } from "@/constants/annotations";
import { coreApiClient, type Secret } from "@halo-dev/api-client";
import { Toast, VButton, VModal, VSpace } from "@halo-dev/components";
import { useMutation, useQueryClient } from "@tanstack/vue-query";
import { ref } from "vue";
import { useI18n } from "vue-i18n";
import { Q_KEY } from "../composables/use-secrets-fetch";
import type { SecretFormState } from "../types";
import SecretForm from "./SecretForm.vue";

const { t } = useI18n();
const queryClient = useQueryClient();

const props = withDefaults(
  defineProps<{
    secret: Secret;
  }>(),
  {}
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

    return await coreApiClient.secret.patchSecret({
      name: props.secret.metadata.name,
      jsonPatchInner: [
        {
          op: "add",
          path: "/stringData",
          value: stringData,
        },
        {
          op: "add",
          path: `/metadata/annotations`,
          value: {
            ...props.secret.metadata.annotations,
            [secretAnnotations.DESCRIPTION]: data.description,
          },
        },
      ],
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

const formState: SecretFormState = {
  description:
    props.secret.metadata.annotations?.[secretAnnotations.DESCRIPTION],
  stringDataArray: Object.entries(props.secret.stringData || {}).map(
    ([key, value]) => ({
      key,
      value,
    })
  ),
};
</script>

<template>
  <VModal
    ref="modal"
    :title="$t('core.formkit.secret.edit_modal.title')"
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
