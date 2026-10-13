import { flushPromises, mount } from "@vue/test-utils";
import { beforeEach, expect, it, vi } from "vite-plus/test";
import { h, ref } from "vue";
import CollapsibleCommentContent from "../CollapsibleCommentContent.vue";

vi.mock("../../composables/use-content-provider-extension-point", () => ({
  useContentProviderExtensionPoint: () => ({
    data: ref({
      component: {
        props: ["content"],
        setup(props: { content?: string }) {
          return () => h("div", props.content);
        },
      },
    }),
  }),
}));

const mocks = vi.hoisted(() => ({
  callback: undefined as ResizeObserverCallback | undefined,
  observe: vi.fn(),
  disconnect: vi.fn(),
}));

vi.stubGlobal(
  "ResizeObserver",
  class {
    constructor(callback: ResizeObserverCallback) {
      mocks.callback = callback;
    }
    observe = mocks.observe;
    unobserve = vi.fn();
    disconnect = mocks.disconnect;
  }
);

function setScrollHeight(el: Element, height: number) {
  Object.defineProperty(el, "scrollHeight", {
    value: height,
    configurable: true,
  });
}

function triggerResize(height: number) {
  const el = document.querySelector(
    "[id^='collapsible-comment-content-']"
  ) as HTMLElement;
  setScrollHeight(el, height);
  mocks.callback?.([], {} as ResizeObserver);
}

beforeEach(() => {
  document.body.innerHTML = "";
  mocks.callback = undefined;
  mocks.observe.mockClear();
  mocks.disconnect.mockClear();
});

function mountContent(content: string) {
  return mount(CollapsibleCommentContent, {
    props: { content },
    attachTo: document.body,
    global: { mocks: { $t: (key: string) => key } },
  });
}

it("does not collapse content shorter than the threshold", async () => {
  const wrapper = mountContent("short");
  await flushPromises();
  triggerResize(100);
  await flushPromises();

  expect(wrapper.find("button").exists()).toBe(false);
  const content = wrapper.get("[id^='collapsible-comment-content-']");
  expect(content.attributes("style")).toBeUndefined();
  expect(
    (content.element.firstElementChild as HTMLElement).getAttribute("inert")
  ).toBe("false");
  wrapper.unmount();
});

it("collapses tall content and toggles via button and content click", async () => {
  const wrapper = mountContent("tall");
  await flushPromises();
  triggerResize(600);
  await flushPromises();

  const button = wrapper.get("button");
  expect(button.text()).toContain("core.comment.text.expand_content");
  expect(button.attributes("aria-expanded")).toBe("false");
  const content = wrapper.get("[id^='collapsible-comment-content-']");
  expect(button.attributes("aria-controls")).toBe(content.attributes("id"));
  expect(content.attributes("style")).toContain("max-height: 200px");
  expect(content.attributes("style")).toContain("mask-image");
  expect(
    (content.element.firstElementChild as HTMLElement).getAttribute("inert")
  ).toBe("true");

  await button.trigger("click");
  expect(button.attributes("aria-expanded")).toBe("true");
  expect(button.text()).toContain("core.comment.text.collapse_content");
  expect(content.attributes("style")).toBeUndefined();
  expect(
    (content.element.firstElementChild as HTMLElement).getAttribute("inert")
  ).toBe("false");

  // Collapse again via the button, then expand by clicking the content area.
  await button.trigger("click");
  expect(content.attributes("style")).toContain("max-height: 200px");
  await content.trigger("click");
  expect(button.attributes("aria-expanded")).toBe("true");
  wrapper.unmount();
});

it("stops collapsing when content shrinks below the threshold", async () => {
  const wrapper = mountContent("tall");
  await flushPromises();
  triggerResize(600);
  await flushPromises();
  expect(wrapper.find("button").exists()).toBe(true);

  triggerResize(100);
  await flushPromises();
  expect(wrapper.find("button").exists()).toBe(false);
  expect(
    wrapper.get("[id^='collapsible-comment-content-']").attributes("style")
  ).toBeUndefined();
  wrapper.unmount();
});
