import { mount } from "@vue/test-utils";
import {
  afterEach,
  beforeEach,
  describe,
  expect,
  it,
  vi,
} from "vite-plus/test";
import { defineComponent, h, nextTick, ref } from "vue";
import { VButton } from "../../button";
import { VPopover } from "../index";

class ResizeObserverMock {
  constructor(readonly callback: ResizeObserverCallback) {}

  observe() {
    this.callback([], this as unknown as ResizeObserver);
  }

  unobserve() {}

  disconnect() {}
}

function mountPopover(
  props: Record<string, unknown> = {},
  options: { disabledTrigger?: boolean } = {}
) {
  return mount(VPopover, {
    props,
    slots: {
      default: `<button type="button"${
        options.disabledTrigger ? " disabled" : ""
      }>Open</button>`,
      content: `<div data-testid="popover-content">Rich content</div>`,
    },
    attachTo: document.body,
  });
}

function getContent() {
  return document.body.querySelector("[data-testid='popover-content']");
}

// Reka's Presence waits for a tick before unmounting the content.
async function settle() {
  await nextTick();
  await nextTick();
}

beforeEach(() => {
  vi.stubGlobal("ResizeObserver", ResizeObserverMock);
});

afterEach(() => {
  document.body.innerHTML = "";
  vi.unstubAllGlobals();
});

describe("Popover", () => {
  it("opens on trigger click and marks the trigger expanded", async () => {
    const wrapper = mountPopover();
    const trigger = wrapper.get("button");

    expect(trigger.attributes("aria-haspopup")).toBe("dialog");
    expect(trigger.attributes("aria-expanded")).toBe("false");
    expect(getContent()).toBeNull();

    await trigger.trigger("click");
    await settle();

    expect(wrapper.emitted("update:open")?.[0]).toEqual([true]);
    expect(trigger.attributes("aria-expanded")).toBe("true");
    expect(getContent()?.textContent).toBe("Rich content");

    wrapper.unmount();
  }, 15_000);

  it("reflects a controlled open prop", async () => {
    const wrapper = mountPopover({ open: false });

    expect(getContent()).toBeNull();

    await wrapper.setProps({ open: true });
    await settle();

    expect(getContent()).not.toBeNull();

    await wrapper.setProps({ open: false });
    await settle();

    expect(getContent()).toBeNull();

    wrapper.unmount();
  });

  it("closes on Escape", async () => {
    const wrapper = mountPopover();

    await wrapper.get("button").trigger("click");
    await settle();
    expect(getContent()).not.toBeNull();

    document.dispatchEvent(
      new KeyboardEvent("keydown", { key: "Escape", bubbles: true })
    );
    await settle();

    expect(wrapper.emitted("update:open")?.at(-1)).toEqual([false]);
    expect(getContent()).toBeNull();

    wrapper.unmount();
  });

  it("exposes show and hide", async () => {
    const wrapper = mountPopover();

    (wrapper.vm as unknown as { show: () => void }).show();
    await settle();

    expect(getContent()).not.toBeNull();

    (wrapper.vm as unknown as { hide: () => void }).hide();
    await settle();

    expect(getContent()).toBeNull();

    wrapper.unmount();
  });

  it("keeps the type prop of a VButton trigger", async () => {
    const wrapper = mount(VPopover, {
      slots: {
        default: () => h(VButton, { type: "primary" }, () => "Open"),
        content: `<div>Rich content</div>`,
      },
      attachTo: document.body,
    });

    expect(wrapper.get("button").classes()).toContain("btn-primary");

    wrapper.unmount();
  });

  it("does not open when disabled", async () => {
    const wrapper = mountPopover({ disabled: true }, { disabledTrigger: true });

    await wrapper.get("button").trigger("click");
    await settle();

    expect(wrapper.emitted("update:open")).toBeUndefined();
    expect(getContent()).toBeNull();

    wrapper.unmount();
  });

  it("renders trigger and content slot props", async () => {
    const Host = defineComponent({
      components: { VPopover },
      setup() {
        const open = ref(false);

        return { open };
      },
      template: `
        <VPopover v-model:open="open">
          <button type="button">
            {{ open ? "Open" : "Closed" }}
          </button>
          <template #content="{ close }">
            <button data-testid="popover-content" type="button" @click="close">
              Close
            </button>
          </template>
        </VPopover>
      `,
    });

    const wrapper = mount(Host, { attachTo: document.body });

    await wrapper.get("button").trigger("click");
    await settle();

    expect(wrapper.get("button").text()).toBe("Open");

    getContent()?.dispatchEvent(new MouseEvent("click", { bubbles: true }));
    await settle();

    expect(wrapper.get("button").text()).toBe("Closed");

    wrapper.unmount();
  });
});
