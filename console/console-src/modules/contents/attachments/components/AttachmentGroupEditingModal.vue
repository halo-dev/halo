<script lang="ts" setup>
import { Toast, VButton, VModal, VSpace } from "@halo-dev/components";
import SubmitButton from "@/components/button/SubmitButton.vue";
import type { Group } from "@halo-dev/api-client";
import { computed, ref, watch } from "vue";
import cloneDeep from "lodash.clonedeep";
import { apiClient } from "@/utils/api-client";
import { reset } from "@formkit/core";
import { setFocus } from "@/formkit/utils/focus";
import { useI18n } from "vue-i18n";

const props = withDefaults(
  defineProps<{
    visible: boolean;
    group: Group | null;
  }>(),
  {
    visible: false,
    group: null,
  }
);

const emit = defineEmits<{
  (event: "update:visible", visible: boolean): void;
  (event: "close"): void;
}>();

const { t } = useI18n();

const initialFormState: Group = {
  spec: {
    displayName: "",
  },
  apiVersion: "storage.halo.run/v1alpha1",
  kind: "Group",
  metadata: {
    name: "",
    generateName: "attachment-group-",
  },
};

const formState = ref<Group>(cloneDeep(initialFormState));
const saving = ref(false);

const isUpdateMode = computed(() => {
  return !!formState.value.metadata.creationTimestamp;
});

const modalTitle = computed(() => {
  return isUpdateMode.value
    ? t("core.attachment.group_editing_modal.titles.update")
    : t("core.attachment.group_editing_modal.titles.create");
});

const handleSave = async () => {
  try {
    saving.value = true;
    if (isUpdateMode.value) {
      await apiClient.extension.storage.group.updatestorageHaloRunV1alpha1Group(
        {
          name: formState.value.metadata.name,
          group: formState.value,
        }
      );
    } else {
      await apiClient.extension.storage.group.createstorageHaloRunV1alpha1Group(
        {
          group: formState.value,
        }
      );
    }

    Toast.success(t("core.common.toast.save_success"));
    onVisibleChange(false);
  } catch (e) {
    console.error("Failed to save attachment group", e);
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

const handleResetForm = () => {
  formState.value = cloneDeep(initialFormState);
  reset("attachment-group-form");
};

watch(
  () => props.visible,
  (visible) => {
    if (visible) {
      setFocus("displayNameInput");
    } else {
      handleResetForm();
    }
  }
);

watch(
  () => props.group,
  (group) => {
    if (group) {
      formState.value = cloneDeep(group);
    } else {
      handleResetForm();
    }
  }
);
</script>
<template>
  <VModal
    :title="modalTitle"
    :visible="visible"
    :width="500"
    @update:visible="onVisibleChange"
  >
    <FormKit
      id="attachment-group-form"
      name="attachment-group-form"
      :config="{ validationVisibility: 'submit' }"
      type="form"
      :actions="false"
      @submit="handleSave"
    >
      <FormKit
        id="displayNameInput"
        v-model="formState.spec.displayName"
        :label="
          $t('core.attachment.group_editing_modal.fields.display_name.label')
        "
        type="text"
        name="displayName"
        validation="required|length:0,50"
      ></FormKit>
    </FormKit>
    <template #footer>
      <VSpace>
        <SubmitButton
          v-if="visible"
          :loading="saving"
          type="secondary"
          :text="$t('core.common.buttons.submit')"
          @submit="$formkit.submit('attachment-group-form')"
        >
        </SubmitButton>
        <VButton @click="onVisibleChange(false)">
          {{ $t("core.common.buttons.cancel_and_shortcut") }}
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>
