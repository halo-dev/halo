<script lang="ts" setup>
// core libs
import { computed, nextTick, ref, watch } from "vue";
import { apiClient } from "@/utils/api-client";
import type { User } from "@halo-dev/api-client";

// components
import { Toast, VButton, VModal, VSpace } from "@halo-dev/components";
import SubmitButton from "@/components/button/SubmitButton.vue";

// libs
import cloneDeep from "lodash.clonedeep";
import { reset } from "@formkit/core";

// hooks
import { setFocus } from "@/formkit/utils/focus";
import AnnotationsForm from "@/components/form/AnnotationsForm.vue";
import { useUserStore } from "@/stores/user";

const userStore = useUserStore();

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

const initialFormState: User = {
  spec: {
    displayName: "",
    avatar: "",
    email: "",
    phone: "",
    password: "",
    bio: "",
    disabled: false,
    loginHistoryLimit: 0,
  },
  apiVersion: "v1alpha1",
  kind: "User",
  metadata: {
    name: "",
  },
};

const formState = ref<User>(cloneDeep(initialFormState));
const saving = ref(false);

const isUpdateMode = computed(() => {
  return !!formState.value.metadata.creationTimestamp;
});

const creationModalTitle = computed(() => {
  return isUpdateMode.value ? "编辑用户" : "新增用户";
});

watch(
  () => props.visible,
  (visible) => {
    if (visible) {
      setFocus(isUpdateMode.value ? "displayNameInput" : "userNameInput");
    } else {
      handleResetForm();
    }
  }
);

watch(
  () => props.user,
  (user) => {
    if (user) {
      formState.value = cloneDeep(user);
    } else {
      handleResetForm();
    }
  }
);

const onVisibleChange = (visible: boolean) => {
  emit("update:visible", visible);
  if (!visible) {
    emit("close");
  }
};

const handleResetForm = () => {
  formState.value = cloneDeep(initialFormState);
  reset("user-form");
};

const annotationsFormRef = ref<InstanceType<typeof AnnotationsForm>>();

const handleCreateUser = async () => {
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
    saving.value = true;
    if (isUpdateMode.value) {
      if (props.user?.metadata.name === userStore.currentUser?.metadata.name) {
        await apiClient.user.updateCurrentUser({
          user: formState.value,
        });
      } else {
        await apiClient.extension.user.updatev1alpha1User({
          name: formState.value.metadata.name,
          user: formState.value,
        });
      }
    } else {
      await apiClient.extension.user.createv1alpha1User({
        user: formState.value,
      });
    }

    onVisibleChange(false);

    Toast.success("保存成功");
  } catch (e) {
    console.error("Failed to create or update user", e);
  } finally {
    saving.value = false;
  }
};
</script>
<template>
  <VModal
    :title="creationModalTitle"
    :visible="visible"
    :width="700"
    @update:visible="onVisibleChange"
  >
    <FormKit
      id="user-form"
      name="user-form"
      :config="{ validationVisibility: 'submit' }"
      type="form"
      @submit="handleCreateUser"
    >
      <div>
        <div class="md:grid md:grid-cols-4 md:gap-6">
          <div class="md:col-span-1">
            <div class="sticky top-0">
              <span class="text-base font-medium text-gray-900"> 常规 </span>
            </div>
          </div>
          <div class="mt-5 divide-y divide-gray-100 md:col-span-3 md:mt-0">
            <FormKit
              id="userNameInput"
              v-model="formState.metadata.name"
              :disabled="isUpdateMode"
              label="用户名"
              type="text"
              name="name"
              :validation="[
                ['required'],
                ['length:0,63'],
                [
                  'matches',
                  /^[a-z0-9]([-a-z0-9]*[a-z0-9])?(\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*$/,
                ],
              ]"
              :validation-messages="{
                matches: '请输入有效的用户名',
              }"
            ></FormKit>
            <FormKit
              id="displayNameInput"
              v-model="formState.spec.displayName"
              label="显示名称"
              type="text"
              name="displayName"
              validation="required|length:0,50"
            ></FormKit>
            <FormKit
              v-model="formState.spec.email"
              label="电子邮箱"
              type="email"
              name="email"
              validation="required|email|length:0,100"
            ></FormKit>
            <FormKit
              v-model="formState.spec.phone"
              label="手机号"
              type="text"
              name="phone"
              validation="length:0,20"
            ></FormKit>
            <FormKit
              v-model="formState.spec.avatar"
              label="头像"
              type="attachment"
              name="avatar"
              validation="url|length:0,1024"
            ></FormKit>
            <FormKit
              v-model="formState.spec.bio"
              label="描述"
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
          <span class="text-base font-medium text-gray-900"> 元数据 </span>
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
          v-if="visible"
          :loading="saving"
          type="secondary"
          @submit="$formkit.submit('user-form')"
        >
        </SubmitButton>
        <VButton @click="onVisibleChange(false)">取消 Esc</VButton>
      </VSpace>
    </template>
  </VModal>
</template>
