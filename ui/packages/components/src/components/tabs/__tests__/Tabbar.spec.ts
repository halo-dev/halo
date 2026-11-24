import { flushPromises, mount, type VueWrapper } from "@vue/test-utils";
import {
  afterEach,
  beforeEach,
  describe,
  expect,
  it,
  vi,
} from "vite-plus/test";
import { nextTick } from "vue";
import {
  VTabbar,
  type TabbarItem,
  type TabsDirection,
  type TabsType,
} from "../index";

const items = Array.from({ length: 6 }, (_, index) => ({
  id: `tab-${index + 1}`,
  label: `Tab ${index + 1}`,
}));

const resizeObservers: ResizeObserverMock[] = [];
let containerWidth = 400;
let contentWidth = 800;
let reducedMotion = false;
let wrapper: VueWrapper | undefined;

class ResizeObserverMock {
  readonly observedElements = new Set<Element>();

  constructor(readonly callback: ResizeObserverCallback) {
    resizeObservers.push(this);
  }

  observe(target: Element) {
    this.observedElements.add(target);
  }

  unobserve(target: Element) {
    this.observedElements.delete(target);
  }

  disconnect() {
    this.observedElements.clear();
  }

  trigger() {
    const entries = Array.from(this.observedElements).map(
      (target) =>
        ({
          target,
          contentRect: {
            width: (target as HTMLElement).offsetWidth,
            height: (target as HTMLElement).offsetHeight,
          },
        }) as ResizeObserverEntry
    );
    if (entries.length > 0) {
      this.callback(entries, this as unknown as ResizeObserver);
    }
  }
}

async function mountTabbar(
  props: {
    activeId?: string;
    direction?: TabsDirection;
    flush?: boolean;
    items?: TabbarItem[];
    type?: TabsType;
  } = {}
) {
  wrapper = mount(VTabbar, {
    attachTo: document.body,
    props: {
      activeId: "tab-1",
      items,
      ...props,
    },
  });
  const scroller = wrapper.get<HTMLElement>(".tabbar-items").element;
  const scrollBy = vi.fn();
  scroller.scrollBy = scrollBy;
  await nextTick();
  resizeObservers.forEach((observer) => observer.trigger());
  return { wrapper, scroller, scrollBy };
}

async function setScrollLeft(scroller: HTMLElement, value: number) {
  Object.defineProperty(scroller, "scrollLeft", {
    configurable: true,
    value,
  });
  scroller.dispatchEvent(new Event("scroll"));
  await nextTick();
}

function dispatchWheel(
  scroller: HTMLElement,
  deltaY: number,
  init: WheelEventInit = {}
): WheelEvent {
  const event = new WheelEvent("wheel", {
    cancelable: true,
    deltaY,
    ...init,
  });
  scroller.dispatchEvent(event);
  return event;
}

function createRect(left: number, right: number): DOMRect {
  return {
    bottom: 40,
    height: 40,
    left,
    right,
    top: 0,
    width: right - left,
    x: left,
    y: 0,
    toJSON: () => ({}),
  } as DOMRect;
}

