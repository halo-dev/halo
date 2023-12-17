<script lang="ts" setup>
import { apiClient } from "@/utils/api-client";
import type { User } from "@halo-dev/api-client";
import { VModal, VSpace, VButton } from "@halo-dev/components";
import SubmitButton from "@/components/button/SubmitButton.vue";
import { ref } from "vue";

const props = withDefaults(
  defineProps<{
    visible: boolean;
    user?: User;
  }>(),
  {
    visible: false,
    user: undefined,
  }
);

const emit = defineEmits<{
  (event: "update:visible", visible: boolean): void;
  (event: "close"): void;
}>();

const selectedRole = ref("");
const saving = ref(false);

const handleGrantPermission = async () => {
  try {
    saving.value = true;
    await apiClient.user.grantPermission({
      name: props.user?.metadata.name as string,
      grantRequest: {
        roles: [selectedRole.value],
      },
    });
    onVisibleChange(false);
  } catch (error) {
    console.error("Failed to grant permission to user", error);
  } finally {
    saving.value = false;
  }
};

const onVisibleChange = (visible: boolean) => {
  emit("update:visible", visible);
  if (!visible) {
    emit("close");
  }
};
</script>

<template>
  <VModal
    :title="$t('core.user.grant_permission_modal.title')"
    :visible="visible"
    :width="500"
    @update:visible="onVisibleChange"
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
          v-if="visible"
          :loading="saving"
          type="secondary"
          :text="$t('core.common.buttons.submit')"
          @submit="$formkit.submit('grant-permission-form')"
        >
        </SubmitButton>
        <VButton @click="onVisibleChange(false)">
          {{ $t("core.common.buttons.cancel_and_shortcut") }}
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>
