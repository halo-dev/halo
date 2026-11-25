import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vite-plus/test";
import { defineComponent, nextTick } from "vue";
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

  it("renders a label associated with the switch control", () => {
    const wrapper = mount(VSwitch, {
      props: {
        label: "Enable feature",
      },
    });

    const button = wrapper.get("button");
    const label = wrapper.get("label");

    expect(label.text()).toBe("Enable feature");
    expect(button.attributes("id")).toBeTruthy();
    expect(label.attributes("for")).toBe(button.attributes("id"));
  });

  it("uses the provided id for the label association", () => {
    const wrapper = mount(VSwitch, {
      props: {
        label: "Enable feature",
      },
      attrs: {
        id: "feature-switch",
      },
    });

    expect(wrapper.get("button").attributes("id")).toBe("feature-switch");
    expect(wrapper.get("label").attributes("for")).toBe("feature-switch");
  });

  it("toggles the switch when the label is clicked", async () => {
    const wrapper = mount(VSwitch, {
      props: {
        label: "Enable feature",
      },
    });

    await wrapper.get("label").trigger("click");

    expect(wrapper.emitted("update:modelValue")).toEqual([[true]]);
  });

  it("forwards a single click to the wrapper when the label is clicked", async () => {
    let clicks = 0;
    const wrapper = mount(VSwitch, {
      props: {
        label: "Enable feature",
      },
      attrs: {
        onClick: () => clicks++,
      },
    });

    await wrapper.get("label").trigger("click");

    expect(clicks).toBe(1);
  });

  it("does not render a label by default", () => {
    const wrapper = mount(VSwitch);

    expect(wrapper.find("label").exists()).toBe(false);
    expect(wrapper.get("button").attributes("id")).toBeUndefined();
  });

  it("does not toggle a disabled switch when the label is clicked", async () => {
    const wrapper = mount(VSwitch, {
      props: {
        label: "Enable feature",
        disabled: true,
      },
    });

    await wrapper.get("label").trigger("click");

    expect(wrapper.emitted("update:modelValue")).toBeUndefined();
  });

  it("renders the label slot and places the label on the requested side", async () => {
    const wrapper = mount(VSwitch, {
      slots: {
        label: "<span data-testid='label-content'>Custom label</span>",
      },
    });

    expect(wrapper.get("[data-testid='label-content']").text()).toBe(
      "Custom label"
    );
    // The label comes first in the DOM; `order-last` moves it to the right.
    expect(wrapper.get("label").classes()).toContain("order-last");

    await wrapper.setProps({ labelPosition: "left" });

    expect(wrapper.get("label").classes()).not.toContain("order-last");
  });

  it("forwards attributes that are added after an initially empty attrs object", async () => {
    const wrapper = mount(
      defineComponent({
        components: { VSwitch },
        data() {
          return {
            extraAttrs: {} as Record<string, string>,
          };
        },
        template: `<VSwitch v-bind="extraAttrs" />`,
      })
    );

    await wrapper.setData({
      extraAttrs: {
        "aria-label": "Enable feature",
        id: "feature-switch",
        class: "custom-switch",
      },
    });
    await nextTick();

    const button = wrapper.get("button");

    expect(button.attributes("aria-label")).toBe("Enable feature");
    expect(button.attributes("id")).toBe("feature-switch");
    expect(wrapper.get(".switch-wrapper").classes()).toContain("custom-switch");
  });
});
