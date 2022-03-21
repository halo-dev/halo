import { describe, expect, it, vi } from "vitest";
import { VButton } from "../index";
import { mount } from "@vue/test-utils";

describe("Button", () => {
  it("should render", () => {
    expect(mount(VButton).html()).contains("button");
  });

  it("should render with text", () => {
    expect(mount(VButton, { slots: { default: "Hello Halo" } }).text()).toBe(
      "Hello Halo"
    );
  });

  it("should work with type prop", () => {
    // default button type is default
    expect(mount(VButton).find(".btn").classes()).toContain("btn-default");

    ["primary", "secondary", "danger"].forEach((type) => {
      const button = mount(VButton, { props: { type } });
      expect(button.find(".btn").classes()).toContain(`btn-${type}`);
      button.unmount();
    });
  });

  it("should work with size prop", async () => {
    // default button size is md
    expect(mount(VButton).find(".btn").classes()).toContain("btn-md");

    ["lg", "sm", "xs"].forEach((size) => {
      const button = mount(VButton, { props: { size } });
      expect(button.find(".btn").classes()).toContain(`btn-${size}`);
      button.unmount();
    });
  });

  it("should work with circle prop", async () => {
    const button = mount(VButton);

    // default: false
    expect(button.find(".btn").classes()).not.toContain("btn-circle");

    await button.setProps({ circle: true });
    expect(button.find(".btn").classes()).toContain("btn-circle");
  });

  it("should work with block prop", async () => {
    const button = mount(VButton);

    // default: false
    expect(button.find(".btn").classes()).not.toContain("btn-block");

    await button.setProps({ block: true });
    expect(button.find(".btn").classes()).toContain("btn-block");
  });

  it("should work with disabled prop", async () => {
    const onClick = vi.fn(() => 1);

    // default: false
    const button = mount(VButton, {
      emits: { click: onClick },
    });

    await button.trigger("click");
    expect(onClick).toHaveBeenCalled();

    onClick.mockReset();
    await button.setProps({ disabled: true });
    await button.trigger("click");
    expect(onClick).not.toHaveBeenCalled();
  });
});
