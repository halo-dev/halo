import { describe, expect, it } from "vitest";
import { VRadio, VRadioGroup } from "../index";
import { mount } from "@vue/test-utils";

describe("RadioGroup", () => {
  it("should render", function () {
    expect(VRadioGroup).toBeDefined();
    expect(
      mount(VRadioGroup, {
        props: {
          options: [
            {
              value: "foo",
              label: "foo",
            },
            {
              value: "bar",
              label: "bar",
            },
          ],
        },
      }).html()
    ).toMatchSnapshot();
  });

  it("should work with options prop", function () {
    const wrapper = mount({
      data() {
        return {
          options: [
            {
              value: "foo",
              label: "foo",
            },
            {
              value: "bar",
              label: "bar",
            },
          ],
        };
      },
      template: '<v-radio-group :options="options" />',
      components: {
        VRadioGroup,
      },
    });

    expect(wrapper.findAllComponents(VRadio).length).toBe(2);
    expect(wrapper.findAllComponents(VRadio)[0].vm.$props.value).toBe("foo");
    expect(wrapper.findAllComponents(VRadio)[0].vm.$props.label).toBe("foo");
    expect(wrapper.findAllComponents(VRadio)[1].vm.$props.value).toBe("bar");
    expect(wrapper.findAllComponents(VRadio)[1].vm.$props.label).toBe("bar");
  });

  it("should work with v-model", async function () {
    const wrapper = mount({
      data() {
        return {
          value: "foo",
          options: [
            {
              value: "foo",
              label: "foo",
            },
            {
              value: "bar",
              label: "bar",
            },
          ],
        };
      },
      template: '<v-radio-group v-model="value" :options="options" />',
      components: {
        VRadioGroup,
      },
    });

    expect(wrapper.findAllComponents(VRadio)[0].classes()).toContain(
      "radio-wrapper-checked"
    );

    await wrapper
      .findAllComponents(VRadio)[1]
      .find('input[type="radio"]')
      .trigger("change");

    expect(wrapper.findAllComponents(VRadio)[0].classes()).not.toContain(
      "radio-wrapper-checked"
    );
    expect(wrapper.findAllComponents(VRadio)[1].classes()).toContain(
      "radio-wrapper-checked"
    );
  });

  it("should work with valueKey and labelKey props", async function () {
    const wrapper = mount({
      data() {
        return {
          value: "foo",
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
      template:
        '<v-radio-group v-model="value" :options="options" value-key="id" label-key="name" />',
      components: {
        VRadioGroup,
      },
    });

    expect(
      wrapper.findAllComponents(VRadio)[0].find("input").attributes("value")
    ).toBe("foo");
    expect(
      wrapper.findAllComponents(VRadio)[0].find(".radio-label").text()
    ).toBe("foo");

    await wrapper
      .findAllComponents(VRadio)[1]
      .find('input[type="radio"]')
      .trigger("change");

    expect(wrapper.findAllComponents(VRadio)[1].classes()).toContain(
      "radio-wrapper-checked"
    );
  });
});
