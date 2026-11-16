import { enableAutoUnmount, mount } from "@vue/test-utils";
import { OverlayScrollbars } from "overlayscrollbars";
import { afterEach, describe, expect, it, vi } from "vite-plus/test";
import { defineComponent, nextTick, ref } from "vue";
import { VModal } from "../index";

enableAutoUnmount(afterEach);

afterEach(() => {
  vi.restoreAllMocks();
});

describe("Modal", () => {
  it("should render", () => {
    expect(VModal).toBeDefined();
  });

  describe("accessibility", () => {
    it("labels the dialog with its title", () => {
      const wrapper = mount(VModal, {
        props: { visible: true, title: "Hello Halo" },
      });
      const dialog = wrapper.get(".modal-wrapper");
      const labelledby = dialog.attributes("aria-labelledby");

      expect(labelledby).toBeTruthy();
      expect(wrapper.get(`[id="${labelledby}"]`).text()).toBe("Hello Halo");
    });

    it("keeps a native summary in the tab sequence", async () => {
      vi.spyOn(HTMLElement.prototype, "getClientRects").mockReturnValue([
        { width: 10, height: 10 },
      ] as unknown as DOMRectList);

      const wrapper = mount(VModal, {
        attachTo: document.body,
        props: { visible: false },
        slots: {
          default: `
            <button id="before">Before</button>
            <details>
              <summary id="more">More</summary>
              <div>Hidden content</div>
            </details>
          `,
        },
      });

      await wrapper.setProps({ visible: true });
      await nextTick();
      await nextTick();

      const before = document.getElementById("before") as HTMLButtonElement;
      before.focus();

      const event = new KeyboardEvent("keydown", {
        key: "Tab",
        bubbles: true,
        cancelable: true,
      });
      before.dispatchEvent(event);

      expect(event.defaultPrevented).toBe(false);
    });

    it.each(["-1", "-2", "-1x", "-1.5", " -2 "])(
      "ignores a summary with tabindex %j in the tab order",
      async (tabindex) => {
        vi.spyOn(HTMLElement.prototype, "getClientRects").mockReturnValue([
          { width: 10, height: 10 },
        ] as unknown as DOMRectList);

        const wrapper = mount(VModal, {
          attachTo: document.body,
          props: { visible: false },
          slots: {
            default: `
              <button id="before">Before</button>
              <details>
                <summary id="more" tabindex="${tabindex}">More</summary>
                <div>Hidden content</div>
              </details>
            `,
          },
        });

        await wrapper.setProps({ visible: true });
        await nextTick();
        await nextTick();

        const before = document.getElementById("before") as HTMLButtonElement;
        before.focus();

        const event = new KeyboardEvent("keydown", {
          key: "Tab",
          bubbles: true,
          cancelable: true,
        });
        before.dispatchEvent(event);

        expect(event.defaultPrevented).toBe(true);
      }
    );

    it.each(["invalid", "", " -1", "-0"])(
      "keeps a focusable element with tabindex %j in the tab sequence",
      async (tabindex) => {
        vi.spyOn(HTMLElement.prototype, "getClientRects").mockReturnValue([
          { width: 10, height: 10 },
        ] as unknown as DOMRectList);

        const wrapper = mount(VModal, {
          attachTo: document.body,
          props: { visible: false },
          slots: {
            default: `
              <button id="before">Before</button>
              <div id="edit" contenteditable="true" tabindex="${tabindex}">Edit</div>
            `,
          },
        });

        await wrapper.setProps({ visible: true });
        await nextTick();
        await nextTick();

        const edit = document.getElementById("edit") as HTMLElement;
        Object.defineProperty(edit, "tabIndex", {
          configurable: true,
          get: () => -1,
        });

        const before = document.getElementById("before") as HTMLButtonElement;
        before.focus();

        const event = new KeyboardEvent("keydown", {
          key: "Tab",
          bubbles: true,
          cancelable: true,
        });
        before.dispatchEvent(event);

        expect(event.defaultPrevented).toBe(false);
      }
    );

    it("restores focus to the opener after closing", async () => {
      const opener = document.createElement("button");
      document.body.appendChild(opener);
      opener.focus();

      const wrapper = mount(VModal, {
        attachTo: document.body,
        props: { visible: false, title: "Hello Halo" },
      });

      await wrapper.setProps({ visible: true });
      await nextTick();
      await nextTick();

      expect(document.activeElement).toBe(
        wrapper.get(".modal-wrapper").element
      );

      await wrapper.setProps({ visible: false });
      await nextTick();

      expect(document.activeElement).toBe(opener);

      opener.remove();
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
          })
        );
        const body = wrapper.get(".modal-body").element as HTMLElement;

        for (let opening = 0; opening < 2; opening++) {
          visible.value = true;
          await nextTick();
          await vi.waitFor(() => {
            expect(OverlayScrollbars.valid(OverlayScrollbars(body))).toBe(true);
          });
          const viewport = OverlayScrollbars(body)!.elements().viewport;
          expect(viewport.textContent).toBe(
            initialCondition ? "Content" : hiddenText
          );

          for (const condition of [!initialCondition, initialCondition]) {
            showContent.value = condition;
            await nextTick();
            expect(viewport.textContent).toBe(
              condition ? "Content" : hiddenText
            );
          }

          visible.value = false;
          await nextTick();
          expect(OverlayScrollbars(body)).toBeUndefined();
        }
      }
    );
  });
});
