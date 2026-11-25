import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vite-plus/test";
import { VSpace } from "../index";
import type { SpaceAlign, SpaceDirection, SpaceJustify } from "../types";
import { SpaceSpacingSize } from "../types";

type VSpaceProps = InstanceType<typeof VSpace>["$props"];

const mountSpace = (
  props: Partial<VSpaceProps> = {},
  attrs: Record<string, string> = {}
) =>
  mount(VSpace, {
    props,
    attrs,
    slots: { default: "<span>first</span><span>second</span>" },
  });

const styleOf = (wrapper: ReturnType<typeof mountSpace>) =>
  (wrapper.element as HTMLElement).style;

describe("Space", () => {
  it("should render defaults unchanged", function () {
    const wrapper = mountSpace();

    expect(wrapper.classes()).toEqual([
      "space-wrapper",
      "space-direction-row",
      "space-align-center",
    ]);
    expect(styleOf(wrapper).getPropertyValue("--v-space-gap-x")).toBe("10px");
    expect(styleOf(wrapper).getPropertyValue("--v-space-gap-y")).toBe("10px");
  });

  it("should render slot content", function () {
    const wrapper = mountSpace();

    expect(wrapper.findAll("span")).toHaveLength(2);
    expect(wrapper.text()).toBe("firstsecond");
  });

  it("should work with preset spacing", function () {
    Object.keys(SpaceSpacingSize).forEach((key: string) => {
      const wrapper = mountSpace({ spacing: key as never });
      const size = SpaceSpacingSize[key as keyof typeof SpaceSpacingSize];

      expect(styleOf(wrapper).getPropertyValue("--v-space-gap-x")).toBe(
        `${size}px`
      );
      expect(styleOf(wrapper).getPropertyValue("--v-space-gap-y")).toBe(
        `${size}px`
      );

      wrapper.unmount();
    });
  });

  it("should work with a numeric spacing", function () {
    const wrapper = mountSpace({ spacing: 8 });

    expect(styleOf(wrapper).getPropertyValue("--v-space-gap-x")).toBe("8px");
    expect(styleOf(wrapper).getPropertyValue("--v-space-gap-y")).toBe("8px");
  });

  it("should work with a [horizontal, vertical] spacing", function () {
    const wrapper = mountSpace({ spacing: [20, 4] });

    expect(styleOf(wrapper).getPropertyValue("--v-space-gap-x")).toBe("20px");
    expect(styleOf(wrapper).getPropertyValue("--v-space-gap-y")).toBe("4px");
  });

  it("should fall back for unknown spacing", function () {
    const wrapper = mountSpace({ spacing: "huge" as never });

    expect(styleOf(wrapper).getPropertyValue("--v-space-gap-x")).toBe("10px");
    expect(styleOf(wrapper).getPropertyValue("--v-space-gap-y")).toBe("10px");
    expect(wrapper.attributes("style")).not.toContain("undefined");
  });

  it("should work with direction prop", function () {
    (["row", "column"] as SpaceDirection[]).forEach((direction) => {
      const wrapper = mountSpace({ direction });

      expect(wrapper.classes()).toContain(`space-direction-${direction}`);

      wrapper.unmount();
    });
  });

  it("should work with align prop", function () {
    (["center", "start", "end", "stretch", "baseline"] as SpaceAlign[]).forEach(
      (align) => {
        const wrapper = mountSpace({ align });

        expect(wrapper.classes()).toContain(`space-align-${align}`);

        wrapper.unmount();
      }
    );
  });

  it("should work with justify prop", function () {
    (
      [
        "start",
        "end",
        "center",
        "between",
        "around",
        "evenly",
      ] as SpaceJustify[]
    ).forEach((justify) => {
      const wrapper = mountSpace({ justify });

      expect(wrapper.classes()).toContain(`space-justify-${justify}`);

      wrapper.unmount();
    });
  });

  it("should not add a justify class by default", function () {
    const wrapper = mountSpace();

    expect(
      wrapper.classes().some((name) => name.startsWith("space-justify-"))
    ).toBe(false);
  });

  it("should support wrap, block and fill", function () {
    const wrapper = mountSpace({ wrap: true, block: true, fill: true });

    expect(wrapper.classes()).toContain("space-wrap");
    expect(wrapper.classes()).toContain("space-block");
    expect(wrapper.classes()).toContain("space-fill");
  });

  it("should not add layout classes by default", function () {
    const wrapper = mountSpace();

    expect(wrapper.classes()).not.toContain("space-wrap");
    expect(wrapper.classes()).not.toContain("space-block");
    expect(wrapper.classes()).not.toContain("space-fill");
  });

  it("should render the element given by as", function () {
    const wrapper = mountSpace({ as: "ul" });

    expect(wrapper.element.tagName).toBe("UL");
  });

  it("should merge external attrs with internal classes", function () {
    const wrapper = mountSpace({ spacing: "lg" }, { class: "mt-4" });

    expect(wrapper.classes()).toContain("space-wrapper");
    expect(wrapper.classes()).toContain("mt-4");
  });
});
