import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vite-plus/test";
import { VSkeleton } from "../index";

describe("Skeleton", () => {
  it("renders an aria-hidden rectangle with a shimmer by default", () => {
    const wrapper = mount(VSkeleton);

    expect(wrapper.element.tagName).toBe("DIV");
    expect(wrapper.attributes("aria-hidden")).toBe("true");
    expect(wrapper.classes()).toEqual(
      expect.arrayContaining([
        "skeleton",
        "skeleton--rect",
        "skeleton--shimmer",
      ])
    );
    expect(wrapper.element.style.width).toBe("");
    expect(wrapper.element.style.height).toBe("");
  });

  it("converts numeric dimensions to pixels", () => {
    const wrapper = mount(VSkeleton, {
      props: { width: 120, height: 20 },
    });

    expect(wrapper.element.style.width).toBe("120px");
    expect(wrapper.element.style.height).toBe("20px");
  });

  it("passes string dimensions through", () => {
    const wrapper = mount(VSkeleton, {
      props: { width: "50%", height: "1.25rem" },
    });

    expect(wrapper.element.style.width).toBe("50%");
    expect(wrapper.element.style.height).toBe("1.25rem");
  });

  it("uses one dimension for both axes when rendering a circle", () => {
    const wrapper = mount(VSkeleton, {
      props: { shape: "circle", width: 40 },
    });

    expect(wrapper.classes()).toContain("skeleton--circle");
    expect(wrapper.element.style.width).toBe("40px");
    expect(wrapper.element.style.height).toBe("40px");
  });

  it("renders the requested element", () => {
    const wrapper = mount(VSkeleton, {
      props: { as: "span" },
    });

    expect(wrapper.element.tagName).toBe("SPAN");
  });

  it("supports pulse and no animation", () => {
    const pulse = mount(VSkeleton, {
      props: { animation: "pulse" },
    });
    const none = mount(VSkeleton, {
      props: { animation: "none" },
    });

    expect(pulse.classes()).toContain("skeleton--pulse");
    expect(none.classes()).toContain("skeleton--none");
  });

  it("merges consumer classes and styles", () => {
    const wrapper = mount(VSkeleton, {
      attrs: {
        class: "w-1/2",
        style: { backgroundColor: "red" },
      },
    });

    expect(wrapper.classes()).toContain("w-1/2");
    expect(wrapper.element.style.backgroundColor).toBe("red");
  });
});
