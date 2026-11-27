import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vite-plus/test";
import { VStatusDot } from "../index";

describe("StatusDot", () => {
  it("should render", () => {
    expect(mount(VStatusDot)).toBeDefined();
  });

  it("should match snapshot", () => {
    const wrapper = mount(VStatusDot);
    expect(wrapper.html()).toMatchSnapshot();
  });

  it("should default to success state and md size", () => {
    const wrapper = mount(VStatusDot);
    expect(wrapper.classes()).toContain("status-dot-success");
    expect(wrapper.classes()).toContain("status-dot-md");
    expect(wrapper.classes()).not.toContain("status-dot-animate");
  });

  it("should work with state prop", () => {
    ["default", "success", "warning", "error", "info", "processing"].forEach(
      (state) => {
        const wrapper = mount(VStatusDot, { props: { state } });
        expect(wrapper.classes()).toContain(`status-dot-${state}`);
      }
    );
  });

  it("should work with animate prop", () => {
    const wrapper = mount(VStatusDot, { props: { animate: true } });
    expect(wrapper.classes()).toContain("status-dot-animate");
  });

  it("should animate when state is processing", () => {
    const wrapper = mount(VStatusDot, { props: { state: "processing" } });
    expect(wrapper.classes()).toContain("status-dot-animate");
  });

  it("should work with size prop", () => {
    const wrapper = mount(VStatusDot, { props: { size: "sm" } });
    expect(wrapper.classes()).toContain("status-dot-sm");
  });

  it("should work with text prop", () => {
    const wrapper = mount(VStatusDot, { props: { text: "text" } });
    expect(wrapper.find(".status-dot-text").text()).toBe("text");
  });

  it("should render text slot inside the text wrapper", () => {
    const wrapper = mount(VStatusDot, { slots: { text: "slot text" } });
    expect(wrapper.find(".status-dot-text").text()).toBe("slot text");
  });

  it("should not render text when text is empty", () => {
    const wrapper = mount(VStatusDot, { props: { text: "" } });
    expect(wrapper.find(".status-dot-text").exists()).toBe(false);
  });

  it("should render label for screen readers", () => {
    const wrapper = mount(VStatusDot, { props: { label: "删除中" } });
    const label = wrapper.find(".status-dot-label");
    expect(label.exists()).toBe(true);
    expect(label.text()).toBe("删除中");
  });

  it("should not render label when text is present", () => {
    const wrapper = mount(VStatusDot, {
      props: { text: "运行中", label: "运行中" },
    });
    expect(wrapper.find(".status-dot-label").exists()).toBe(false);
  });

  it("should apply title attribute", () => {
    const wrapper = mount(VStatusDot, { props: { title: "删除中" } });
    expect(wrapper.attributes("title")).toBe("删除中");
  });

  it("should apply custom color", () => {
    const wrapper = mount(VStatusDot, { props: { color: "#8B5CF6" } });
    expect(
      (wrapper.element as HTMLElement).style.getPropertyValue(
        "--status-dot-color"
      )
    ).toBe("#8B5CF6");
  });

  it("should hide the dot from assistive technology", () => {
    const wrapper = mount(VStatusDot);
    expect(wrapper.find(".status-dot-body").attributes("aria-hidden")).toBe(
      "true"
    );
  });
});
