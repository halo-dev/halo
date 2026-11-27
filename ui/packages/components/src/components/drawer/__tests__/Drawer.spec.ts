import { enableAutoUnmount, mount, type VueWrapper } from "@vue/test-utils";
import {
  afterEach,
  beforeEach,
  describe,
  expect,
  it,
  vi,
} from "vite-plus/test";
import { nextTick } from "vue";
import { VDrawer } from "../index";

enableAutoUnmount(afterEach);

function stubInsetViewport(matches: boolean) {
  vi.stubGlobal(
    "matchMedia",
    vi.fn().mockReturnValue({
      matches,
      addEventListener: vi.fn(),
      removeEventListener: vi.fn(),
    })
  );
}

function getPanel() {
  return document.body.querySelector<HTMLElement>(".drawer-panel");
}

function updateVisibleEvents(wrapper: VueWrapper) {
  return (wrapper.emitted("update:visible") ?? []).map((event) => event[0]);
}

interface PointerInit {
  clientX: number;
  clientY: number;
}

function dispatchPointer(type: string, target: EventTarget, init: PointerInit) {
  const event = new Event(type, { bubbles: true, cancelable: true });

  Object.assign(event, {
    ...init,
    pointerId: 1,
    pointerType: "mouse",
    isPrimary: true,
    button: 0,
  });

  target.dispatchEvent(event);
  return event;
}

