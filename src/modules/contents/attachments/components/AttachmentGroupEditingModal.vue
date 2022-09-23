<script lang="ts" setup>
import { VButton, VModal, VSpace } from "@halo-dev/components";
import type { Group } from "@halo-dev/api-client";
import { v4 as uuid } from "uuid";
import { computed, ref, watch, watchEffect } from "vue";
import cloneDeep from "lodash.clonedeep";
import { apiClient } from "@/utils/api-client";
import { reset, submitForm } from "@formkit/core";
import { useMagicKeys } from "@vueuse/core";
import { setFocus } from "@/formkit/utils/focus";

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

const initialFormState: Group = {
  spec: {
    displayName: "",
  },
  apiVersion: "storage.halo.run/v1alpha1",
  kind: "Group",
  metadata: {
    name: uuid(),
  },
};

const formState = ref<Group>(cloneDeep(initialFormState));
const saving = ref(false);

const isUpdateMode = computed(() => {
  return !!formState.value.metadata.creationTimestamp;
});

const modalTitle = computed(() => {
  return isUpdateMode.value ? "编辑附件分组" : "新增附件分组";
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
  formState.value.metadata.name = uuid();
  reset("attachment-group-form");
};

const { Command_Enter } = useMagicKeys();

watchEffect(() => {
  if (Command_Enter.value && props.visible) {
    submitForm("attachment-group-form");
  }
});

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
        label="名称"
        type="text"
        validation="required"
      ></FormKit>
    </FormKit>
    <template #footer>
      <VSpace>
        <VButton
          :loading="saving"
          type="secondary"
          @click="$formkit.submit('attachment-group-form')"
        >
          保存 ⌘ + ↵
        </VButton>
        <VButton @click="onVisibleChange(false)">取消 Esc</VButton>
      </VSpace>
    </template>
  </VModal>
</template>
