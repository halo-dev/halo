import { createVNode, render, type Component } from "vue";
import DialogComponent from "./Dialog.vue";
import type { DialogProps } from "./interface";

export type DialogApiProps = Omit<DialogProps, "type" | "visible">;

export type DialogApi = (props?: DialogApiProps) => void;

export interface DialogEntry {
  (props: DialogProps): void;
  info: DialogApi;
  success: DialogApi;
  error: DialogApi;
  warning: DialogApi;
}

const defaultProps: DialogProps = {
  title: "",
  visible: false,
};

const DIALOG_CONTAINER_CLASS = ".dialog-container";
const MODAL_WRAPPER_CLASS = ".modal-wrapper";

function getOrCreateContainer() {
  let container = document.body.querySelector(DIALOG_CONTAINER_CLASS);
  if (!container) {
    container = document.createElement("div");
    container.className = "dialog-container";
    document.body.appendChild(container);
  }
  return container;
}

const dialog: DialogEntry = (userProps: DialogProps) => {
  const props = {
    ...defaultProps,
    ...userProps,
  };

  const container = getOrCreateContainer();

  if (
    props.uniqueId &&
    container.querySelector(`[data-unique-id="${props.uniqueId}"]`)
  ) {
    return;
  }

  const { vnode, container: hostContainer } = createVNodeComponent(
    DialogComponent,
    props
  );

  hostContainer.firstElementChild &&
    container.appendChild(hostContainer.firstElementChild);

  vnode.component?.props && (vnode.component.props.visible = true);

  if (vnode?.props) {
    vnode.props.onClose = () => {
      const modals = container.querySelectorAll(MODAL_WRAPPER_CLASS);

      if (modals.length > 1) {
        hostContainer.firstElementChild?.remove();
      } else {
        container.remove();
      }

      render(null, hostContainer);
    };
  }
};

function createVNodeComponent(
  component: Component,
  props: Record<string, unknown>
) {
  const vnode = createVNode(component, props);
  const container = document.createElement("div");
  render(vnode, container);
  return { vnode, container };
}

dialog.success = (props?: DialogApiProps) =>
  dialog({ ...props, type: "success" });
dialog.info = (props?: DialogApiProps) => dialog({ ...props, type: "info" });
dialog.warning = (props?: DialogApiProps) =>
  dialog({ ...props, type: "warning" });
dialog.error = (props?: DialogApiProps) => dialog({ ...props, type: "error" });

export { dialog as Dialog };
