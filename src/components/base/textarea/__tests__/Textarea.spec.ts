import { describe, expect, it } from "vitest";
import { VTextarea } from "../index";
import { mount } from "@vue/test-utils";

describe("Textarea", () => {
  it("should render", function () {
    expect(VTextarea).toBeDefined();
    expect(mount(VTextarea).html()).toMatchSnapshot();
  });

  it("should work with placeholder prop", async function () {
    const textarea = mount(VTextarea);
    expect(
      textarea.find("textarea").attributes()["placeholder"]
    ).toBeUndefined();

    // set placeholder prop
    const placeholderText = "Please enter your text";
    await textarea.setProps({ placeholder: placeholderText });
    expect(textarea.find("textarea").attributes()["placeholder"]).toBe(
      placeholderText
    );
  });

  it("should work with disabled prop", async function () {
    const textarea = mount(VTextarea);
    expect(textarea.find("textarea").attributes()["disabled"]).toBeUndefined();

    // set disabled prop
    await textarea.setProps({ disabled: true });
    expect(textarea.find("textarea").attributes()["disabled"]).toBeDefined();
  });

  it("should work with rows prop", function () {
    const textarea = mount(VTextarea, {
      propsData: {
        rows: 5,
      },
    });
    expect(textarea.find("textarea").attributes()["rows"]).toBe("5");
  });

  it("should work with v-model", async function () {
    const wrapper = mount({
      data() {
        return {
          value: "my name is ryanwang",
        };
      },
      template: `
        <v-textarea v-model="value"/>
      `,
      components: { VTextarea },
    });

    expect(wrapper.find("textarea").element.value).toBe("my name is ryanwang");

    // change value
    await wrapper.setData({
      value: "my name is ryanwang, my website is https://ryanc.cc",
    });
    expect(wrapper.find("textarea").element.value).toBe(
      "my name is ryanwang, my website is https://ryanc.cc"
    );

    // change modelValue by textarea element value
    await wrapper.find("textarea").setValue("my name is ryanwang");
    expect(wrapper.vm.$data.value).toBe("my name is ryanwang");
  });
});
