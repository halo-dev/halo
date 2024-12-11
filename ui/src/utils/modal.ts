import { i18n } from "@/locales";
import { VButton, VModal } from "@halo-dev/components";
import { type Component, createApp, h } from "vue";

interface ModalOptions {
  uniqueId?: string;
  title?: string;
  width?: number;
  height?: string;
  centered?: boolean;
  content: Component;
}

export function createHTMLContentModal(options: ModalOptions) {
  if (options.uniqueId) {
    const existingModal = document.getElementById(`modal-${options.uniqueId}`);
    if (existingModal) {
      return;
    }
  }

  const container = document.createElement("div");
  if (options.uniqueId) {
    container.id = `modal-${options.uniqueId}`;
  }

  document.body.appendChild(container);

  const app = createApp({
    setup() {
      const handleClose = () => {
        app.unmount();
        container.remove();
      };

      return () =>
        h(
          VModal,
          {
            title: options.title,
            width: options.width || 500,
            height: options.height,
            centered: options.centered ?? true,
            onClose: handleClose,
            "onUpdate:visible": (value: boolean) => {
              if (!value) handleClose();
            },
          },
          {
            default: () => options.content,
            footer: () =>
              h(
                VButton,
                {
                  onClick: handleClose,
                },
                {
                  default: () =>
                    h("div", i18n.global.t("core.common.buttons.close")),
                }
              ),
          }
        );
    },
  });

  app.mount(container);

  return {
    close: () => {
      app.unmount();
      container.remove();
    },
  };
}
