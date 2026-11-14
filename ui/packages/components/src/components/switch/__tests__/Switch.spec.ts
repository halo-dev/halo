import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vite-plus/test";
import { VSwitch } from "../index";

describe("Switch", () => {
  it("should render", () => {
    expect(mount(VSwitch)).toBeDefined();
  });

  it("should reflect modelValue in aria-checked", async () => {
    const wrapper = mount(VSwitch);
    const button = wrapper.get("button");

    expect(button.attributes("role")).toBe("switch");
    expect(button.attributes("aria-checked")).toBe("false");

    await wrapper.setProps({ modelValue: true });
    expect(button.attributes("aria-checked")).toBe("true");
  });

  it("forwards aria attributes and id to the switch control", () => {
    const wrapper = mount(VSwitch, {
      attrs: {
        "aria-label": "Enable feature",
        class: "custom-switch",
        id: "feature-switch",
      },
    });

    const button = wrapper.get("button");

    expect(button.attributes("aria-label")).toBe("Enable feature");
    expect(button.attributes("id")).toBe("feature-switch");
    expect(wrapper.get(".switch-wrapper").attributes("aria-label")).toBe(
      undefined
    );
    expect(wrapper.classes()).toContain("custom-switch");
  });
});
