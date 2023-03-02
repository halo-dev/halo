import ToastComponent from "./Toast.vue";
import { createVNode, render, type Component, type VNode } from "vue";
import type { ToastProps } from "./interface";

export type ToastApiProps = Omit<ToastProps, "type" | "content">;

export interface ToastInstance {
  id: string;
  vnode: VNode;
}

export type ToastApi = (
  content: string,
  props?: ToastApiProps
) => ToastInstance;

export interface ToastEntry {
  (props: ToastProps): ToastInstance;
  info: ToastApi;
  success: ToastApi;
  error: ToastApi;
  warning: ToastApi;
}

let index = 0;

const instances: ToastInstance[] = [];

const defaultProps: ToastProps = {
  frozenOnHover: true,
  duration: 3000,
  count: 0,
};

const toast: ToastEntry = (userProps: ToastProps) => {
  const id = "toast-" + index++;

  const props = {
    ...defaultProps,
    ...userProps,
    id,
  };

  let container = document.body.querySelector(".toast-container");
  if (!container) {
    container = document.createElement("div");
    container.className = "toast-container";
    document.body.appendChild(container);
  }

  // Grouping toasts
  if (instances.length > 0) {
    const instance = instances.find((item) => {
      const { vnode } = item;
      if (vnode?.props) {
        return (
          vnode.props.content === props.content &&
          vnode.props.type === props.type
        );
      }
      return undefined;
    });

    if (instance?.vnode.component?.props) {
      (instance.vnode.component.props.count as number) += 1;
      index = instances.length - 1;
      return instance;
    }
  }

  const { vnode, container: hostContainer } = createVNodeComponent(
    ToastComponent,
    props
  );

  if (hostContainer.firstElementChild) {
    container.appendChild(hostContainer.firstElementChild);
  }

  if (vnode?.props) {
    // close emit
    vnode.props.onClose = () => {
      removeInstance(id);
      render(null, hostContainer);
    };
  }

  const instance = {
    id,
    vnode,
    close: () => {
      vnode?.component?.exposed?.close();
    },
  };

  instances.push(instance);
  return instance;
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

function removeInstance(id: string) {
  const index = instances.findIndex((instance) => instance.id === id);
  if (index >= 0) {
    instances.splice(index, 1);

    if (instances.length === 0) {
      const container = document.body.querySelector(".toast-container");
      container?.remove();
    }
  }
}

toast.success = (content: string, props?: ToastApiProps) =>
  toast({ ...props, type: "success", content });
toast.info = (content: string, props?: ToastApiProps) =>
  toast({ ...props, type: "info", content });
toast.warning = (content: string, props?: ToastApiProps) =>
  toast({ ...props, type: "warning", content });
toast.error = (content: string, props?: ToastApiProps) =>
  toast({ ...props, type: "error", content });

export { toast as Toast };
