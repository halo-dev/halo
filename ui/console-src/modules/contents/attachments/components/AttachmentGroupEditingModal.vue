<script lang="ts" setup>
import SubmitButton from "@/components/button/SubmitButton.vue";
import { setFocus } from "@/formkit/utils/focus";
import { apiClient } from "@/utils/api-client";
import type { Group } from "@halo-dev/api-client";
import { Toast, VButton, VModal, VSpace } from "@halo-dev/components";
import { cloneDeep } from "lodash-es";
import { onMounted, ref } from "vue";
import { useI18n } from "vue-i18n";

const props = withDefaults(
  defineProps<{
    group?: Group;
  }>(),
  {
    group: undefined,
  }
);

const emit = defineEmits<{
  (event: "close"): void;
}>();

const { t } = useI18n();

const modal = ref<InstanceType<typeof VModal> | null>(null);
const formState = ref<Group>({
  spec: {
    displayName: "",
  },
  apiVersion: "storage.halo.run/v1alpha1",
  kind: "Group",
  metadata: {
    name: "",
    generateName: "attachment-group-",
  },
});
const isSubmitting = ref(false);

const modalTitle = props.group
  ? t("core.attachment.group_editing_modal.titles.update")
  : t("core.attachment.group_editing_modal.titles.create");

const handleSave = async () => {
  try {
    isSubmitting.value = true;
    if (props.group) {
      await apiClient.extension.storage.group.updateStorageHaloRunV1alpha1Group(
        {
          name: formState.value.metadata.name,
          group: formState.value,
        }
      );
    } else {
      await apiClient.extension.storage.group.createStorageHaloRunV1alpha1Group(
        {
          group: formState.value,
        }
      );
    }

    Toast.success(t("core.common.toast.save_success"));
    modal.value?.close();
  } catch (e) {
    console.error("Failed to save attachment group", e);
  } finally {
    isSubmitting.value = false;
  }
};

onMounted(() => {
  setFocus("displayNameInput");

  if (props.group) {
    formState.value = cloneDeep(props.group);
  }
});
</script>
<template>
  <VModal
    ref="modal"
    mount-to-body
    :title="modalTitle"
    :width="500"
    @close="emit('close')"
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
          :loading="isSubmitting"
          type="secondary"
          :text="$t('core.common.buttons.submit')"
          @submit="$formkit.submit('attachment-group-form')"
        >
        </SubmitButton>
        <VButton @click="modal?.close()">
          {{ $t("core.common.buttons.cancel_and_shortcut") }}
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>
