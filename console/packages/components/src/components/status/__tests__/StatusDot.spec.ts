import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import { VStatusDot } from "../index";

describe("StatusDot", () => {
  it("should render", () => {
    expect(mount(VStatusDot)).toBeDefined();
  });

  it("should match snapshot", () => {
    const wrapper = mount(VStatusDot);
    expect(wrapper.html()).toMatchSnapshot();
  });

  it("should work with state prop", () => {
    ["default", "success", "warning", "error"].forEach((state) => {
      const wrapper = mount(VStatusDot, { props: { state } });
      expect(wrapper.classes()).toContain(`status-dot-${state}`);
    });
  });

  it("should work with animate prop", () => {
    const wrapper = mount(VStatusDot, { props: { animate: true } });
    expect(wrapper.classes()).toContain("status-dot-animate");
  });

  it("should work with text prop", () => {
    const wrapper = mount(VStatusDot, { props: { text: "text" } });
    expect(wrapper.find(".status-dot-text").text()).toBe("text");
  });
});
