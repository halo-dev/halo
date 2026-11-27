import { createVNode, render } from "vue";
import DialogComponent from "./Dialog.vue";
import type { DialogProps } from "./types";

export type DialogApiProps = Omit<DialogProps, "type" | "visible">;

export interface DialogInstance {
  /**
   * Closes the dialog without calling `onCancel`.
   */
  close: () => void;
  /**
   * Removes the dialog immediately, skipping the leave animation.
   */
  destroy: () => void;
}

export type DialogApi = (props?: DialogApiProps) => DialogInstance | undefined;

export interface DialogEntry {
  (props: DialogProps): DialogInstance | undefined;
  info: DialogApi;
  success: DialogApi;
  error: DialogApi;
  warning: DialogApi;
  /**
   * Destroys every open dialog created through the manager API.
   */
  destroyAll: () => void;
}

const LEAVE_ANIMATION_MS = 200;

const defaultProps: DialogProps = {
  visible: false,
};

interface ActiveDialog {
  uniqueId?: string;
  host: HTMLElement;
  dispose: () => void;
}

const activeDialogs = new Set<ActiveDialog>();

function disposeDialog(active: ActiveDialog) {
  render(null, active.host);
  active.host.remove();
  activeDialogs.delete(active);
}

const dialog: DialogEntry = (userProps: DialogProps) => {
  const { uniqueId } = userProps;

  if (
    uniqueId &&
    Array.from(activeDialogs).some((item) => item.uniqueId === uniqueId)
  ) {
    return;
  }

  const host = document.createElement("div");
  document.body.appendChild(host);

  let disposed = false;
  let leaveTimer: ReturnType<typeof setTimeout> | undefined;

  const dispose = () => {
    if (disposed) {
      return;
    }

    disposed = true;

    if (leaveTimer !== undefined) {
      clearTimeout(leaveTimer);
      leaveTimer = undefined;
    }

    disposeDialog(active);
  };

  const active: ActiveDialog = {
    uniqueId,
    host,
    dispose,
  };

  const instance: DialogInstance = {
    close: () => {
      const exposed = vnode.component?.exposed as
        | { close?: () => void }
        | undefined;

      if (exposed?.close) {
        exposed.close();
      } else {
        dispose();
      }
    },
    destroy: dispose,
  };

  const vnode = createVNode(DialogComponent, {
    ...defaultProps,
    ...userProps,
    visible: true,
    onClose: () => {
      if (leaveTimer !== undefined) {
        clearTimeout(leaveTimer);
      }

      leaveTimer = setTimeout(dispose, LEAVE_ANIMATION_MS);
    },
  });

  activeDialogs.add(active);
  render(vnode, host);

  return instance;
};

dialog.success = (props?: DialogApiProps) =>
  dialog({ ...props, type: "success" });
dialog.info = (props?: DialogApiProps) => dialog({ ...props, type: "info" });
dialog.warning = (props?: DialogApiProps) =>
  dialog({ ...props, type: "warning" });
dialog.error = (props?: DialogApiProps) => dialog({ ...props, type: "error" });

dialog.destroyAll = () => {
  Array.from(activeDialogs).forEach((item) => item.dispose());
};

export { dialog as Dialog };
