import { VButton } from "@halo-dev/components";
import { createVNode, h, render, type VNode } from "vue";
import type { NotificationType } from "../components/ActionNotification.vue";
import ActionNotification from "../components/ActionNotification.vue";

export interface ActionNotificationAction {
  label: string;
  type?: "primary" | "secondary" | "danger" | "default";
  onClick: () => void | Promise<void>;
}

export interface ActionNotificationProps {
  type?: NotificationType;
  title?: string;
  message?: string;
  closable?: boolean;
  actions?: ActionNotificationAction[];
  duration?: number;
  onClose?: () => void;
}

export interface ActionNotificationInstance {
  id: string;
  vnode: VNode;
  close: () => void;
}

let index = 0;
const instances: ActionNotificationInstance[] = [];

function createActionNotification(
  props: ActionNotificationProps
): ActionNotificationInstance {
  const id = "action-notification-" + index++;

  let container = document.body.querySelector(
    ".action-notification-container"
  ) as HTMLElement | null;
  if (!container) {
    container = document.createElement("div");
    container.className = "action-notification-container";
    Object.assign(container.style, {
      position: "fixed",
      bottom: "20px",
      right: "20px",
      zIndex: "9999",
      display: "flex",
      flexDirection: "column",
      gap: "12px",
      pointerEvents: "none",
    });
    document.body.appendChild(container);
  }

  const wrapper = document.createElement("div");
  wrapper.style.pointerEvents = "auto";
  wrapper.id = id;

  const vnode = createVNode(ActionNotification, {
    ...props,
    onClose: () => {
      close();
      props.onClose?.();
    },
  });

  // Add actions slot if actions are provided
  if (props.actions && props.actions.length > 0) {
    vnode.children = {
      actions: () =>
        props.actions!.map((action) =>
          h(
            VButton,
            {
              size: "sm",
              type: action.type || "default",
              onClick: async () => {
                await action.onClick();
                // Don't auto-close on action click - let the action handler decide
              },
            },
            () => action.label
          )
        ),
    };
  }

  render(vnode, wrapper);
  container.appendChild(wrapper);

  const close = () => {
    const idx = instances.findIndex((inst) => inst.id === id);
    if (idx >= 0) {
      instances.splice(idx, 1);
    }

    render(null, wrapper);
    wrapper.remove();

    if (instances.length === 0) {
      container?.remove();
    }
  };

  // Auto-close after duration
  if (props.duration && props.duration > 0) {
    setTimeout(close, props.duration);
  }

  const instance: ActionNotificationInstance = {
    id,
    vnode,
    close,
  };

  instances.push(instance);
  return instance;
}

export const ActionNotificationManager = {
  show: createActionNotification,
  closeAll: () => {
    instances.forEach((inst) => inst.close());
  },
};
