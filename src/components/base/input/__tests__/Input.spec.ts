import { describe, expect, it } from "vitest";
import { VInput } from "../index";
import { mount } from "@vue/test-utils";

describe("Input", () => {
  it("should render", () => {
    expect(VInput).toBeDefined();
    expect(mount(VInput).html()).toMatchSnapshot();
  });

  it("should work with size prop", function () {
    ["lg", "md", "sm", "xs"].forEach((size) => {
      const input = mount(VInput, { props: { size } });

      expect(input.html()).toMatchSnapshot();
      expect(input.find("input").classes()).toContain(`input-${size}`);
      input.unmount();
    });
  });

  it("should work with disabled prop", async function () {
    const input = mount(VInput);
    expect(input.find("input").attributes()["disabled"]).toBeUndefined();

    // set disabled prop
    await input.setProps({ disabled: true });
    expect(input.find("input").attributes()["disabled"]).toBeDefined();
  });

  it("should work with placeholder prop", async function () {
    const input = mount(VInput);
    expect(input.find("input").attributes()["placeholder"]).toBeUndefined();

    // set placeholder prop
    const placeholderText = "Please enter your name";
    await input.setProps({ placeholder: placeholderText });
    expect(input.find("input").attributes()["placeholder"]).toBe(
      placeholderText
    );
  });

  it("should work with v-model", async function () {
    const wrapper = mount({
      data() {
        return {
          value: "Ryan",
        };
      },
      template: `
        <v-input v-model="value"/>
      `,
      components: { VInput },
    });

    expect(wrapper.find("input").element.value).toBe("Ryan");

    // change value
    await wrapper.setData({
      value: "Ryan Wang",
    });
    expect(wrapper.find("input").element.value).toBe("Ryan Wang");

    // change modelValue by input element value
    await wrapper.find("input").setValue("ryanwang");
    expect(wrapper.vm.$data.value).toBe("ryanwang");
  });
});