describe("Drawer", () => {
  beforeEach(() => {
    Object.defineProperty(window, "innerHeight", {
      configurable: true,
      value: 800,
    });
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("does not render while closed", async () => {
    mount(VDrawer, { props: { visible: false } });
    await nextTick();

    expect(getPanel()).toBeNull();
  });

  it("renders a modal dialog when visible", async () => {
    mount(VDrawer, {
      attachTo: document.body,
      props: { visible: true, title: "Settings" },
    });
    await nextTick();

    const panel = getPanel();

    expect(panel).not.toBeNull();
    expect(panel?.getAttribute("role")).toBe("dialog");
    expect(document.body.querySelector(".drawer-overlay")).not.toBeNull();
  });

  it("labels the dialog with its title", async () => {
    mount(VDrawer, {
      attachTo: document.body,
      props: { visible: true, title: "Settings" },
    });
    await nextTick();

    const labelledby = getPanel()?.getAttribute("aria-labelledby");

    expect(labelledby).toBeTruthy();
    expect(document.getElementById(labelledby!)?.textContent?.trim()).toBe(
      "Settings"
    );
  });

  it("falls back to the accessible label when there is no visible title", async () => {
    mount(VDrawer, {
      attachTo: document.body,
      props: { visible: true, ariaLabel: "Filters" },
    });
    await nextTick();

    const labelledby = getPanel()?.getAttribute("aria-labelledby");

    expect(document.getElementById(labelledby!)?.textContent?.trim()).toBe(
      "Filters"
    );
  });

  it("describes the dialog with its description", async () => {
    mount(VDrawer, {
      attachTo: document.body,
      props: {
        visible: true,
        title: "Settings",
        description: "Manage your workspace",
      },
    });
    await nextTick();

    const describedby = getPanel()?.getAttribute("aria-describedby");

    expect(document.getElementById(describedby!)?.textContent?.trim()).toBe(
      "Manage your workspace"
    );
  });

  it("applies the placement", async () => {
    mount(VDrawer, {
      attachTo: document.body,
      props: { visible: true, placement: "left" },
    });
    await nextTick();

    const panel = getPanel();

    expect(panel?.className.split(/\s+/).includes("drawer-panel--left")).toBe(
      true
    );
    expect(panel?.dataset.placement).toBe("left");
  });

  it("emits update:visible when the close button is clicked", async () => {
    const wrapper = mount(VDrawer, {
      attachTo: document.body,
      props: { defaultVisible: true, title: "Settings" },
    });
    await nextTick();

    getPanel()?.querySelector<HTMLButtonElement>(".drawer-close")!.click();
    await nextTick();

    expect(updateVisibleEvents(wrapper)).toContain(false);
    await vi.waitFor(() => {
      expect(getPanel()).toBeNull();
    });
  });

  it("keeps the drawer open when beforeClose returns false", async () => {
    const beforeClose = vi.fn(() => false);
    const wrapper = mount(VDrawer, {
      attachTo: document.body,
      props: { defaultVisible: true, beforeClose },
    });
    await nextTick();

    getPanel()?.querySelector<HTMLButtonElement>(".drawer-close")!.click();

    await vi.waitFor(() => {
      expect(beforeClose).toHaveBeenCalledTimes(1);
    });

    expect(updateVisibleEvents(wrapper)).not.toContain(false);
    expect(getPanel()).not.toBeNull();
  });

  it("closes after beforeClose allows it", async () => {
    const beforeClose = vi.fn(async () => true);
    const wrapper = mount(VDrawer, {
      attachTo: document.body,
      props: { defaultVisible: true, beforeClose },
    });
    await nextTick();

    getPanel()?.querySelector<HTMLButtonElement>(".drawer-close")!.click();

    await vi.waitFor(() => {
      expect(updateVisibleEvents(wrapper)).toContain(false);
    });
  });

  it("closes on Escape", async () => {
    const wrapper = mount(VDrawer, {
      attachTo: document.body,
      props: { defaultVisible: true },
    });
    await nextTick();

    window.dispatchEvent(
      new KeyboardEvent("keydown", {
        key: "Escape",
        bubbles: true,
        cancelable: true,
      })
    );
    await nextTick();

    expect(updateVisibleEvents(wrapper)).toContain(false);
  });

  it("keeps the drawer open on Escape when closeOnEsc is false", async () => {
    const wrapper = mount(VDrawer, {
      attachTo: document.body,
      props: { defaultVisible: true, closeOnEsc: false },
    });
    await nextTick();

    window.dispatchEvent(
      new KeyboardEvent("keydown", {
        key: "Escape",
        bubbles: true,
        cancelable: true,
      })
    );
    await nextTick();

    expect(updateVisibleEvents(wrapper)).not.toContain(false);
  });

  it("closes when the overlay is clicked", async () => {
    const wrapper = mount(VDrawer, {
      attachTo: document.body,
      props: { defaultVisible: true },
    });
    await nextTick();
    await new Promise((resolve) => setTimeout(resolve, 0));

    document.body.dispatchEvent(
      new Event("pointerdown", { bubbles: true, cancelable: true })
    );

    await vi.waitFor(() => {
      expect(updateVisibleEvents(wrapper)).toContain(false);
    });
  });

  it("ignores outside pointer events when layerClosable is false", async () => {
    const wrapper = mount(VDrawer, {
      attachTo: document.body,
      props: { defaultVisible: true, layerClosable: false },
    });
    await nextTick();
    await new Promise((resolve) => setTimeout(resolve, 0));

    document.body.dispatchEvent(
      new Event("pointerdown", { bubbles: true, cancelable: true })
    );
    await new Promise((resolve) => setTimeout(resolve, 20));
    await nextTick();

    expect(updateVisibleEvents(wrapper)).not.toContain(false);
  });

  it("keeps the drawer open when interacting with a teleported dropdown popper", async () => {
    const wrapper = mount(VDrawer, {
      attachTo: document.body,
      props: { defaultVisible: true, layerClosable: true },
    });
    await nextTick();
    await new Promise((resolve) => setTimeout(resolve, 0));

    const popper = document.createElement("div");
    popper.className =
      "v-popper__popper v-popper--theme-dropdown v-popper__popper--shown";
    const item = document.createElement("div");
    popper.appendChild(item);
    document.body.appendChild(popper);

    item.dispatchEvent(
      new Event("pointerdown", { bubbles: true, cancelable: true })
    );
    await new Promise((resolve) => setTimeout(resolve, 20));
    await nextTick();

    expect(updateVisibleEvents(wrapper)).not.toContain(false);

    popper.remove();
  });

  it("renders a non-modal drawer without an overlay", async () => {
    mount(VDrawer, {
      attachTo: document.body,
      props: { visible: true, modal: false },
    });
    await nextTick();

    expect(getPanel()).not.toBeNull();
    expect(document.body.querySelector(".drawer-overlay")).toBeNull();
  });

  it("applies the configured size", async () => {
    mount(VDrawer, {
      attachTo: document.body,
      props: { visible: true, placement: "right", size: 480 },
    });
    await nextTick();

    expect(getPanel()?.style.width).toBe("480px");
  });

  it("adds the inset class when requested", async () => {
    stubInsetViewport(true);
    mount(VDrawer, {
      attachTo: document.body,
      props: { visible: true, inset: true, placement: "right", size: 420 },
    });
    await nextTick();

    expect(getPanel()?.classList.contains("drawer-panel--inset")).toBe(true);
  });

  it("clamps snap points to the inset viewport", async () => {
    stubInsetViewport(true);
    mount(VDrawer, {
      attachTo: document.body,
      props: {
        visible: true,
        inset: true,
        placement: "bottom",
        snapPoints: [0.5, 1],
      },
    });
    await nextTick();

    expect(getPanel()?.style.height).toBe("768px");
  });

  it("keeps snap points at full viewport below the inset breakpoint", async () => {
    stubInsetViewport(false);
    mount(VDrawer, {
      attachTo: document.body,
      props: {
        visible: true,
        inset: true,
        placement: "bottom",
        snapPoints: [0.5, 1],
      },
    });
    await nextTick();

    expect(getPanel()?.style.height).toBe("800px");
  });

  it("uses the largest snap point by default", async () => {
    mount(VDrawer, {
      attachTo: document.body,
      props: {
        visible: true,
        placement: "bottom",
        snapPoints: [0.5, 1],
      },
    });
    await nextTick();

    expect(getPanel()?.style.height).toBe("800px");
  });

  it("honours a controlled snap point", async () => {
    mount(VDrawer, {
      attachTo: document.body,
      props: {
        visible: true,
        placement: "bottom",
        snapPoints: [0.5, 1],
        snapPoint: 0.5,
      },
    });
    await nextTick();

    expect(getPanel()?.style.height).toBe("400px");
  });

  it("emits update:snapPoint when snapping programmatically", async () => {
    const wrapper = mount(VDrawer, {
      attachTo: document.body,
      props: {
        defaultVisible: true,
        placement: "bottom",
        snapPoints: [0.5, 1],
      },
    });
    await nextTick();

    (wrapper.vm as unknown as { snapTo: (index: number) => void }).snapTo(0);
    await nextTick();

    expect(wrapper.emitted("update:snapPoint")?.[0]).toEqual([0.5]);
    expect(getPanel()?.style.height).toBe("400px");
  });

  it("does not apply snap points to non-bottom placements", async () => {
    mount(VDrawer, {
      attachTo: document.body,
      props: {
        visible: true,
        placement: "right",
        snapPoints: [0.5, 1],
      },
    });
    await nextTick();

    expect(getPanel()?.style.height).toBe("");
    expect(getPanel()?.style.width).toBe("400px");
  });

  it("renders the body and footer slots", async () => {
    mount(VDrawer, {
      attachTo: document.body,
      props: { visible: true },
      slots: {
        default: "<p class='drawer-test-body'>Body</p>",
        footer: "<button class='drawer-test-footer'>Save</button>",
      },
    });
    await nextTick();

    expect(document.body.querySelector(".drawer-test-body")).not.toBeNull();
    expect(document.body.querySelector(".drawer-test-footer")).not.toBeNull();
  });

  it("opens with a drag handle when requested", async () => {
    mount(VDrawer, {
      attachTo: document.body,
      props: { visible: true, showHandle: true },
    });
    await nextTick();

    expect(document.body.querySelector(".drawer-handle")).not.toBeNull();
  });

  describe("drag", () => {
    it("expands a snap point drawer when dragged up", async () => {
      const wrapper = mount(VDrawer, {
        attachTo: document.body,
        props: {
          defaultVisible: true,
          placement: "bottom",
          snapPoints: [0.25, 0.5],
          defaultSnapPoint: 0.25,
          showHandle: true,
        },
      });
      await nextTick();

      const panel = getPanel()!;
      const handle = document.querySelector<HTMLElement>(".drawer-handle")!;

      expect(panel.style.height).toBe("200px");

      dispatchPointer("pointerdown", handle, { clientX: 10, clientY: 600 });
      dispatchPointer("pointermove", window, { clientX: 10, clientY: 400 });
      await nextTick();

      expect(panel.style.height).toBe("400px");
      expect(panel.className.split(/\s+/)).toContain("drawer-panel--dragging");

      dispatchPointer("pointerup", window, { clientX: 10, clientY: 400 });

      await vi.waitFor(() => {
        expect(wrapper.emitted("update:snapPoint")?.[0]).toEqual([0.5]);
      });

      expect(panel.style.height).toBe("400px");
    });

    it.each([
      {
        placement: "left" as const,
        axis: "x",
        startX: 300,
        endX: 200,
        startY: 100,
        endY: 100,
      },
      {
        placement: "top" as const,
        axis: "y",
        startX: 100,
        endX: 100,
        startY: 300,
        endY: 200,
      },
    ])(
      "translates $placement drawers in the dismiss direction",
      async ({ placement, axis, startX, startY, endX, endY }) => {
        mount(VDrawer, {
          attachTo: document.body,
          props: {
            defaultVisible: true,
            placement,
            title: "Filters",
          },
        });
        await nextTick();

        const panel = getPanel()!;
        const header = document.querySelector<HTMLElement>(".drawer-header")!;

        dispatchPointer("pointerdown", header, {
          clientX: startX,
          clientY: startY,
        });
        dispatchPointer("pointermove", window, {
          clientX: endX,
          clientY: endY,
        });
        await nextTick();

        expect(panel.style.getPropertyValue(`--drawer-drag-${axis}`)).toBe(
          "-100px"
        );

        dispatchPointer("pointerup", window, {
          clientX: endX,
          clientY: endY,
        });
      }
    );

    it("closes when dragged past the dismiss threshold", async () => {
      mount(VDrawer, {
        attachTo: document.body,
        props: {
          defaultVisible: true,
          placement: "bottom",
          size: 400,
          showHandle: true,
        },
      });
      await nextTick();

      const handle = document.querySelector<HTMLElement>(".drawer-handle")!;

      dispatchPointer("pointerdown", handle, { clientX: 10, clientY: 300 });
      dispatchPointer("pointermove", window, { clientX: 10, clientY: 700 });
      dispatchPointer("pointerup", window, { clientX: 10, clientY: 700 });

      await vi.waitFor(() => {
        expect(getPanel()).toBeNull();
      });
    });
  });
});
