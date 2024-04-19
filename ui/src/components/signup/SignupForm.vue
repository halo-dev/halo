<script lang="ts" setup>
import { computed, type ComputedRef, onMounted, reactive, ref } from "vue";
import { submitForm } from "@formkit/core";
import { Toast, VButton } from "@halo-dev/components";
import { apiClient } from "@/utils/api-client";
import { useRouteQuery } from "@vueuse/router";
import { useI18n } from "vue-i18n";
import { useMutation } from "@tanstack/vue-query";
import { useIntervalFn } from "@vueuse/shared";
import { useGlobalInfoStore } from "@/stores/global-info";

const { t } = useI18n();

withDefaults(
  defineProps<{
    buttonText?: string;
  }>(),
  {
    buttonText: "core.signup.operations.submit.button",
  }
);

const formState = ref({
  password: "",
  user: {
    apiVersion: "v1alpha1",
    kind: "User",
    metadata: {
      name: "",
    },
    spec: {
      avatar: "",
      displayName: "",
      email: "",
    },
  },
  verifyCode: "",
});
const loading = ref(false);

const emit = defineEmits<{
  (event: "succeed"): void;
}>();

const login = useRouteQuery<string>("login");
const name = useRouteQuery<string>("name");
const globalInfoStore = useGlobalInfoStore();
const signUpCond = reactive({
  mustVerifyEmailOnRegistration: false,
});

onMounted(() => {
  signUpCond.mustVerifyEmailOnRegistration =
    globalInfoStore.globalInfo?.mustVerifyEmailOnRegistration || false;
  if (login.value) {
    formState.value.user.metadata.name = login.value;
  }
  if (name.value) {
    formState.value.user.spec.displayName = name.value;
  }
});
const emailRegex = new RegExp("^[\\w\\-.]+@([\\w-]+\\.)+[\\w-]{2,}$");
const emailValidation: ComputedRef<
  // please see https://github.com/formkit/formkit/blob/bd5cf1c378d358ed3aba7b494713af20b6c909ab/packages/inputs/src/props.ts#L660
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  string | Array<[rule: string, ...args: any]>
> = computed(() => {
  if (signUpCond.mustVerifyEmailOnRegistration)
    return [["required"], ["matches", emailRegex]];
  else return "required|email|length:0,100";
});

const handleSignup = async () => {
  try {
    loading.value = true;

    await apiClient.common.user.signUp({
      signUpRequest: formState.value,
    });

    Toast.success(t("core.signup.operations.submit.toast_success"));

    emit("succeed");
  } catch (error) {
    console.error("Failed to sign up", error);
  } finally {
    loading.value = false;
  }
};

const inputClasses = {
  outer: "!py-3 first:!pt-0 last:!pb-0",
};

// the code below is copied from console/uc-src/modules/profile/components/EmailVerifyModal.vue
const timer = ref(0);
const { pause, resume, isActive } = useIntervalFn(
  () => {
    if (timer.value <= 0) {
      pause();
    } else {
      timer.value--;
    }
  },
  1000,
  {
    immediate: false,
  }
);

const { mutate: sendVerifyCode, isLoading: isSending } = useMutation({
  mutationKey: ["send-verify-code"],
  mutationFn: async () => {
    if (!formState.value.user.spec.email.match(emailRegex)) {
      Toast.error(t("core.signup.fields.email.matchFailed"));
      throw new Error("email is illegal");
    }
    return await apiClient.common.user.sendRegisterVerifyEmail({
      registerVerifyEmailRequest: {
        email: formState.value.user.spec.email,
      },
    });
  },
  onSuccess() {
    Toast.success(
      t("core.signup.fields.verify_code.operations.send_code.toast_success")
    );
    timer.value = 60;
    resume();
  },
});

const sendVerifyCodeButtonText = computed(() => {
  if (isSending.value) {
    return t(
      "core.signup.fields.verify_code.operations.send_code.buttons.sending"
    );
  }
  return isActive.value
    ? t(
        "core.signup.fields.verify_code.operations.send_code.buttons.countdown",
        {
          timer: timer.value,
        }
      )
    : t("core.signup.fields.verify_code.operations.send_code.buttons.send");
});
</script>

<template>
  <FormKit
    id="signup-form"
    name="signup-form"
    :actions="false"
    :classes="{
      form: '!divide-none',
    }"
    type="form"
    :config="{ validationVisibility: 'submit' }"
    @submit="handleSignup"
    @keyup.enter="submitForm('signup-form')"
  >
    <FormKit
      v-model="formState.user.metadata.name"
      name="username"
      :placeholder="$t('core.signup.fields.username.placeholder')"
      :validation-label="$t('core.signup.fields.username.placeholder')"
      :classes="inputClasses"
      :autofocus="true"
      type="text"
      :validation="[
        ['required'],
        ['length', '4', '63'],
        [
          'matches',
          /^[a-z0-9]([-a-z0-9]*[a-z0-9])?(\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*$/,
        ],
      ]"
      :validation-messages="{
        matches: $t('core.user.editing_modal.fields.username.validation'),
      }"
    >
    </FormKit>
    <FormKit
      v-model="formState.user.spec.displayName"
      name="displayName"
      :placeholder="$t('core.signup.fields.display_name.placeholder')"
      :validation-label="$t('core.signup.fields.display_name.placeholder')"
      :classes="inputClasses"
      type="text"
      validation="required"
    >
    </FormKit>
    <FormKit
      v-model="formState.user.spec.email"
      :placeholder="$t('core.signup.fields.email.placeholder')"
      :validation-label="$t('core.signup.fields.email.placeholder')"
      type="email"
      name="email"
      :validation="emailValidation"
      :validation-messages="{
        matches: $t('core.signup.fields.email.matchFailed'),
      }"
    ></FormKit>
    <FormKit
      v-if="signUpCond.mustVerifyEmailOnRegistration"
      v-model="formState.verifyCode"
      type="number"
      name="code"
      :placeholder="$t('core.signup.fields.verify_code.placeholder')"
      :validation-label="$t('core.signup.fields.verify_code.placeholder')"
      validation="required"
    >
      <template #suffix>
        <VButton
          :loading="isSending"
          :disabled="isActive"
          class="rounded-none border-y-0 border-l border-r-0 tabular-nums"
          @click="sendVerifyCode"
        >
          {{ sendVerifyCodeButtonText }}
        </VButton>
      </template>
    </FormKit>
    <FormKit
      v-model="formState.password"
      name="password"
      :placeholder="$t('core.signup.fields.password.placeholder')"
      :validation-label="$t('core.signup.fields.password.placeholder')"
      :classes="inputClasses"
      type="password"
      validation="required:trim|length:5,100|matches:/^\S.*\S$/"
      :validation-messages="{
        matches: $t('core.formkit.validation.trim'),
      }"
    >
    </FormKit>
    <FormKit
      name="password_confirm"
      :placeholder="$t('core.signup.fields.password_confirm.placeholder')"
      :validation-label="$t('core.signup.fields.password_confirm.placeholder')"
      :classes="inputClasses"
      type="password"
      validation="confirm|required:trim|length:5,100|matches:/^\S.*\S$/"
      :validation-messages="{
        matches: $t('core.formkit.validation.trim'),
      }"
    >
    </FormKit>
  </FormKit>
  <VButton
    class="mt-8"
    block
    type="secondary"
    :loading="loading"
    @click="submitForm('signup-form')"
  >
    {{ $t(buttonText) }}
  </VButton>
</template>
