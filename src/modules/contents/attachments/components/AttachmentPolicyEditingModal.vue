<script lang="ts" setup>
import { VButton, VModal, VSpace } from "@halo-dev/components";
import type { Policy, PolicyTemplate } from "@halo-dev/api-client";
import cloneDeep from "lodash.clonedeep";
import { computed, ref, watch, watchEffect } from "vue";
import { apiClient, useSettingForm } from "@halo-dev/admin-shared";
import { v4 as uuid } from "uuid";
import { reset, submitForm } from "@formkit/core";
import { useMagicKeys } from "@vueuse/core";
import { setFocus } from "@/formkit/utils/focus";

const props = withDefaults(
  defineProps<{
    visible: boolean;
    policy: Policy | null;
  }>(),
  {
    visible: false,
    policy: null,
  }
);

const emit = defineEmits<{
  (event: "update:visible", visible: boolean): void;
  (event: "close"): void;
}>();

const initialFormState: Policy = {
  spec: {
    displayName: "",
    templateRef: {
      name: "",
    },
    configMapRef: {
      name: "",
    },
  },
  apiVersion: "storage.halo.run/v1alpha1",
  kind: "Policy",
  metadata: {
    name: uuid(),
  },
};

const formState = ref<Policy>(cloneDeep(initialFormState));
const policyTemplate = ref<PolicyTemplate | undefined>();

const settingName = computed(
  () => policyTemplate.value?.spec?.settingRef?.name
);
const configMapName = computed(() => formState.value.spec.configMapRef?.name);

const {
  settings,
  configMapFormData,
  saving,
  handleFetchConfigMap,
  handleFetchSettings,
  handleSaveConfigMap,
  handleReset: handleResetSettingForm,
} = useSettingForm(settingName, configMapName);

const formSchema = computed(() => {
  if (!settings?.value?.spec) {
    return undefined;
  }
  return settings.value.spec.find((item) => item.group === "default")
    ?.formSchema;
});

watchEffect(() => {
  if (settingName.value) {
    handleFetchSettings();
  }
});

watchEffect(() => {
  if (configMapName.value && settings.value) {
    handleFetchConfigMap();
  }
});

const isUpdateMode = computed(() => {
  return !!formState.value.metadata.creationTimestamp;
});

const modalTitle = computed(() => {
  return isUpdateMode.value
    ? `编辑策略：${props.policy?.spec.displayName}`
    : `新增策略：${policyTemplate.value?.spec?.displayName}`;
});

const handleSave = async () => {
  try {
    saving.value = true;

    await handleSaveConfigMap();

    if (isUpdateMode.value) {
      await apiClient.extension.storage.policy.updatestorageHaloRunV1alpha1Policy(
        {
          name: formState.value.metadata.name,
          policy: formState.value,
        }
      );
    } else {
      await apiClient.extension.storage.policy.createstorageHaloRunV1alpha1Policy(
        {
          policy: formState.value,
        }
      );
    }

    onVisibleChange(false);
  } catch (e) {
    console.error("Failed to save attachment policy", e);
  } finally {
    saving.value = false;
  }
};

const handleResetForm = () => {
  formState.value = cloneDeep(initialFormState);
  formState.value.metadata.name = uuid();
  reset("local-policy-form");
};

const { Command_Enter } = useMagicKeys();

watchEffect(() => {
  if (Command_Enter.value && props.visible) {
    submitForm("local-policy-form");
  }
});

watch(
  () => props.visible,
  (visible) => {
    if (visible) {
      setFocus("displayNameInput");
    } else {
      const timer = setTimeout(() => {
        policyTemplate.value = undefined;
        handleResetForm();
        handleResetSettingForm();
        clearTimeout(timer);
      }, 100);
    }
  }
);

watch(
  () => props.policy,
  async (policy) => {
    if (policy) {
      formState.value = cloneDeep(policy);

      // Get policy template
      if (formState.value.spec.templateRef?.name) {
        const { data } =
          await apiClient.extension.storage.policyTemplate.getstorageHaloRunV1alpha1PolicyTemplate(
            {
              name: formState.value.spec.templateRef.name,
            }
          );
        policyTemplate.value = data;
      }
    } else {
      setTimeout(() => {
        policyTemplate.value = undefined;
        handleResetForm();
        handleResetSettingForm();
      }, 100);
    }
  }
);

const onVisibleChange = (visible: boolean) => {
  emit("update:visible", visible);
  if (!visible) {
    emit("close");
  }
};
</script>
<template>
  <VModal
    :title="modalTitle"
    :visible="visible"
    :width="600"
    @update:visible="onVisibleChange"
  >
    <FormKit
      v-if="formSchema && configMapFormData"
      id="local-policy-form"
      v-model="configMapFormData['default']"
      :actions="false"
      :preserve="true"
      type="form"
      :config="{ validationVisibility: 'submit' }"
      @submit="handleSave"
    >
      <FormKit
        id="displayNameInput"
        v-model="formState.spec.displayName"
        label="名称"
        type="text"
        validation="required"
      ></FormKit>
      <FormKitSchema :schema="formSchema" />
    </FormKit>

    <template #footer>
      <VSpace>
        <VButton
          :loading="saving"
          type="secondary"
          @click="$formkit.submit('local-policy-form')"
        >
          保存 ⌘ + ↵
        </VButton>
        <VButton @click="onVisibleChange(false)">取消 Esc</VButton>
      </VSpace>
    </template>
  </VModal>
</template>
