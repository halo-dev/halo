<script lang="ts" setup>
import {
  VButton,
  Toast,
  IconCheckboxCircle,
  IconErrorWarning,
} from "@halo-dev/components";
import {
  createMessage,
  type FormKitFrameworkContext,
  type FormKitNode,
} from "@formkit/core";
import type { PropType } from "vue";
import { i18n } from "@/locales";
import { axiosInstance } from "@/utils/api-client";
import { setIncompleteMessage } from "./features";
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
 * Handle the submit event.
 *
 * @param e - The event
 *
 * @internal
 */
async function handleSubmit(submitEvent: Event) {
  const node = props.context.node;
  const submitNonce = Math.random();
  node.props._submitNonce = submitNonce;
  submitEvent.preventDefault();
  await node.settled;

  if (node.ledger.value("validating")) {
    // There are validation rules still pending.
    node.store.set(loading);
    await node.ledger.settled("validating");
    node.store.remove("loading");
    // If this was not the same submit event, bail out.
    if (node.props._submitNonce !== submitNonce) return;
  }
  // Set the submitted state on all children
  const setSubmitted = (n: FormKitNode) =>
    n.store.set(
      createMessage({
        key: "submitted",
        value: true,
        visible: false,
      })
    );
  node.walk(setSubmitted);
  setSubmitted(node);

  node.emit("submit-raw");
  if (typeof node.props.onSubmitRaw === "function") {
    node.props.onSubmitRaw(submitEvent, node);
  }

  if (node.ledger.value("blocking")) {
    if (typeof node.props.onSubmitInvalid === "function") {
      node.props.onSubmitInvalid(node);
    }
    // There is still a blocking message in the store.
    if (node.props.incompleteMessage !== false) {
      setIncompleteMessage(node);
    }
  } else {
    verifyActions();
  }
}

/**
 * verify actions
 * @param node
 */
function verifyActions() {
  const node = props.context.node;
  const actions = node.props.actions;
  if (!actions) {
    const message = i18n.global.t(
      "core.formkit.verify_form.no_action_defined",
      {
        label: node.props.submitLabel,
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
    .post(actions, val)
    .then(() => {
      stateMessage.value = {
        state: "success",
      };
      Toast.success(
        i18n.global.t("core.formkit.verify_form.verify_success", {
          label: node.props.submitLabel,
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
  <div :class="context.classes.submit" @click="handleSubmit">
    <VButton
      v-tooltip="stateMessage.message"
      :disabled="context.node.props.submitAttrs.disabled"
      :loading="loadingState"
    >
      {{ context.node.props.submitLabel }}
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
