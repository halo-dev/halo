<script lang="ts" setup>
import {
  VButton,
  Toast,
  IconCheckboxCircle,
  IconErrorWarning,
} from "@halo-dev/components";
import { createMessage, type FormKitFrameworkContext } from "@formkit/core";
import type { PropType } from "vue";
import { i18n } from "@/locales";
import { axiosInstance } from "@/utils/api-client";
import { nextTick, onMounted, ref } from "vue";

const props = defineProps({
  context: {
    type: Object as PropType<FormKitFrameworkContext>,
    required: true,
  },
});

const loadingState = ref<boolean>(false);
const stateMessage = ref<{
  state: "default" | "success" | "error";
  message?: string;
}>({
  state: "default",
});

const loading = createMessage({
  key: "loading",
  value: true,
  visible: false,
});

/**
 * Handle the verify event.
 *
 * @param e - The event
 *
 * @internal
 */
async function handleVerification(event: Event) {
  const node = props.context.node;
  event.preventDefault();
  await node.settled;

  if (!node.ledger.value("blocking")) {
    verifyAction();
  }
}

/**
 * verify action
 * @param node
 */
function verifyAction() {
  const node = props.context.node;
  const action = node.props.action;
  if (!action) {
    const message = i18n.global.t(
      "core.formkit.verification_form.no_action_defined",
      {
        label: node.props.label,
      }
    );
    stateMessage.value = {
      state: "error",
      message,
    };
    Toast.error(message);
    return;
  }
  loadingState.value = true;
  const val = node.value as Record<string, unknown>;
  node.store.set(loading);
  // call onSubmit
  axiosInstance
    .post(action, val)
    .then(() => {
      stateMessage.value = {
        state: "success",
      };
      Toast.success(
        i18n.global.t("core.formkit.verification_form.verify_success", {
          label: node.props.label,
        })
      );
    })
    .catch((error) => {
      stateMessage.value = {
        state: "error",
      };
      const errorResponse = error.response;
      const { title, detail } = errorResponse.data;
      if (title || detail) {
        stateMessage.value.message = detail || title;
      }
    })
    .finally(() => {
      node.store.remove("loading");
      loadingState.value = false;
    });
}

onMounted(() => {
  nextTick(() => {
    const node = props.context.node;
    node.on("commit", () => {
      stateMessage.value = {
        state: "default",
      };
    });
  });
});
</script>

<template>
  <div :class="context.classes.submit" class="py-4" @click="handleVerification">
    <VButton
      v-tooltip="stateMessage.message"
      :disabled="context.node.props.buttonAttrs.disabled"
      :loading="loadingState"
    >
      {{ context.node.props.label }}
      <template v-if="stateMessage.state !== 'default'" #icon>
        <IconCheckboxCircle
          v-if="stateMessage.state === 'success'"
          class="h-full w-full text-green-500"
        />
        <IconErrorWarning
          v-else-if="stateMessage.state === 'error'"
          class="h-full w-full text-red-500"
        />
      </template>
    </VButton>
  </div>
</template>
