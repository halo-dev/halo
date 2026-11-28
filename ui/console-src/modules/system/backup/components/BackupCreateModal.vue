<script lang="ts" setup>
import { VButton, VModal, VSpace } from "@halo-dev/components";
import { ref } from "vue";
import SubmitButton from "@/components/button/SubmitButton.vue";
import { useBackup } from "../composables/use-backup";

const { createBackup } = useBackup();

const emit = defineEmits<{
  (event: "close"): void;
}>();

interface BackupFormState {
  password: string;
  password_confirm: string;
}

const modal = ref<InstanceType<typeof VModal> | null>(null);
const formState = ref<BackupFormState>({
  password: "",
  password_confirm: "",
});
const isSubmitting = ref(false);

async function handleCreate() {
  try {
    isSubmitting.value = true;
    await createBackup(formState.value.password || undefined);
    modal.value?.close();
  } catch (error) {
    console.error("Failed to create backup", error);
  } finally {
    isSubmitting.value = false;
  }
}
</script>

<template>
  <VModal
    ref="modal"
    :width="500"
    :title="$t('core.backup.operations.create.title')"
    @close="emit('close')"
  >
    <p class="mb-4 text-sm text-gray-600">
      {{ $t("core.backup.operations.create.description") }}
    </p>
    <FormKit
      id="backup-create-form"
      v-model="formState"
      name="backup-create-form"
      type="form"
      :actions="false"
      :config="{ validationVisibility: 'submit' }"
      @submit="handleCreate"
    >
      <FormKit
        :label="$t('core.backup.operations.create.fields.password.label')"
        :help="$t('core.backup.operations.create.fields.password.help')"
        name="password"
        type="password"
        autocomplete="new-password"
      ></FormKit>
      <FormKit
        :label="
          $t('core.backup.operations.create.fields.confirm_password.label')
        "
        name="password_confirm"
        type="password"
        autocomplete="new-password"
        validation="confirm"
      ></FormKit>
    </FormKit>
    <template #footer>
      <VSpace>
        <SubmitButton
          :loading="isSubmitting"
          type="secondary"
          :text="$t('core.common.buttons.submit')"
          @submit="$formkit.submit('backup-create-form')"
        >
        </SubmitButton>
        <VButton @click="modal?.close()">
          {{ $t("core.common.buttons.cancel_and_shortcut") }}
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>
