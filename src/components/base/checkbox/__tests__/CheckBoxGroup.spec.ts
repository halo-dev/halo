import { describe, expect, it } from "vitest";
import { VCheckbox, VCheckboxGroup } from "../index";
import { mount } from "@vue/test-utils";

const options = [
  {
    value: "foo",
    label: "foo",
  },
  {
    value: "bar",
    label: "bar",
  },
];

describe("CheckBoxGroup", () => {
  it("should render", () => {
    expect(VCheckboxGroup).toBeDefined();
    expect(
      mount(VCheckboxGroup, {
        props: {
          options,
        },
      }).html()
    ).toMatchSnapshot();
  });

  it("should work with options prop", function () {
    const wrapper = mount({
      data() {
        return {
          options: options,
        };
      },
      template: `
        <v-checkbox-group :options="options" />
      `,
      components: {
        VCheckboxGroup,
      },
    });

    expect(wrapper.findAllComponents(VCheckbox).length).toBe(2);
    expect(wrapper.findAllComponents(VCheckbox)[0].vm.$props.value).toBe("foo");
    expect(wrapper.findAllComponents(VCheckbox)[0].vm.$props.label).toBe("foo");
    expect(wrapper.findAllComponents(VCheckbox)[1].vm.$props.value).toBe("bar");
    expect(wrapper.findAllComponents(VCheckbox)[1].vm.$props.label).toBe("bar");
  });

  it("should work with v-model", async function () {
    const wrapper = mount({
      data() {
        return {
          value: ["foo"],
          options: options,
        };
      },
      template: `
        <v-checkbox-group v-model="value" :options="options" />
      `,
      components: {
        VCheckboxGroup,
      },
    });

    expect(wrapper.findAllComponents(VCheckbox)[0].classes()).toContain(
      "checkbox-wrapper-checked"
    );
    expect(
      wrapper.findAllComponents(VCheckbox)[0].find("input").element.checked
    ).toBe(true);

    // mock click event
    await wrapper.findAllComponents(VCheckbox)[1].find("input").setValue(true);

    expect(wrapper.findAllComponents(VCheckbox)[1].classes()).toContain(
      "checkbox-wrapper-checked"
    );
    expect(
      wrapper.findAllComponents(VCheckbox)[1].find("input").element.checked
    ).toBe(true);
    expect(wrapper.vm.value).toEqual(["foo", "bar"]);
  });

  it("should work with valueKey and labelKey props", async function () {
    const wrapper = mount({
      data() {
        return {
          value: ["foo"],
          options: [
            {
              id: "foo",
              name: "foo",
            },
            {
              id: "bar",
              name: "bar",
            },
          ],
        };
      },
      template: `
        <v-checkbox-group v-model="value" :options="options" value-key="id" label-key="name"  />
      `,
      components: {
        VCheckboxGroup,
      },
    });

    expect(
      wrapper.findAllComponents(VCheckbox)[0].find("input").attributes("value")
    ).toBe("foo");
    expect(
      wrapper.findAllComponents(VCheckbox)[0].find(".checkbox-label").text()
    ).toBe("foo");

    await wrapper.findAllComponents(VCheckbox)[1].find("input").setValue(true);
    expect(wrapper.findAllComponents(VCheckbox)[1].classes()).toContain(
      "checkbox-wrapper-checked"
    );

    expect(wrapper.vm.value).toEqual(["foo", "bar"]);
  });
});
