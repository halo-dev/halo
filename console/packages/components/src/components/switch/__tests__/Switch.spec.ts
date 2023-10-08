import { describe, expect, it } from "vitest";
import { VSwitch } from "../index";
import { mount, shallowMount } from "@vue/test-utils";

describe("Switch", () => {
  it("should render", () => {
    expect(mount(VSwitch)).toBeDefined();
  });

  it("emits the correct events when clicked", () => {
    const wrapper = shallowMount(VSwitch);
    wrapper.find("button").trigger("click");
    expect(wrapper.emitted("update:modelValue")).toBeTruthy();
    expect(wrapper.emitted("change")).toBeTruthy();
  });

  it("does not emit events when disabled or loading", () => {
    const wrapper = shallowMount(VSwitch, {
      props: { disabled: true, loading: true },
    });
    wrapper.find("button").trigger("click");
    expect(wrapper.emitted("update:modelValue")).toBeFalsy();
    expect(wrapper.emitted("change")).toBeFalsy();
  });
});
