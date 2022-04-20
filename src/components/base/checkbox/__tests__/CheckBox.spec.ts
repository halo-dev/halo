import { describe, expect, it } from "vitest";
import { VCheckbox } from "../index";
import { mount } from "@vue/test-utils";

describe("CheckBox", () => {
  it("should render", () => {
    expect(VCheckbox).toBeDefined();
    expect(mount(VCheckbox).html()).toMatchSnapshot();
  });

  it("should work with v-model:checked", async function () {
    const wrapper = mount({
      data() {
        return {
          checked: false,
        };
      },
      template: `
        <v-checkbox v-model:checked="checked"/>
      `,
      components: {
        VCheckbox,
      },
    });

    expect(wrapper.find("input").element.checked).toBe(false);
    expect(wrapper.findComponent(VCheckbox).classes()).not.toContain(
      "checkbox-wrapper-checked"
    );

    // change checked value
    await wrapper.setData({ checked: true });
    expect(wrapper.find("input").element.checked).toBe(true);
    expect(wrapper.findComponent(VCheckbox).classes()).toContain(
      "checkbox-wrapper-checked"
    );

    // click on checkbox
    await wrapper.find("input").setValue(false);
    expect(wrapper.vm.checked).toBe(false);
    expect(wrapper.find("input").element.checked).toBe(false);
    expect(wrapper.findComponent(VCheckbox).classes()).not.toContain(
      "checkbox-wrapper-checked"
    );
  });

  it("should work with label prop", async function () {
    const wrapper = mount(VCheckbox, {
      props: {
        checked: true,
      },
    });

    expect(wrapper.html()).not.toContain("label");

    await wrapper.setProps({ label: "label" });

    expect(wrapper.html()).toContain("label");
    expect(wrapper.find("label").text()).toBe("label");
  });
});
