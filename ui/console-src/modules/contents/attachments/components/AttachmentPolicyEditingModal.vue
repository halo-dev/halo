<script lang="ts" setup>
import SubmitButton from "@/components/button/SubmitButton.vue";
import { setFocus } from "@/formkit/utils/focus";
import { apiClient } from "@/utils/api-client";
import { useSettingFormConvert } from "@console/composables/use-setting-form";
import type { Policy } from "@halo-dev/api-client";
import { Toast, VButton, VLoading, VModal, VSpace } from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import { cloneDeep } from "lodash-es";
import { computed, onMounted, ref, toRaw, toRefs } from "vue";
import { useI18n } from "vue-i18n";

const props = withDefaults(
  defineProps<{
    policy?: Policy;
    templateName?: string;
  }>(),
  {
    policy: undefined,
    templateName: undefined,
  }
);

const { policy } = toRefs(props);

const emit = defineEmits<{
  (event: "close"): void;
}>();

const { t } = useI18n();

const modal = ref<InstanceType<typeof VModal> | null>(null);

const formState = ref<Policy>({
  spec: {
    displayName: "",
    templateName: "",
    configMapName: "",
  },
  apiVersion: "storage.halo.run/v1alpha1",
  kind: "Policy",
  metadata: {
    name: "",
    generateName: "attachment-policy-",
  },
});

const isUpdateMode = !!props.policy;

onMounted(async () => {
  if (props.policy) {
    formState.value = cloneDeep(props.policy);
  }
  if (props.templateName) {
    formState.value.spec.templateName = props.templateName;
  }

  setFocus("displayNameInput");
});

const { data: policyTemplate } = useQuery({
  queryKey: [
    "core:attachment:policy-template",
    formState.value.spec.templateName,
  ],
  cacheTime: 0,
  queryFn: async () => {
    const { data } =
      await apiClient.extension.storage.policyTemplate.getStorageHaloRunV1alpha1PolicyTemplate(
        {
          name: formState.value.spec.templateName,
        }
      );
    return data;
  },
  retry: 0,
  enabled: computed(() => !!formState.value.spec.templateName),
});

const { data: setting, isLoading } = useQuery({
  queryKey: [
    "core:attachment:policy-template:setting",
    policyTemplate.value?.spec?.settingName,
  ],
  cacheTime: 0,
  queryFn: async () => {
    if (!policyTemplate.value?.spec?.settingName) {
      throw new Error("No setting found");
    }

    const { data } = await apiClient.extension.setting.getV1alpha1Setting({
      name: policyTemplate.value.spec.settingName,
    });

    return data;
  },
  retry: 0,
  enabled: computed(() => !!policyTemplate.value?.spec?.settingName),
});

const { data: configMap } = useQuery({
  queryKey: [
    "core:attachment:policy-template:configMap",
    policy.value?.spec.configMapName,
  ],
  cacheTime: 0,
  initialData: {
    data: {},
    apiVersion: "v1alpha1",
    kind: "ConfigMap",
    metadata: {
      generateName: "configMap-",
      name: "",
    },
  },
  retry: 0,
  queryFn: async () => {
    if (!policy.value?.spec.configMapName) {
      throw new Error("No configMap found");
    }
    const { data } = await apiClient.extension.configMap.getV1alpha1ConfigMap({
      name: policy.value?.spec.configMapName,
    });
    return data;
  },
  enabled: computed(() => !!policy.value?.spec.configMapName),
});

const { configMapFormData, formSchema, convertToSave } = useSettingFormConvert(
  setting,
  configMap,
  ref("default")
);

const submitting = ref(false);

const handleSave = async () => {
  try {
    submitting.value = true;

    const configMapToUpdate = convertToSave();

    if (isUpdateMode) {
      await apiClient.extension.configMap.updateV1alpha1ConfigMap({
        name: configMap.value.metadata.name,
        configMap: configMapToUpdate,
      });

      await apiClient.extension.storage.policy.updateStorageHaloRunV1alpha1Policy(
        {
          name: formState.value.metadata.name,
          policy: formState.value,
        }
      );
    } else {
      const { data: newConfigMap } =
        await apiClient.extension.configMap.createV1alpha1ConfigMap({
          configMap: configMapToUpdate,
        });

      formState.value.spec.configMapName = newConfigMap.metadata.name;
      await apiClient.extension.storage.policy.createStorageHaloRunV1alpha1Policy(
        {
          policy: formState.value,
        }
      );
    }

    Toast.success(t("core.common.toast.save_success"));
    modal.value?.close();
  } catch (e) {
    console.error("Failed to save attachment policy", e);
  } finally {
    submitting.value = false;
  }
};

const modalTitle = props.policy
  ? t("core.attachment.policy_editing_modal.titles.update", {
      policy: props.policy?.spec.displayName,
    })
  : t("core.attachment.policy_editing_modal.titles.create", {
      policy_template: policyTemplate.value?.spec?.displayName,
    });
</script>
<template>
  <VModal
    ref="modal"
    mount-to-body
    :title="modalTitle"
    :width="600"
    @close="emit('close')"
  >
    <div>
      <VLoading v-if="isLoading" />
      <template v-else>
        <FormKit
          v-if="formSchema && configMapFormData"
          id="attachment-policy-form"
          v-model="configMapFormData['default']"
          name="attachment-policy-form"
          :actions="false"
          :preserve="true"
          type="form"
          :config="{ validationVisibility: 'submit' }"
          @submit="handleSave"
        >
          <FormKit
            id="displayNameInput"
            v-model="formState.spec.displayName"
            :label="
              $t(
                'core.attachment.policy_editing_modal.fields.display_name.label'
              )
            "
            type="text"
            name="displayName"
            validation="required|length:0,50"
          ></FormKit>
          <FormKitSchema
            :schema="toRaw(formSchema)"
            :data="configMapFormData['default']"
          />
        </FormKit>
      </template>
    </div>

    <template #footer>
      <VSpace>
        <SubmitButton
          :loading="submitting"
          type="secondary"
          :text="$t('core.common.buttons.submit')"
          @submit="$formkit.submit('attachment-policy-form')"
        >
        </SubmitButton>
        <VButton @click="modal?.close()">
          {{ $t("core.common.buttons.cancel_and_shortcut") }}
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>
