import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vite-plus/test";
import SelectOption from "../SelectOption.vue";

describe("select option scrolling", () => {
  it("loads more as soon as a fast scroll reaches the bottom", () => {
    const wrapper = mount(SelectOption, {
      props: {
        options: [{ label: "Option 1", value: "1" }],
        multiple: false,
        maxCount: NaN,
        nextLoading: false,
      },
    });
    const element = wrapper.element as HTMLElement;
    Object.defineProperties(element, {
      clientHeight: { value: 100 },
      scrollHeight: { value: 500 },
    });

    element.scrollTop = 400;
    element.dispatchEvent(new Event("scroll"));
    element.scrollTop = 100;
    element.dispatchEvent(new Event("scroll"));

    expect(wrapper.emitted("loadMore")).toBeDefined();
    wrapper.unmount();
  });
});
