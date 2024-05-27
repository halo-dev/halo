<script lang="ts" setup>
// core libs
import { nextTick, ref } from "vue";
import { apiClient } from "@/utils/api-client";
import type { User } from "@halo-dev/api-client";

// components
import { Toast, VButton, VModal, VSpace } from "@halo-dev/components";
import SubmitButton from "@/components/button/SubmitButton.vue";

// libs
import { cloneDeep } from "lodash-es";

// hooks
import AnnotationsForm from "@/components/form/AnnotationsForm.vue";
import { useI18n } from "vue-i18n";
import { useQueryClient } from "@tanstack/vue-query";

const { t } = useI18n();
const queryClient = useQueryClient();

const props = withDefaults(
  defineProps<{
    user: User;
  }>(),
  {}
);

const emit = defineEmits<{
  (event: "close"): void;
}>();

const modal = ref<InstanceType<typeof VModal> | null>(null);
const formState = ref<User>(cloneDeep(props.user));
const isSubmitting = ref(false);

const annotationsFormRef = ref<InstanceType<typeof AnnotationsForm>>();

const handleUpdateUser = async () => {
  annotationsFormRef.value?.handleSubmit();
  await nextTick();

  const { customAnnotations, annotations, customFormInvalid, specFormInvalid } =
    annotationsFormRef.value || {};
  if (customFormInvalid || specFormInvalid) {
    return;
  }

  formState.value.metadata.annotations = {
    ...annotations,
    ...customAnnotations,
  };

  try {
    isSubmitting.value = true;

    await apiClient.extension.user.updateV1alpha1User({
      name: formState.value.metadata.name,
      user: formState.value,
    });

    modal.value?.close();

    queryClient.invalidateQueries({ queryKey: ["users"] });
    queryClient.invalidateQueries({ queryKey: ["user-detail"] });

    Toast.success(t("core.common.toast.save_success"));
  } catch (e) {
    console.error("Failed to create or update user", e);
  } finally {
    isSubmitting.value = false;
  }
};
</script>
<template>
  <VModal
    ref="modal"
    :title="$t('core.user.editing_modal.titles.update')"
    :width="700"
    @close="emit('close')"
  >
    <FormKit
      id="user-form"
      name="user-form"
      :config="{ validationVisibility: 'submit' }"
      type="form"
      @submit="handleUpdateUser"
    >
      <div>
        <div class="md:grid md:grid-cols-4 md:gap-6">
          <div class="md:col-span-1">
            <div class="sticky top-0">
              <span class="text-base font-medium text-gray-900">
                {{ $t("core.user.editing_modal.groups.general") }}
              </span>
            </div>
          </div>
          <div class="mt-5 divide-y divide-gray-100 md:col-span-3 md:mt-0">
            <FormKit
              id="userNameInput"
              v-model="formState.metadata.name"
              :disabled="true"
              :label="$t('core.user.editing_modal.fields.username.label')"
              type="text"
              name="name"
            ></FormKit>
            <FormKit
              id="displayNameInput"
              v-model="formState.spec.displayName"
              :label="$t('core.user.editing_modal.fields.display_name.label')"
              type="text"
              name="displayName"
              validation="required|length:0,50"
            ></FormKit>
            <FormKit
              v-model="formState.spec.email"
              :label="$t('core.user.editing_modal.fields.email.label')"
              type="email"
              name="email"
              validation="required|email|length:0,100"
            ></FormKit>
            <FormKit
              v-model="formState.spec.phone"
              :label="$t('core.user.editing_modal.fields.phone.label')"
              type="text"
              name="phone"
              validation="length:0,20"
            ></FormKit>
            <FormKit
              v-model="formState.spec.bio"
              :label="$t('core.user.editing_modal.fields.bio.label')"
              type="textarea"
              name="bio"
              validation="length:0,2048"
            ></FormKit>
          </div>
        </div>
      </div>
    </FormKit>

    <div class="py-5">
      <div class="border-t border-gray-200"></div>
    </div>

    <div class="md:grid md:grid-cols-4 md:gap-6">
      <div class="md:col-span-1">
        <div class="sticky top-0">
          <span class="text-base font-medium text-gray-900">
            {{ $t("core.user.editing_modal.groups.annotations") }}
          </span>
        </div>
      </div>
      <div class="mt-5 divide-y divide-gray-100 md:col-span-3 md:mt-0">
        <AnnotationsForm
          :key="formState.metadata.name"
          ref="annotationsFormRef"
          :value="formState.metadata.annotations"
          kind="User"
          group=""
        />
      </div>
    </div>
    <template #footer>
      <VSpace>
        <SubmitButton
          :loading="isSubmitting"
          type="secondary"
          :text="$t('core.common.buttons.submit')"
          @submit="$formkit.submit('user-form')"
        >
        </SubmitButton>
        <VButton @click="modal?.close()">
          {{ $t("core.common.buttons.cancel_and_shortcut") }}
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>
