<script lang="ts" setup>
import { Toast, VButton, VSpace } from "@halo-dev/components";

import { VModal } from "@halo-dev/components";
import { nextTick, onMounted, ref } from "vue";
import { useMutation, useQueryClient } from "@tanstack/vue-query";
import { apiClient } from "@/utils/api-client";
import { useUserStore } from "@/stores/user";
import { useIntervalFn } from "@vueuse/shared";
import { computed } from "vue";
import { useI18n } from "vue-i18n";

const queryClient = useQueryClient();
const { t } = useI18n();

const { currentUser, fetchCurrentUser } = useUserStore();

const emit = defineEmits<{
  (event: "close"): void;
}>();

// fixme: refactor VModal component
const shouldRender = ref(false);
const visible = ref(false);

onMounted(() => {
  shouldRender.value = true;
  nextTick(() => {
    visible.value = true;
  });
});

function onClose() {
  visible.value = false;
  setTimeout(() => {
    shouldRender.value = false;
    emit("close");
  }, 200);
}

// count down
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

const email = ref(currentUser?.spec.email);

const { mutate: sendVerifyCode, isLoading: isSending } = useMutation({
  mutationKey: ["send-verify-code"],
  mutationFn: async () => {
    if (!email.value) {
      Toast.error(
        t(
          "core.uc_profile.email_verify_modal.operations.send_code.toast_email_empty"
        )
      );
      throw new Error("email is empty");
    }
    return await apiClient.user.sendEmailVerificationCode({
      emailVerifyRequest: {
        email: email.value,
      },
    });
  },
  onSuccess() {
    Toast.success(
      t("core.uc_profile.email_verify_modal.operations.send_code.toast_success")
    );
    timer.value = 60;
    resume();
  },
});

const sendVerifyCodeButtonText = computed(() => {
  if (isSending.value) {
    return t(
      "core.uc_profile.email_verify_modal.operations.send_code.buttons.sending"
    );
  }
  return isActive.value
    ? t(
        "core.uc_profile.email_verify_modal.operations.send_code.buttons.countdown",
        { timer: timer.value }
      )
    : t("core.uc_profile.email_verify_modal.operations.send_code.buttons.send");
});

const { mutate: verifyEmail, isLoading: isVerifying } = useMutation({
  mutationKey: ["verify-email"],
  mutationFn: async ({ code }: { code: string }) => {
    return await apiClient.user.verifyEmail({
      verifyCodeRequest: {
        code: code,
      },
    });
  },
  onSuccess() {
    Toast.success(
      t("core.uc_profile.email_verify_modal.operations.verify.toast_success")
    );
    queryClient.invalidateQueries({ queryKey: ["user-detail"] });
    fetchCurrentUser();
    onClose();
  },
});

function handleVerify(data: { code: string }) {
  verifyEmail({ code: data.code });
}
</script>

<template>
  <VModal
    v-if="shouldRender"
    v-model:visible="visible"
    :title="
      currentUser?.spec.emailVerified
        ? $t('core.uc_profile.email_verify_modal.titles.modify')
        : $t('core.uc_profile.email_verify_modal.titles.verify')
    "
    @close="onClose"
  >
    <FormKit
      id="email-verify-form"
      type="form"
      name="email-verify-form"
      @submit="handleVerify"
    >
      <FormKit
        v-model="email"
        type="email"
        :label="
          currentUser?.spec.emailVerified
            ? $t('core.uc_profile.email_verify_modal.fields.new_email.label')
            : $t('core.uc_profile.email_verify_modal.fields.email.label')
        "
        name="email"
        validation="required|email"
      ></FormKit>
      <FormKit
        type="number"
        name="code"
        :label="$t('core.uc_profile.email_verify_modal.fields.code.label')"
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
    </FormKit>
    <template #footer>
      <VSpace>
        <VButton
          :loading="isVerifying"
          type="secondary"
          @click="$formkit.submit('email-verify-form')"
        >
          {{ $t("core.common.buttons.verify") }}
        </VButton>
        <VButton @click="emit('close')">
          {{ $t("core.common.buttons.close_and_shortcut") }}
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>
