<script lang="ts" setup>
import { Toast, VAlert, VButton, VModal, VSpace } from "@halo-dev/components";
import { useMutation, useQuery } from "@tanstack/vue-query";
import { useIntervalFn } from "@vueuse/shared";
import { computed, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import {
  cancelSudoConfirm,
  completeSudoConfirm,
  confirmSudo,
  fetchSudoStatus,
  sendSudoCode,
  useSudoConfirmModalState,
  type SudoMethod,
} from "@/composables/use-sudo-confirm";

const { t } = useI18n();
const { visible } = useSudoConfirmModalState();

const modal = ref<InstanceType<typeof VModal> | null>(null);
const selectedMethod = ref("");
const code = ref("");
const confirmed = ref(false);

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
  { immediate: false }
);

const {
  data: status,
  isLoading,
  error,
} = useQuery({
  queryKey: ["sudo-status"],
  queryFn: async () => {
    const { data } = await fetchSudoStatus();
    return data;
  },
  enabled: visible,
  staleTime: 30_000,
  refetchOnWindowFocus: false,
});

const methods = computed<SudoMethod[]>(() => status.value?.methods ?? []);

const currentMethod = computed(() =>
  methods.value.find((method) => method.name === selectedMethod.value)
);

const methodOptions = computed(() =>
  methods.value.map((method) => ({
    label: methodLabel(method),
    value: method.name,
  }))
);

const modalVisible = computed(
  () => visible.value && !isLoading.value && !!status.value
);

watch(methods, (value) => {
  if (!value.length) {
    return;
  }
  if (!value.some((method) => method.name === selectedMethod.value)) {
    selectedMethod.value = value[0].name;
  }
});

watch(visible, (value) => {
  if (!value) {
    return;
  }
  confirmed.value = false;
  code.value = "";
  timer.value = 0;
  pause();
});

watch(error, (value) => {
  if (value && visible.value) {
    Toast.error(t("core.common.toast.unknown_error"));
    cancelSudoConfirm();
  }
});

const { mutate: sendCode, isLoading: isSending } = useMutation({
  mutationKey: ["sudo-send-code"],
  mutationFn: async () => {
    await sendSudoCode(selectedMethod.value);
  },
  onSuccess() {
    Toast.success(t("core.sudo.operations.send_code.toast_success"));
    timer.value = 60;
    resume();
  },
});

const { mutate: confirm, isLoading: isConfirming } = useMutation({
  mutationKey: ["sudo-confirm"],
  mutationFn: async () => {
    await confirmSudo(selectedMethod.value, String(code.value));
  },
  onSuccess() {
    Toast.success(t("core.sudo.operations.confirm.toast_success"));
    confirmed.value = true;
    modal.value?.close();
  },
});

const sendButtonText = computed(() => {
  if (isSending.value) {
    return t("core.sudo.operations.send_code.buttons.sending");
  }
  return isActive.value
    ? t("core.sudo.operations.send_code.buttons.countdown", {
        timer: timer.value,
      })
    : t("core.sudo.operations.send_code.buttons.send");
});

function methodLabel(method: SudoMethod) {
  const key = `core.sudo.methods.${method.name}`;
  const translated = t(key);
  if (translated !== key) {
    return method.maskedTarget
      ? `${translated} (${method.maskedTarget})`
      : translated;
  }
  return method.maskedTarget
    ? `${method.name} (${method.maskedTarget})`
    : method.name;
}

function onSubmit() {
  confirm();
}

function onClose() {
  if (confirmed.value) {
    completeSudoConfirm();
    return;
  }
  cancelSudoConfirm();
}

function onSendCode() {
  sendCode();
}
</script>

<template>
  <VModal
    v-if="modalVisible"
    ref="modal"
    class="sudo-confirm-modal"
    :width="500"
    :title="$t('core.sudo.modal.title')"
    @close="onClose"
  >
    <VAlert
      :title="$t('core.common.text.tip')"
      :description="$t('core.sudo.modal.alert')"
      type="warning"
      :closable="false"
    />
    <FormKit
      id="sudo-confirm-form"
      type="form"
      name="sudo-confirm-form"
      :actions="false"
      @submit="onSubmit"
    >
      <FormKit
        v-if="methods.length > 1"
        v-model="selectedMethod"
        type="radio"
        name="method"
        :label="$t('core.sudo.fields.method.label')"
        :options="methodOptions"
        validation="required"
      />
      <FormKit
        v-model="code"
        type="text"
        inputmode="numeric"
        name="code"
        :label="$t('core.sudo.fields.code.label')"
        :help="
          currentMethod?.sendable
            ? $t('core.sudo.fields.code.help_email', {
                target: currentMethod.maskedTarget || '',
              })
            : $t('core.sudo.fields.code.help_totp')
        "
        validation="required"
      >
        <template #suffix>
          <VButton
            v-show="currentMethod?.sendable"
            :loading="isSending"
            :disabled="isActive"
            class="rounded-none border-y-0 border-l border-r-0 tabular-nums"
            @click="onSendCode"
          >
            {{ sendButtonText }}
          </VButton>
        </template>
      </FormKit>
    </FormKit>
    <template #footer>
      <VSpace>
        <VButton
          :loading="isConfirming"
          type="secondary"
          @click="$formkit.submit('sudo-confirm-form')"
        >
          {{ $t("core.common.buttons.verify") }}
        </VButton>
        <VButton @click="modal?.close()">
          {{ $t("core.common.buttons.cancel") }}
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>

<style>
.sudo-confirm-modal.modal-wrapper {
  z-index: 3000;
}
</style>
