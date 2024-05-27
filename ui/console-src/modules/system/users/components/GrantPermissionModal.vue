<script lang="ts" setup>
import { apiClient } from "@/utils/api-client";
import type { User } from "@halo-dev/api-client";
import { VButton, VModal, VSpace } from "@halo-dev/components";
import SubmitButton from "@/components/button/SubmitButton.vue";
import { ref } from "vue";

const props = withDefaults(
  defineProps<{
    user?: User;
  }>(),
  {
    user: undefined,
  }
);

const emit = defineEmits<{
  (event: "close"): void;
}>();

const modal = ref<InstanceType<typeof VModal> | null>(null);
const selectedRole = ref("");
const isSubmitting = ref(false);

const handleGrantPermission = async () => {
  try {
    isSubmitting.value = true;
    await apiClient.user.grantPermission({
      name: props.user?.metadata.name as string,
      grantRequest: {
        roles: [selectedRole.value],
      },
    });
    modal.value?.close();
  } catch (error) {
    console.error("Failed to grant permission to user", error);
  } finally {
    isSubmitting.value = false;
  }
};
</script>

<template>
  <VModal
    ref="modal"
    :title="$t('core.user.grant_permission_modal.title')"
    :width="500"
    @close="emit('close')"
  >
    <FormKit
      id="grant-permission-form"
      name="grant-permission-form"
      :config="{ validationVisibility: 'submit' }"
      type="form"
      @submit="handleGrantPermission"
    >
      <FormKit
        v-model="selectedRole"
        :label="$t('core.user.grant_permission_modal.fields.role.label')"
        type="roleSelect"
      ></FormKit>
    </FormKit>
    <template #footer>
      <VSpace>
        <SubmitButton
          :loading="isSubmitting"
          type="secondary"
          :text="$t('core.common.buttons.submit')"
          @submit="$formkit.submit('grant-permission-form')"
        >
        </SubmitButton>
        <VButton @click="modal?.close()">
          {{ $t("core.common.buttons.cancel_and_shortcut") }}
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>
