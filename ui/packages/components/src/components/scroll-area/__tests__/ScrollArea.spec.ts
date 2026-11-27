import { mount } from "@vue/test-utils";
import {
  afterEach,
  beforeEach,
  describe,
  expect,
  it,
  vi,
} from "vite-plus/test";
import { nextTick } from "vue";
import { VScrollArea } from "..";

class ResizeObserverMock {
  observe() {}
  unobserve() {}
  disconnect() {}
}

const viewportSelector = "[data-reka-scroll-area-viewport]";

function mountScrollArea(props: Record<string, unknown> = {}) {
  return mount(VScrollArea, {
    props,
    slots: {
      default: () => "Scrollable content",
    },
    attachTo: document.body,
  });
}

describe("ScrollArea", () => {
  beforeEach(() => {
    vi.stubGlobal("ResizeObserver", ResizeObserverMock);
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("renders the slot inside the scroll viewport", () => {
    const wrapper = mountScrollArea();

    expect(wrapper.get(viewportSelector).text()).toBe("Scrollable content");
    expect(wrapper.get(".v-scroll-area__content").element.parentElement).toBe(
      wrapper.get(viewportSelector).element
    );
  });

  it("mounts the scrollbar lazily when it only shows while scrolling", () => {
    const wrapper = mountScrollArea({ type: "scroll" });

    expect(wrapper.findAll(".v-scroll-area__scrollbar")).toHaveLength(0);
  });

  it("only renders a vertical scrollbar by default", () => {
    const wrapper = mountScrollArea({ type: "always" });
    const scrollbars = wrapper.findAll(".v-scroll-area__scrollbar");

    expect(scrollbars).toHaveLength(1);
    expect(scrollbars[0].attributes("data-orientation")).toBe("vertical");
    expect(wrapper.find(".v-scroll-area__corner").exists()).toBe(false);
  });

  it("renders both scrollbars and the corner when both axes are enabled", () => {
    const wrapper = mountScrollArea({ type: "always", orientation: "both" });
    const scrollbars = wrapper.findAll(".v-scroll-area__scrollbar");

    expect(scrollbars).toHaveLength(2);
    expect(
      scrollbars.map((scrollbar) => scrollbar.attributes("data-orientation"))
    ).toEqual(["vertical", "horizontal"]);
    expect(wrapper.get(".v-scroll-area").classes()).toContain(
      "v-scroll-area--horizontal"
    );
  });

  it("only renders a horizontal scrollbar when the orientation is horizontal", () => {
    const wrapper = mountScrollArea({
      type: "always",
      orientation: "horizontal",
    });
    const scrollbars = wrapper.findAll(".v-scroll-area__scrollbar");

    expect(scrollbars).toHaveLength(1);
    expect(scrollbars[0].attributes("data-orientation")).toBe("horizontal");
  });

  it("re-emits the native scroll event", async () => {
    const wrapper = mountScrollArea();

    await wrapper.get(viewportSelector).trigger("scroll");

    expect(wrapper.emitted("scroll")).toHaveLength(1);
  });

  it("exposes the viewport element", async () => {
    const wrapper = mountScrollArea();
    await nextTick();

    expect((wrapper.vm as unknown as { viewport?: HTMLElement }).viewport).toBe(
      wrapper.get(viewportSelector).element
    );
  });
});
