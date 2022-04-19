import { describe, expect, it } from "vitest";
import { VRadio } from "../index";
import { mount } from "@vue/test-utils";

describe("Radio", () => {
  it("should render", () => {
    expect(VRadio).toBeDefined();
    expect(mount(VRadio).html()).toMatchSnapshot();
  });

  it("should work with v-model", async function () {
    const wrapper = mount({
      data() {
        return {
          value: "",
        };
      },
      template: "<v-radio v-model='value' name='test' value='bar' />",
      components: {
        VRadio,
      },
    });

    expect(wrapper.findComponent(VRadio).classes()).not.toContain(
      "radio-wrapper-checked"
    );

    await wrapper.setData({ value: "bar" });

    expect(wrapper.findComponent(VRadio).classes()).toContain(
      "radio-wrapper-checked"
    );
  });

  it("should work with label prop", async function () {
    const wrapper = mount(VRadio, {
      props: {
        value: "foo",
      },
    });
    expect(wrapper.html()).not.toContain("label");

    await wrapper.setProps({ label: "foo" });

    expect(wrapper.html()).toContain("label");
    expect(wrapper.find("label").text()).toBe("foo");
  });

  it("should work with multiple radio", async function () {
    const wrapper = mount({
      data() {
        return {
          value: "foo",
        };
      },
      template:
        "<v-radio v-model='value' name='test' value='foo' label='foo' />" +
        "<v-radio v-model='value' name='test' value='bar' label='bar' />",
      components: {
        VRadio,
      },
    });

    expect(wrapper.findAllComponents(VRadio).length).toBe(2);
    expect(wrapper.findAllComponents(VRadio)[0].classes()).toContain(
      "radio-wrapper-checked"
    );

    // set value to bar
    await wrapper.setData({ value: "bar" });

    expect(wrapper.findAllComponents(VRadio)[0].classes()).not.toContain(
      "radio-wrapper-checked"
    );
    expect(wrapper.findAllComponents(VRadio)[1].classes()).toContain(
      "radio-wrapper-checked"
    );

    // click on the first radio
    await wrapper
      .findAllComponents(VRadio)[0]
      .find('input[type="radio"]')
      .trigger("change");

    expect(wrapper.findAllComponents(VRadio)[0].classes()).toContain(
      "radio-wrapper-checked"
    );

    // click on the second radio
    await wrapper
      .findAllComponents(VRadio)[1]
      .find('input[type="radio"]')
      .trigger("change");
    expect(wrapper.findAllComponents(VRadio)[1].classes()).toContain(
      "radio-wrapper-checked"
    );
  });
});
