import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vite-plus/test";
import { VSkeletonText } from "../index";

describe("SkeletonText", () => {
  it("renders three text lines with a shorter last line by default", () => {
    const wrapper = mount(VSkeletonText);
    const lines = wrapper.findAll(".skeleton");

    expect(wrapper.attributes("aria-hidden")).toBe("true");
    expect(lines).toHaveLength(3);
    expect(
      lines.every((line) => line.classes().includes("skeleton--text"))
    ).toBe(true);
    expect(lines[0].element.style.width).toBe("100%");
    expect(lines[1].element.style.width).toBe("100%");
    expect(lines[2].element.style.width).toBe("60%");
  });

  it("supports custom line counts, widths and sizes", () => {
    const wrapper = mount(VSkeletonText, {
      props: {
        lines: 2,
        widths: [80, 40],
        lineHeight: 12,
        spacing: 6,
      },
    });
    const lines = wrapper.findAll(".skeleton");

    expect(lines).toHaveLength(2);
    expect(lines[0].element.style.width).toBe("80px");
    expect(lines[1].element.style.width).toBe("40px");
    expect(lines[0].element.style.height).toBe("12px");
    expect(wrapper.element.style.gap).toBe("6px");
  });

  it("uses the last line width only when widths do not provide one", () => {
    const wrapper = mount(VSkeletonText, {
      props: {
        lines: 3,
        widths: [100],
        lastLineWidth: "25%",
      },
    });
    const lines = wrapper.findAll(".skeleton");

    expect(lines[0].element.style.width).toBe("100px");
    expect(lines[1].element.style.width).toBe("100%");
    expect(lines[2].element.style.width).toBe("25%");
  });

  it("passes the animation to every line", () => {
    const wrapper = mount(VSkeletonText, {
      props: { animation: "pulse" },
    });

    expect(
      wrapper
        .findAll(".skeleton")
        .every((line) => line.classes().includes("skeleton--pulse"))
    ).toBe(true);
  });

  it("clamps the line count to a non-negative integer", () => {
    expect(
      mount(VSkeletonText, { props: { lines: -1 } }).findAll(".skeleton")
    ).toHaveLength(0);
    expect(
      mount(VSkeletonText, { props: { lines: 2.8 } }).findAll(".skeleton")
    ).toHaveLength(2);
  });

  it("renders a custom root element and merges consumer classes", () => {
    const wrapper = mount(VSkeletonText, {
      props: { as: "span" },
      attrs: { class: "custom-text" },
    });

    expect(wrapper.element.tagName).toBe("SPAN");
    expect(wrapper.classes()).toContain("custom-text");
  });
});
