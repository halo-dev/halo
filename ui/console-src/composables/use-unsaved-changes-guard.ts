import { getNode } from "@formkit/core";
import { Dialog } from "@halo-dev/components";
import { useEventListener } from "@vueuse/core";
import { cloneDeep, isEqual } from "es-toolkit";
import {
  nextTick,
  onMounted,
  onUnmounted,
  ref,
  toValue,
  watch,
  type MaybeRef,
} from "vue";
import { useI18n } from "vue-i18n";
import { onBeforeRouteLeave, onBeforeRouteUpdate } from "vue-router";

/**
 * Tracks unsaved changes in a FormKit form and shows a confirmation dialog
 * when the user attempts to leave.
 *
 * Dirty state is updated by subscribing to the FormKit node `commit` event.
 * After a successful business save, call `markSaved` explicitly to reset.
 *
 * @param formId - FormKit form id, corresponding to
 * `<FormKit :id="formId" type="form">`.
 *
 * @example
 * ```vue
 * <script setup>
 * useUnsavedChangesGuard(group);
 * </script>
 * ```
 */
export function useUnsavedChangesGuard(formId: MaybeRef<string>) {
  const { t } = useI18n();

  const isDirty = ref(false);

  let commitReceipt: string | undefined;
  let subscribedNodeId: string | undefined;
  let initialFormValue: unknown;
  let retryTimer: ReturnType<typeof setTimeout> | undefined;

  const clearRetryTimer = () => {
    if (retryTimer) {
      clearTimeout(retryTimer);
      retryTimer = undefined;
    }
  };

  const markClean = () => {
    isDirty.value = false;
  };

  const updateDirtyState = (currentValue: unknown) => {
    isDirty.value = !isEqual(currentValue, initialFormValue);
  };

  const unsubscribeFromNode = () => {
    clearRetryTimer();
    if (!subscribedNodeId) {
      return;
    }
    const node = getNode(subscribedNodeId);
    if (commitReceipt) {
      node?.off(commitReceipt);
      commitReceipt = undefined;
    }
  };

  const subscribeToNode = async (id: string) => {
    await nextTick();

    if (!id) {
      return;
    }

    if (subscribedNodeId !== id) {
      unsubscribeFromNode();
      markClean();
      subscribedNodeId = undefined;
      initialFormValue = undefined;
    }

    const node = getNode(id);
    if (!node) {
      clearRetryTimer();
      retryTimer = setTimeout(() => {
        void subscribeToNode(id);
      }, 100);
      return;
    }

    clearRetryTimer();
    subscribedNodeId = id;

    if (initialFormValue === undefined) {
      initialFormValue = cloneDeep(node.value);
    }

    // Re-subscribe to avoid duplicate listeners
    if (commitReceipt) {
      node.off(commitReceipt);
    }
    commitReceipt = node.on("commit", () => {
      updateDirtyState(node.value);
    });
  };

  const markSaved = (savedValue: unknown) => {
    initialFormValue = cloneDeep(savedValue);
    markClean();
  };

  onMounted(() => {
    const id = toValue(formId);
    if (id) {
      subscribeToNode(id);
    }
  });

  // Re-subscribe when formId changes (e.g. tab switching)
  watch(
    () => toValue(formId),
    (newId) => {
      if (newId) {
        subscribeToNode(newId);
        return;
      }
      unsubscribeFromNode();
      initialFormValue = undefined;
      markClean();
    }
  );

  onUnmounted(() => {
    unsubscribeFromNode();
    initialFormValue = undefined;
  });

  const showLeaveDialog = (next: (valid?: boolean) => void) => {
    Dialog.warning({
      title: t("core.common.unsaved_changes_guard.title"),
      description: t("core.common.unsaved_changes_guard.description"),
      confirmText: t("core.common.unsaved_changes_guard.confirm_text"),
      confirmType: "danger",
      cancelText: t("core.common.buttons.cancel"),
      onConfirm: () => {
        next();
      },
      onCancel: () => {
        next(false);
      },
    });
  };

  // Fires when the same route component is reused with different params (e.g. tab switching)
  onBeforeRouteUpdate((_, __, next) => {
    if (!isDirty.value) {
      next();
      return;
    }
    showLeaveDialog(next);
  });

  // Fires when leaving current route record (e.g. navigating to dashboard)
  onBeforeRouteLeave((_, __, next) => {
    if (!isDirty.value) {
      next();
      return;
    }
    showLeaveDialog(next);
  });

  useEventListener(window, "beforeunload", (e: BeforeUnloadEvent) => {
    if (isDirty.value) {
      e.preventDefault();
      e.returnValue = "";
    }
  });

  return { isDirty, markSaved };
}
