import { describe, expect, it } from "vitest";
import { VSpace } from "../index";
import { mount } from "@vue/test-utils";
import { SpacingSize } from "../interface";

describe("Space", () => {
  it("should render", function () {
    expect(VSpace).toBeDefined();
  });

  it("should work with spacing prop", function () {
    Object.keys(SpacingSize).forEach((key: string) => {
      const wrapper = mount(VSpace, {
        propsData: {
          spacing: key,
        },
      });
      expect(wrapper.attributes()["style"]).toContain(
        `gap: ${SpacingSize[key]}px`
      );
      wrapper.unmount();
    });
  });

  it("should work with direction prop", function () {
    ["row", "column"].forEach((direction: string) => {
      const wrapper = mount(VSpace, {
        propsData: {
          direction: direction,
        },
      });
      expect(wrapper.classes()).toContain(`space-direction-${direction}`);
      wrapper.unmount();
    });
  });

  it("should work with align prop", function () {
    ["center", "start", "end", "stretch"].forEach((align: string) => {
      const wrapper = mount(VSpace, {
        propsData: {
          align: align,
        },
      });
      expect(wrapper.classes()).toContain(`space-align-${align}`);
      wrapper.unmount();
    });
  });
});
