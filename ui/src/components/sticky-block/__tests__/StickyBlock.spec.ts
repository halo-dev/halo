import { mount } from "@vue/test-utils";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { nextTick } from "vue";
import StickyBlock from "../StickyBlock.vue";

let rectBottom = 0;
const originalInnerHeight = window.innerHeight;
const resizeObservers: ResizeObserverMock[] = [];

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
    this.callback([], this as unknown as ResizeObserver);
  }
}

describe("StickyBlock", () => {
  beforeEach(() => {
    rectBottom = 900;
    resizeObservers.length = 0;

    vi.stubGlobal("ResizeObserver", ResizeObserverMock);
    Object.defineProperty(window, "innerHeight", {
      configurable: true,
      value: 800,
    });
    vi.spyOn(HTMLElement.prototype, "getBoundingClientRect").mockImplementation(
      () => ({
        bottom: rectBottom,
        height: 0,
        left: 0,
        right: 0,
        top: 0,
        width: 0,
        x: 0,
        y: 0,
        toJSON: () => ({}),
      })
    );
  });

  afterEach(() => {
    vi.restoreAllMocks();
    vi.unstubAllGlobals();
    Object.defineProperty(window, "innerHeight", {
      configurable: true,
      value: originalInnerHeight,
    });
  });

  it("recomputes the bottom shadow when the viewport resizes", async () => {
    const wrapper = mount(StickyBlock, {
      props: {
        position: "bottom",
      },
      slots: {
        default: "Save",
      },
    });
    await nextTick();

    expect(wrapper.classes()).toContain("sticky-shadow");

    rectBottom = 700;
    window.dispatchEvent(new Event("resize"));
    await nextTick();

    expect(wrapper.classes()).not.toContain("sticky-shadow");
  });

  it("recomputes the bottom shadow when the parent layout changes", async () => {
    const wrapper = mount({
      components: {
        StickyBlock,
      },
      template: `<div><StickyBlock position="bottom">Save</StickyBlock></div>`,
    });
    await nextTick();
    await nextTick();

    const stickyBlock = wrapper.findComponent(StickyBlock);
    expect(stickyBlock.classes()).toContain("sticky-shadow");

    const parentElement = stickyBlock.element.parentElement;
    const parentObserver = resizeObservers.find((observer) => {
      return parentElement && observer.observedElements.has(parentElement);
    });
    expect(parentObserver).toBeDefined();

    rectBottom = 700;
    parentObserver?.trigger();
    await nextTick();

    expect(stickyBlock.classes()).not.toContain("sticky-shadow");
  });
});
