import { enableAutoUnmount, mount } from "@vue/test-utils";
import { OverlayScrollbars } from "overlayscrollbars";
import { afterEach, describe, expect, it, vi } from "vite-plus/test";
import { defineComponent, nextTick, ref } from "vue";
import { VModal } from "../index";

enableAutoUnmount(afterEach);

describe("Modal", () => {
  it("should render", () => {
    expect(VModal).toBeDefined();
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
