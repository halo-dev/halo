import type { FormKitFrameworkContext } from "@formkit/core";
import { mount } from "@vue/test-utils";
import { describe, expect, it, vi } from "vite-plus/test";
import { defineComponent } from "vue";
import IconifyLabel from "../../array/labels/IconifyLabel.vue";
import IconifyInput from "../IconifyInput.vue";
import IconifyPicker from "../IconifyPicker.vue";

vi.mock("../IconifyPicker.vue", () => ({ default: { template: "<div />" } }));
vi.mock("@halo-dev/ui-shared", () => ({
  utils: { id: { uuid: () => "test" } },
}));
vi.mock("@halo-dev/components", () => ({
  IconClose: { template: "<span />" },
  VButton: { template: "<button><slot /></button>" },
  VDropdown: {
    methods: { hide: vi.fn() },
    template: '<div><slot :shown="true" /><slot name="popper" /></div>',
  },
}));

const dirtySvg =
  '<svg viewBox="0 0 24 24" onload="alert(1)"><path d="M0 0h24v24H0z"/></svg>';
const FormKit = defineComponent({
  name: "FormKit",
  props: { type: { type: String, default: undefined } },
  emits: ["submit"],
  template: "<div><slot /></div>",
});

function mountInput(valueOnly: boolean, format = "svg") {
  const input = vi.fn();
  const wrapper = mount(IconifyInput, {
    props: {
      context: {
        _value: valueOnly ? dirtySvg : { value: dirtySvg, name: "test:icon" },
        format,
        valueOnly,
        node: { input },
      } as unknown as FormKitFrameworkContext,
    },
    global: {
      components: { FormKit },
      mocks: { $t: (key: string) => key },
      directives: { tooltip: () => {} },
    },
  });
  return { wrapper, input };
}

describe("Iconify SVG rendering", () => {
  it.each([true, false])(
    "sanitizes pre-existing input values (valueOnly=%s)",
    (valueOnly) => {
      const { wrapper, input } = mountInput(valueOnly);
      expect(wrapper.find("svg").attributes("onload")).toBeUndefined();
      expect(wrapper.find("path").exists()).toBe(true);
      expect(input).not.toHaveBeenCalled();
    }
  );

  it.each([true, false])(
    "sanitizes submitted SVG values (valueOnly=%s)",
    async (valueOnly) => {
      const { wrapper, input } = mountInput(valueOnly);
      wrapper
        .findAllComponents(FormKit)
        .find((component) => component.props("type") === "form")!
        .vm.$emit("submit", { value: dirtySvg });
      await wrapper.vm.$nextTick();
      const result = input.mock.calls[0][0];
      expect(valueOnly ? result : result.value).not.toContain("onload");
      expect(valueOnly ? result : result.value).toContain("<path");
    }
  );

  it.each([true, false])(
    "sanitizes selected SVG without mutating the selection (valueOnly=%s)",
    async (valueOnly) => {
      const { wrapper, input } = mountInput(valueOnly);
      const selected = { value: dirtySvg, name: "test:icon", color: "red" };
      wrapper.findComponent(IconifyPicker).vm.$emit("select", selected);
      await wrapper.vm.$nextTick();
      const result = input.mock.calls[0][0];
      expect(valueOnly ? result : result.value).not.toContain("onload");
      expect(selected.value).toBe(dirtySvg);
      if (!valueOnly) {
        expect(result.name).toBe("test:icon");
        expect(result.color).toBe("red");
      }
    }
  );

  it.each([true, false])(
    "sanitizes pre-existing array label values (valueOnly=%s)",
    async (valueOnly) => {
      const wrapper = mount(IconifyLabel, {
        props: {
          itemLabel: {
            type: "iconify",
            format: "svg",
            valueOnly,
            value: valueOnly ? dirtySvg : { value: dirtySvg },
          },
        },
      });
      expect(wrapper.find("svg").attributes("onload")).toBeUndefined();
      await wrapper.setProps({
        itemLabel: {
          type: "iconify",
          format: "svg",
          valueOnly,
          value: valueOnly
            ? '<svg onmouseover="alert(2)"><circle r="2"/></svg>'
            : { value: '<svg onmouseover="alert(2)"><circle r="2"/></svg>' },
        },
      });
      expect(wrapper.find("svg").attributes("onmouseover")).toBeUndefined();
      expect(wrapper.find("circle").exists()).toBe(true);
    }
  );

  it.each(["url", "dataurl", "name"])(
    "does not sanitize %s submissions as SVG",
    async (format) => {
      const { wrapper, input } = mountInput(true, format);
      const value =
        format === "name" ? "mdi:home" : "https://example.com/icon.svg";
      wrapper
        .findAllComponents(FormKit)
        .find((component) => component.props("type") === "form")!
        .vm.$emit("submit", { value });
      await wrapper.vm.$nextTick();
      expect(input).toHaveBeenCalledWith(value);
    }
  );
});
