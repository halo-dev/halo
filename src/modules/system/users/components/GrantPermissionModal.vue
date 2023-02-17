<script lang="ts" setup>
import { rbacAnnotations } from "@/constants/annotations";
import { apiClient } from "@/utils/api-client";
import type { FormKitOptionsList } from "@formkit/inputs";
import type { User } from "@halo-dev/api-client";
import { VModal, VSpace, VButton } from "@halo-dev/components";
import SubmitButton from "@/components/button/SubmitButton.vue";
import { computed, ref } from "vue";
import { useFetchRole } from "../../roles/composables/use-role";

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

const { roles } = useFetchRole();
const rolesMap = computed<FormKitOptionsList>(() => {
  return [
    {
      label: "请选择角色",
      value: "",
    },
    ...roles.value.map((role) => {
      return {
        label:
          role.metadata?.annotations?.[rbacAnnotations.DISPLAY_NAME] ||
          role.metadata.name,
        value: role.metadata?.name,
      };
    }),
  ];
});

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
    title="分类角色"
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
        :options="rolesMap"
        label="角色"
        type="select"
      ></FormKit>
    </FormKit>
    <template #footer>
      <VSpace>
        <SubmitButton
          v-if="visible"
          :loading="saving"
          type="secondary"
          @submit="$formkit.submit('grant-permission-form')"
        >
        </SubmitButton>
        <VButton @click="onVisibleChange(false)">取消 Esc</VButton>
      </VSpace>
    </template>
  </VModal>
</template>
