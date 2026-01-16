<script lang="ts" setup>
import { secretAnnotations } from "@/constants/annotations";
import { coreApiClient } from "@halo-dev/api-client";
import { VButton, VModal, VSpace } from "@halo-dev/components";
import { useMutation, useQueryClient } from "@tanstack/vue-query";
import { ref } from "vue";
import { Q_KEY } from "../composables/use-secrets-fetch";
import type { RequiredKey, SecretFormState } from "../types";
import SecretForm from "./SecretForm.vue";

const queryClient = useQueryClient();

withDefaults(
  defineProps<{
    formState?: SecretFormState;
    requiredKeys?: RequiredKey[];
  }>(),
  { formState: undefined, requiredKeys: () => [] }
);

const emit = defineEmits<{
  (event: "close"): void;
  (event: "created", secretName: string): void;
}>();

const modal = ref<InstanceType<typeof VModal> | null>(null);

const { mutate, isLoading } = useMutation({
  mutationKey: ["create-secret"],
  mutationFn: async ({ data }: { data: SecretFormState }) => {
    const stringData = data.stringDataArray
      .filter(({ key }) => !!key)
      .reduce(
        (acc, { key, value }) => {
          acc[key] = value || "";
          return acc;
        },
        {} as Record<string, string>
      );

    return await coreApiClient.secret.createSecret({
      secret: {
        metadata: {
          generateName: "secret-",
          name: "",
          annotations: {
            [secretAnnotations.DESCRIPTION]: data.description || "",
          },
        },
        kind: "Secret",
        apiVersion: "v1alpha1",
        type: "Opaque",
        stringData: stringData,
      },
    });
  },
  onSuccess(data) {
    queryClient.invalidateQueries({ queryKey: Q_KEY() });
    emit("created", data.data.metadata.name);
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
    mount-to-body
    :title="$t('core.formkit.secret.creation_modal.title')"
    :width="600"
    :centered="false"
    @close="emit('close')"
  >
    <SecretForm
      :form-state="formState"
      :required-keys="requiredKeys"
      @submit="onSubmit"
    />

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