describe("Tabbar", () => {
  beforeEach(() => {
    resizeObservers.length = 0;
    containerWidth = 400;
    contentWidth = 800;
    reducedMotion = false;

    vi.stubGlobal("ResizeObserver", ResizeObserverMock);
    vi.stubGlobal(
      "matchMedia",
      vi.fn().mockImplementation(() => ({ matches: reducedMotion }))
    );
    vi.spyOn(HTMLElement.prototype, "offsetWidth", "get").mockImplementation(
      function () {
        if (this.classList.contains("tabbar-items")) return containerWidth;
        if (this.classList.contains("tabbar-items-track")) return contentWidth;
        return 100;
      }
    );
    vi.spyOn(HTMLElement.prototype, "offsetHeight", "get").mockReturnValue(40);
    vi.spyOn(HTMLElement.prototype, "clientWidth", "get").mockImplementation(
      function () {
        return this.classList.contains("tabbar-items") ? containerWidth : 0;
      }
    );
    vi.spyOn(HTMLElement.prototype, "scrollWidth", "get").mockImplementation(
      function () {
        return this.classList.contains("tabbar-items") ? contentWidth : 0;
      }
    );
  });

  afterEach(() => {
    wrapper?.unmount();
    wrapper = undefined;
    vi.restoreAllMocks();
    vi.unstubAllGlobals();
  });

  it("supports keyboard navigation between tabs", async () => {
    const { wrapper } = await mountTabbar();
    await flushPromises();

    const items = wrapper.findAll(".tabbar-item");

    expect(items[0].attributes("role")).toBe("tab");
    expect(items[0].attributes("aria-selected")).toBe("true");
    expect(items[0].attributes("tabindex")).toBe("0");
    expect(items[1].attributes("tabindex")).toBe("-1");

    await items[0].trigger("keydown", { key: "ArrowRight" });

    expect(wrapper.emitted("update:activeId")?.at(-1)).toEqual(["tab-2"]);
    expect(document.activeElement).toBe(items[1].element);

    await items[1].trigger("keydown", { key: "End" });

    expect(wrapper.emitted("update:activeId")?.at(-1)).toEqual(["tab-6"]);
    expect(document.activeElement).toBe(items[5].element);
  });

  it("updates scroll controls for content size changes", async () => {
    contentWidth = 300;
    const { wrapper } = await mountTabbar();
    await flushPromises();

    const left = wrapper.get('button[aria-label="Scroll tabs left"]');
    const right = wrapper.get('button[aria-label="Scroll tabs right"]');
    expect(left.attributes("disabled")).toBeDefined();
    expect(right.attributes("disabled")).toBeDefined();
    expect(wrapper.get(".tabbar-items").attributes("tabindex")).toBeUndefined();

    contentWidth = 600;
    resizeObservers.forEach((observer) => observer.trigger());
    await nextTick();

    expect(left.attributes("disabled")).toBeDefined();
    expect(right.attributes("disabled")).toBeUndefined();
    expect(wrapper.get(".tabbar-items").attributes("tabindex")).toBe("0");
  });

  it.each(["default", "pills", "outline"] as const)(
    "supports horizontal overflow for %s tabs",
    async (type) => {
      const { wrapper, scrollBy } = await mountTabbar({ type });
      await flushPromises();

      expect(wrapper.classes()).toContain(`tabbar-${type}`);
      const right = wrapper.get('button[aria-label="Scroll tabs right"]');
      expect(right.attributes("disabled")).toBeUndefined();

      await right.trigger("click");
      expect(scrollBy).toHaveBeenCalledWith({
        left: 320,
        behavior: "smooth",
      });
    }
  );

  it("shows scroll controls at the correct boundaries", async () => {
    contentWidth = 800.4;
    const { wrapper, scroller } = await mountTabbar();
    await flushPromises();

    const left = wrapper.get('button[aria-label="Scroll tabs left"]');
    const right = wrapper.get('button[aria-label="Scroll tabs right"]');
    expect(left.attributes("disabled")).toBeDefined();
    expect(right.attributes("disabled")).toBeUndefined();

    await setScrollLeft(scroller, 200);
    expect(left.attributes("disabled")).toBeUndefined();
    expect(right.attributes("disabled")).toBeUndefined();

    await setScrollLeft(scroller, 400);
    expect(left.attributes("disabled")).toBeUndefined();
    expect(right.attributes("disabled")).toBeDefined();

    await setScrollLeft(scroller, 0.5);
    expect(left.attributes("disabled")).toBeDefined();
    expect(right.attributes("disabled")).toBeUndefined();
  });

  it("scrolls arrows by 80 percent of the viewport", async () => {
    const { wrapper, scroller, scrollBy } = await mountTabbar();
    await flushPromises();

    await wrapper
      .get('button[aria-label="Scroll tabs right"]')
      .trigger("click");
    expect(scrollBy).toHaveBeenLastCalledWith({
      left: 320,
      behavior: "smooth",
    });

    await setScrollLeft(scroller, 200);
    reducedMotion = true;
    await wrapper.get('button[aria-label="Scroll tabs left"]').trigger("click");
    expect(scrollBy).toHaveBeenLastCalledWith({
      left: -320,
      behavior: "auto",
    });
  });

  it("moves focus to the opposite arrow when reaching a boundary", async () => {
    const { wrapper, scroller } = await mountTabbar();
    await flushPromises();
    await setScrollLeft(scroller, 200);

    const left = wrapper.get('button[aria-label="Scroll tabs left"]');
    const right = wrapper.get('button[aria-label="Scroll tabs right"]');
    right.element.focus();
    await right.trigger("click");

    expect(document.activeElement).toBe(left.element);
  });

  it("focuses the scroll region when jumping directly between boundaries", async () => {
    contentWidth = 600;
    const { wrapper, scroller } = await mountTabbar();
    await flushPromises();

    const left = wrapper.get('button[aria-label="Scroll tabs left"]');
    const right = wrapper.get('button[aria-label="Scroll tabs right"]');

    right.element.focus();
    await right.trigger("click");
    expect(document.activeElement).toBe(scroller);

    await setScrollLeft(scroller, 200);
    left.element.focus();
    await left.trigger("click");
    expect(document.activeElement).toBe(scroller);
  });

  it.each([
    {
      label: "first",
      activeId: "tab-1",
      itemIndex: 0,
      scrollLeft: 50,
      itemLeft: -50,
      itemRight: 100,
      expectedLeft: -90,
    },
    {
      label: "last",
      activeId: "tab-6",
      itemIndex: 5,
      scrollLeft: 350,
      itemLeft: 350,
      itemRight: 450,
      expectedLeft: 90,
    },
  ])(
    "scrolls a repeatedly clicked active $label item into view",
    async ({
      activeId,
      itemIndex,
      scrollLeft,
      itemLeft,
      itemRight,
      expectedLeft,
    }) => {
      const { wrapper, scroller, scrollBy } = await mountTabbar({ activeId });
      await flushPromises();
      await setScrollLeft(scroller, scrollLeft);

      scroller.getBoundingClientRect = vi.fn(() => createRect(0, 400));
      const item = wrapper.findAll(".tabbar-item")[itemIndex]
        .element as HTMLElement;
      item.getBoundingClientRect = vi.fn(() => createRect(itemLeft, itemRight));

      await wrapper.findAll(".tabbar-item")[itemIndex].trigger("click");
      await flushPromises();

      expect(scrollBy).toHaveBeenCalledWith({
        left: expectedLeft,
        behavior: "smooth",
      });
    }
  );

  it("scrolls an externally activated tab into the visible area", async () => {
    containerWidth = 200;
    contentWidth = 1000;
    const { wrapper, scroller, scrollBy } = await mountTabbar();
    await flushPromises();

    scroller.getBoundingClientRect = vi.fn(() => createRect(0, 200));
    const activeItem = wrapper.findAll(".tabbar-item")[3]
      .element as HTMLElement;
    activeItem.getBoundingClientRect = vi.fn(() => createRect(500, 600));

    await wrapper.setProps({ activeId: "tab-4" });
    await flushPromises();

    expect(scrollBy).toHaveBeenCalledWith({
      left: 440,
      behavior: "smooth",
    });
  });

  it("disables horizontal scrolling in column mode", async () => {
    const { wrapper, scroller, scrollBy } = await mountTabbar({
      direction: "column",
    });
    await flushPromises();

    expect(wrapper.classes()).toContain("tabbar-direction-column");
    expect(
      wrapper
        .get('button[aria-label="Scroll tabs left"]')
        .attributes("disabled")
    ).toBeDefined();
    expect(
      wrapper
        .get('button[aria-label="Scroll tabs right"]')
        .attributes("disabled")
    ).toBeDefined();
    expect(wrapper.get(".tabbar-items").attributes("tabindex")).toBeUndefined();

    expect(dispatchWheel(scroller, 100).defaultPrevented).toBe(false);
    expect(scrollBy).not.toHaveBeenCalled();
  });

  it("handles shift + wheel scrolling only away from the boundaries", async () => {
    const { scroller, scrollBy } = await mountTabbar();
    await flushPromises();

    await setScrollLeft(scroller, 200);
    expect(
      dispatchWheel(scroller, 100, { shiftKey: true }).defaultPrevented
    ).toBe(true);
    expect(scrollBy).toHaveBeenLastCalledWith({ left: 100 });

    scrollBy.mockClear();
    await setScrollLeft(scroller, 0);
    expect(
      dispatchWheel(scroller, -100, { shiftKey: true }).defaultPrevented
    ).toBe(false);
    expect(scrollBy).not.toHaveBeenCalled();

    await setScrollLeft(scroller, 400);
    expect(
      dispatchWheel(scroller, 100, { shiftKey: true }).defaultPrevented
    ).toBe(false);
    expect(scrollBy).not.toHaveBeenCalled();
  });

  it("keeps vertical wheel scrolling the page", async () => {
    const { scroller, scrollBy } = await mountTabbar();
    await flushPromises();

    await setScrollLeft(scroller, 200);
    scrollBy.mockClear();

    expect(dispatchWheel(scroller, 100).defaultPrevented).toBe(false);
    expect(scrollBy).not.toHaveBeenCalled();
  });

  it("normalizes line-based wheel deltas", async () => {
    const { scroller, scrollBy } = await mountTabbar();
    await flushPromises();

    await setScrollLeft(scroller, 200);

    expect(
      dispatchWheel(scroller, 1, { deltaMode: 1, shiftKey: true })
        .defaultPrevented
    ).toBe(true);
    expect(scrollBy).toHaveBeenLastCalledWith({ left: 16 });
  });

  it("skips disabled tabs during keyboard navigation", async () => {
    const { wrapper } = await mountTabbar({
      items: [
        { id: "tab-1", label: "Tab 1" },
        { id: "tab-2", label: "Tab 2", disabled: true },
        { id: "tab-3", label: "Tab 3" },
      ],
    });
    await flushPromises();

    const items = wrapper.findAll(".tabbar-item");
    expect(items[1].attributes("aria-disabled")).toBe("true");
    expect(items[1].attributes("tabindex")).toBe("-1");

    await items[0].trigger("keydown", { key: "ArrowRight" });

    expect(wrapper.emitted("update:activeId")?.at(-1)).toEqual(["tab-3"]);
    expect(document.activeElement).toBe(items[2].element);
  });

  it("does not activate a disabled tab", async () => {
    const { wrapper, scrollBy } = await mountTabbar({
      items: [
        { id: "tab-1", label: "Tab 1" },
        { id: "tab-2", label: "Tab 2", disabled: true },
      ],
    });
    await flushPromises();
    scrollBy.mockClear();
    const emitted = wrapper.emitted("update:activeId")?.length ?? 0;

    await wrapper.findAll(".tabbar-item")[1].trigger("click");

    expect(wrapper.emitted("update:activeId")?.length ?? 0).toBe(emitted);
    expect(scrollBy).not.toHaveBeenCalled();
  });

  it("activates the focused tab with Enter and Space", async () => {
    const { wrapper } = await mountTabbar({ activeId: "missing" });
    await flushPromises();

    const items = wrapper.findAll(".tabbar-item");

    await items[1].trigger("keydown", { key: "Enter" });
    expect(wrapper.emitted("update:activeId")?.at(-1)).toEqual(["tab-2"]);

    await items[2].trigger("keydown", { key: " " });
    expect(wrapper.emitted("update:activeId")?.at(-1)).toEqual(["tab-3"]);
  });

  it("falls back to the first enabled tab and emits the change", async () => {
    const { wrapper } = await mountTabbar({
      activeId: "missing",
      items: [
        { id: "tab-1", label: "Tab 1", disabled: true },
        { id: "tab-2", label: "Tab 2" },
      ],
    });
    await flushPromises();

    expect(wrapper.emitted("update:activeId")?.at(-1)).toEqual(["tab-2"]);
    expect(wrapper.findAll(".tabbar-item")[1].attributes("aria-selected")).toBe(
      "true"
    );
  });

  it("renders icons, badges and accessible names", async () => {
    const icon = { render: () => null };
    const { wrapper } = await mountTabbar({
      items: [
        { id: "tab-1", icon, ariaLabel: "Icon tab" },
        { id: "tab-2", label: "Tab 2", badge: 3 },
        { id: "tab-3", label: "Tab 3", badge: 0 },
      ],
    });
    await flushPromises();

    const items = wrapper.findAll(".tabbar-item");
    expect(items[0].find(".tabbar-item-icon").exists()).toBe(true);
    expect(items[0].attributes("aria-label")).toBe("Icon tab");
    expect(items[1].find(".tabbar-item-badge").text()).toBe("3");
    expect(items[2].find(".tabbar-item-badge").text()).toBe("0");
  });

  it("applies the flush form without changing keyboard behavior", async () => {
    const { wrapper } = await mountTabbar({ flush: true, type: "outline" });
    await flushPromises();

    expect(wrapper.classes()).toContain("is-flush");
    expect(wrapper.get('[role="tablist"]').attributes("aria-label")).toBe(
      "Tab list"
    );

    await wrapper.findAll(".tabbar-item")[0].trigger("keydown", {
      key: "ArrowRight",
    });

    expect(wrapper.emitted("update:activeId")?.at(-1)).toEqual(["tab-2"]);
  });

  it("labels the tab list and scroller with the built-in text", async () => {
    const { wrapper } = await mountTabbar();
    await flushPromises();

    expect(wrapper.get('[role="tablist"]').attributes("aria-label")).toBe(
      "Tab list"
    );
    expect(wrapper.get(".tabbar-items").attributes("aria-label")).toBe(
      "Tab list"
    );
  });
});
