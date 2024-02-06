import DialogComponent from "./Dialog.vue";
import { createVNode, render, type Component } from "vue";
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

const dialog: DialogEntry = (userProps: DialogProps) => {
  const props = {
    ...defaultProps,
    ...userProps,
  };

  let container = document.body.querySelector(".dialog-container");
  if (!container) {
    container = document.createElement("div");
    container.className = "dialog-container";
    document.body.appendChild(container);
  }

  const { vnode, container: hostContainer } = createVNodeComponent(
    DialogComponent,
    props
  );

  if (hostContainer.firstElementChild) {
    container.appendChild(hostContainer.firstElementChild);
  }

  if (vnode.component?.props) {
    vnode.component.props.visible = true;
  }

  if (vnode?.props) {
    // close emit

    vnode.props.onClose = () => {
      container?.remove();
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
