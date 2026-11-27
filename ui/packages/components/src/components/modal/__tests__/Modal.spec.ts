import { enableAutoUnmount, flushPromises, mount } from "@vue/test-utils";
import { afterEach, describe, expect, it, vi } from "vite-plus/test";
import { defineComponent, nextTick, ref } from "vue";
import { VModal } from "../index";

enableAutoUnmount(afterEach);

afterEach(() => {
  vi.useRealTimers();
  vi.restoreAllMocks();
});

describe("Modal", () => {
  it("should render", () => {
    expect(VModal).toBeDefined();
  });

  describe("accessibility", () => {
    it("opens when initially controlled as visible", async () => {
      const wrapper = mount(VModal, {
        attachTo: document.body,
        props: { visible: true, title: "Modal title" },
      });

      await nextTick();

      expect(wrapper.get(".modal-wrapper").isVisible()).toBe(true);
      expect(wrapper.get(".modal-content").attributes("data-state")).toBe(
        "open"
      );
    });

    it("labels the dialog with its title", async () => {
      const wrapper = mount(VModal, {
        attachTo: document.body,
        props: { visible: true, title: "Modal title" },
      });
      const dialog = wrapper.get(".modal-content");
      const labelledby = dialog.attributes("aria-labelledby");

      expect(labelledby).toBeTruthy();
      expect(wrapper.get(`[id="${labelledby}"]`).text()).toBe("Modal title");
    });

    it("uses aria-label when no visible title is rendered", async () => {
      const wrapper = mount(VModal, {
        attachTo: document.body,
        props: { visible: true, ariaLabel: "Search" },
      });
      const dialog = wrapper.get(".modal-content");
      const labelledby = dialog.attributes("aria-labelledby");

      expect(labelledby).toBeTruthy();
      expect(wrapper.get(`[id="${labelledby}"]`).text()).toBe("Search");
    });

    it("moves focus into the dialog and keeps it there", async () => {
      const outside = document.createElement("button");
      document.body.appendChild(outside);

      const wrapper = mount(VModal, {
        attachTo: document.body,
        props: { visible: true, title: "Modal title" },
        slots: {
          default: '<button id="inside">Inside</button>',
        },
      });

      await nextTick();
      await nextTick();

      const dialog = wrapper.get(".modal-content").element;
      await vi.waitFor(() => {
        expect(dialog.contains(document.activeElement)).toBe(true);
      });

      outside.focus();
      await nextTick();

      expect(dialog.contains(document.activeElement)).toBe(true);

      outside.remove();
    });

    it("does not focus the first header action when opening", async () => {
      const wrapper = mount(VModal, {
        attachTo: document.body,
        props: { visible: true, title: "Modal title" },
        slots: {
          actions: '<button id="previous">Previous</button>',
          default: '<button id="inside">Inside</button>',
        },
      });

      await nextTick();
      await nextTick();

      const dialog = wrapper.get(".modal-content").element;

      await vi.waitFor(() => {
        expect(document.activeElement).toBe(dialog);
      });

      expect(wrapper.get("#previous").element).not.toBe(document.activeElement);
    });

    it("restores focus to the opener after closing", async () => {
      const opener = document.createElement("button");
      document.body.appendChild(opener);
      opener.focus();

      const wrapper = mount(VModal, {
        attachTo: document.body,
        props: { visible: false, title: "Modal title" },
      });

      await wrapper.setProps({ visible: true });
      await nextTick();
      await nextTick();

      const dialog = wrapper.get(".modal-content").element;
      await vi.waitFor(() => {
        expect(dialog.contains(document.activeElement)).toBe(true);
      });

      await wrapper.setProps({ visible: false });
      await vi.waitFor(() => {
        expect(document.activeElement).toBe(opener);
      });

      opener.remove();
    });

    it("does not close on Escape when close-on-esc is disabled", async () => {
      const wrapper = mount(VModal, {
        attachTo: document.body,
        props: {
          visible: true,
          title: "Modal title",
          closeOnEsc: false,
        },
      });

      await nextTick();
      vi.useFakeTimers();

      window.dispatchEvent(
        new KeyboardEvent("keydown", {
          key: "Escape",
          bubbles: true,
          cancelable: true,
        })
      );
      await nextTick();
      vi.advanceTimersByTime(250);

      expect(wrapper.emitted("update:visible")).toBeUndefined();
      expect(wrapper.emitted("close")).toBeUndefined();
    });

    it("closes on Escape by default", async () => {
      const wrapper = mount(VModal, {
        attachTo: document.body,
        props: { visible: true, title: "Modal title" },
      });

      await nextTick();
      vi.useFakeTimers();

      window.dispatchEvent(
        new KeyboardEvent("keydown", {
          key: "Escape",
          bubbles: true,
          cancelable: true,
        })
      );
      await nextTick();
      vi.advanceTimersByTime(250);

      expect(wrapper.emitted("update:visible")).toEqual([[false]]);
      expect(wrapper.emitted("close")).toHaveLength(1);
    });

    it("keeps the modal open when before-close rejects the request", async () => {
      const beforeClose = vi.fn(() => false);
      const wrapper = mount(VModal, {
        attachTo: document.body,
        props: {
          visible: true,
          title: "Modal title",
          beforeClose,
        },
      });

      await nextTick();
      vi.useFakeTimers();

      wrapper.vm.close();
      await flushPromises();
      vi.advanceTimersByTime(250);

      expect(beforeClose).toHaveBeenCalledTimes(1);
      expect(wrapper.emitted("update:visible")).toBeUndefined();
      expect(wrapper.get(".modal-content").attributes("data-state")).toBe(
        "open"
      );
    });

    it("closes after an async before-close succeeds", async () => {
      const beforeClose = vi.fn(async () => true);
      const wrapper = mount(VModal, {
        attachTo: document.body,
        props: {
          visible: true,
          title: "Modal title",
          beforeClose,
        },
      });

      await nextTick();
      vi.useFakeTimers();

      wrapper.vm.close();
      await flushPromises();
      vi.advanceTimersByTime(250);

      expect(beforeClose).toHaveBeenCalledTimes(1);
      expect(wrapper.emitted("update:visible")).toEqual([[false]]);
    });

    it("can hide the built-in close button", async () => {
      const wrapper = mount(VModal, {
        attachTo: document.body,
        props: { visible: true, title: "Modal title", showClose: false },
      });

      await nextTick();

      expect(wrapper.find('button[aria-label="Close"]').exists()).toBe(false);
    });
  });

  describe("layer dismissal", () => {
    async function waitForPointerDownListener() {
      // Reka registers the document-level pointerdown listener on the next
      // macrotask so the click that opened the dialog cannot dismiss it.
      await new Promise((resolve) => setTimeout(resolve, 0));
    }

    function pressBackdrop() {
      document.body.dispatchEvent(
        new MouseEvent("pointerdown", {
          bubbles: true,
          cancelable: true,
          button: 0,
        })
      );
    }

    it("nudges the modal and keeps it open when the layer is not closable", async () => {
      const wrapper = mount(VModal, {
        attachTo: document.body,
        props: { visible: true, title: "Modal title" },
      });

      await nextTick();
      await waitForPointerDownListener();
      vi.useFakeTimers();

      pressBackdrop();
      await nextTick();

      const content = wrapper.get(".modal-content");
      expect(content.classes()).toContain("modal-focus");
      expect(content.attributes("data-state")).toBe("open");

      vi.advanceTimersByTime(350);
      await nextTick();

      expect(content.classes()).not.toContain("modal-focus");
      expect(wrapper.emitted("update:visible")).toBeUndefined();
      expect(wrapper.emitted("close")).toBeUndefined();
    });

    it("closes when the layer is clicked and layer-closable is enabled", async () => {
      const wrapper = mount(VModal, {
        attachTo: document.body,
        props: {
          visible: true,
          title: "Modal title",
          layerClosable: true,
        },
      });

      await nextTick();
      await waitForPointerDownListener();
      vi.useFakeTimers();

      pressBackdrop();
      await flushPromises();
      vi.advanceTimersByTime(250);
      await flushPromises();

      expect(wrapper.emitted("update:visible")).toEqual([[false]]);
      expect(wrapper.emitted("close")).toHaveLength(1);
    });

    it("keeps the modal open while interacting with a teleported dropdown popper", async () => {
      const wrapper = mount(VModal, {
        attachTo: document.body,
        props: {
          visible: true,
          title: "Modal title",
          layerClosable: true,
        },
      });

      await nextTick();
      await waitForPointerDownListener();
      vi.useFakeTimers();

      const popper = document.createElement("div");
      popper.className =
        "v-popper__popper v-popper--theme-dropdown v-popper__popper--shown";
      const item = document.createElement("div");
      popper.appendChild(item);
      document.body.appendChild(popper);

      item.dispatchEvent(
        new MouseEvent("pointerdown", {
          bubbles: true,
          cancelable: true,
          button: 0,
        })
      );
      await flushPromises();
      vi.advanceTimersByTime(250);
      await flushPromises();

      expect(wrapper.emitted("update:visible")).toBeUndefined();
      expect(wrapper.emitted("close")).toBeUndefined();
      expect(wrapper.get(".modal-content").classes()).not.toContain(
        "modal-focus"
      );

      popper.remove();
    });
  });

  describe("scroll container", () => {
    it("locks page scrolling while open", async () => {
      const wrapper = mount(VModal, {
        attachTo: document.body,
        props: { visible: false, title: "Modal title" },
      });

      await wrapper.setProps({ visible: true });
      await nextTick();
      await nextTick();

      expect(document.body.style.overflow).toBe("hidden");

      await wrapper.setProps({ visible: false });

      await vi.waitFor(() => {
        expect(document.body.style.overflow).toBe("");
      });
    });

    it("renders the default slot inside the scroll viewport", async () => {
      const wrapper = mount(VModal, {
        attachTo: document.body,
        props: { visible: false },
        slots: { default: "Content" },
      });

      await wrapper.setProps({ visible: true });
      await nextTick();

      expect(
        wrapper.get(".modal-body [data-reka-scroll-area-viewport]").text()
      ).toBe("Content");
    });

    it("applies body classes to the padded scroll content", async () => {
      const wrapper = mount(VModal, {
        attachTo: document.body,
        props: { visible: true, bodyClass: ["!p-0"] },
        slots: { default: "Content" },
      });

      await nextTick();

      expect(wrapper.get(".modal-body-content").classes()).toContain("!p-0");
    });
  });

  describe.each([
    {
      name: "v-if",
      template: '<div v-if="showContent">Content</div>',
      hiddenText: "",
    },
    {
      name: "v-if/v-else",
      template:
        '<div v-if="showContent">Content</div><div v-else>Loading</div>',
      hiddenText: "Loading",
    },
    {
      name: "v-if fragment",
      template:
        '<template v-if="showContent"><span>Con</span><span>tent</span></template>',
      hiddenText: "",
    },
  ])("$name in the default slot", ({ template, hiddenText }) => {
    it.each([false, true])(
      "should update after initialization and reopening with initial condition %s",
      async (initialCondition) => {
        const visible = ref(false);
        const showContent = ref(initialCondition);
        const wrapper = mount(
          defineComponent({
            components: { VModal },
            setup: () => ({ visible, showContent }),
            template: `<VModal :visible="visible">${template}</VModal>`,
          }),
          { attachTo: document.body }
        );
        let viewport: Element | undefined;

        for (let opening = 0; opening < 2; opening++) {
          visible.value = true;
          await nextTick();

          const currentViewport = wrapper.get(
            ".modal-body [data-reka-scroll-area-viewport]"
          );

          if (viewport) {
            // The slot nodes must stay inside the same viewport when the modal
            // is reopened instead of being re-parented.
            expect(currentViewport.element).toBe(viewport);
          }

          viewport = currentViewport.element;

          expect(currentViewport.text()).toBe(
            initialCondition ? "Content" : hiddenText
          );

          for (const condition of [!initialCondition, initialCondition]) {
            showContent.value = condition;
            await nextTick();
            expect(currentViewport.text()).toBe(
              condition ? "Content" : hiddenText
            );
          }

          visible.value = false;
          await nextTick();
        }
      }
    );
  });
});
