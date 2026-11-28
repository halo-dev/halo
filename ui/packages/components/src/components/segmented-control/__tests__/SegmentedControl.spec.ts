import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vite-plus/test";
import { defineComponent, h, ref } from "vue";
import {
  VSegmentedControl,
  type SegmentedControlItem,
  type SegmentedControlValue,
} from "../index";

const items: SegmentedControlItem[] = [
  { value: "", label: "全部" },
  { value: "pending", label: "待支付" },
  { value: "done", label: "已完成" },
];

function mountControlled(
  initialValue: SegmentedControlValue,
  controlItems: SegmentedControlItem[] = items
) {
  const active = ref<SegmentedControlValue>(initialValue);

  const wrapper = mount(VSegmentedControl, {
    props: {
      modelValue: initialValue,
      items: controlItems,
      ariaLabel: "订单状态",
      "onUpdate:modelValue": (value: SegmentedControlValue) => {
        active.value = value;
      },
    },
  });

  return { active, wrapper };
}

describe("SegmentedControl", () => {
  it("renders a radiogroup and marks the selected item", () => {
    const wrapper = mount(VSegmentedControl, {
      props: {
        modelValue: "pending",
        items,
        ariaLabel: "订单状态",
      },
    });

    const group = wrapper.get('[role="radiogroup"]');
    expect(group.attributes("aria-label")).toBe("订单状态");

    const radios = wrapper.findAll('[role="radio"]');
    expect(radios).toHaveLength(3);
    expect(radios[1].attributes("aria-checked")).toBe("true");
    expect(radios[1].attributes("data-state")).toBe("checked");
    expect(radios[0].attributes("aria-checked")).toBe("false");
  });

  it("emits update and change when an item is clicked", async () => {
    const { active, wrapper } = mountControlled("pending");

    await wrapper.findAll('[role="radio"]')[2].trigger("click");

    expect(active.value).toBe("done");
    expect(wrapper.emitted("update:modelValue")?.at(-1)).toEqual(["done"]);
    expect(wrapper.emitted("change")?.at(-1)).toEqual(["done"]);
  });

  it("supports an empty string value", () => {
    const wrapper = mount(VSegmentedControl, {
      props: {
        modelValue: "",
        items,
        ariaLabel: "订单状态",
      },
    });

    const radios = wrapper.findAll('[role="radio"]');
    expect(radios[0].attributes("aria-checked")).toBe("true");
    expect(radios[0].text()).toBe("全部");
  });

  it("supports numeric values", () => {
    const wrapper = mount(VSegmentedControl, {
      props: {
        modelValue: 2,
        ariaLabel: "每页条数",
        items: [
          { value: 1, label: "10" },
          { value: 2, label: "20" },
        ],
      },
    });

    expect(
      wrapper.findAll('[role="radio"]')[1].attributes("aria-checked")
    ).toBe("true");
  });

  it("does not activate a disabled item", async () => {
    const { active, wrapper } = mountControlled("pending", [
      { value: "pending", label: "待支付" },
      { value: "done", label: "已完成", disabled: true },
    ]);

    await wrapper.findAll('[role="radio"]')[1].trigger("click");

    expect(active.value).toBe("pending");
    expect(wrapper.emitted("update:modelValue")).toBeUndefined();
  });

  it("disables every item when the control is disabled", () => {
    const wrapper = mount(VSegmentedControl, {
      props: {
        modelValue: "pending",
        items,
        ariaLabel: "订单状态",
        disabled: true,
      },
    });

    for (const radio of wrapper.findAll('[role="radio"]')) {
      expect(radio.attributes("disabled")).toBeDefined();
    }
  });

  it("names icon-only items and sizes them as squares", () => {
    const icon = defineComponent({
      render: () => h("svg"),
    });
    const wrapper = mount(VSegmentedControl, {
      props: {
        modelValue: "list",
        ariaLabel: "视图",
        items: [
          { value: "list", icon, ariaLabel: "列表视图" },
          { value: "grid", icon, ariaLabel: "网格视图" },
        ],
      },
    });

    const radios = wrapper.findAll('[role="radio"]');
    expect(radios[0].attributes("aria-label")).toBe("列表视图");
    expect(radios[0].classes()).toContain("is-icon-only");
  });

  it("applies the size and stretch classes", () => {
    const wrapper = mount(VSegmentedControl, {
      props: {
        modelValue: "pending",
        items,
        ariaLabel: "订单状态",
        size: "sm",
        stretch: true,
      },
    });

    const control = wrapper.get(".segmented-control");
    expect(control.classes()).toContain("segmented-control-sm");
    expect(control.classes()).toContain("segmented-control-stretch");
  });

  it("supports a vertical direction", async () => {
    const wrapper = mount(VSegmentedControl, {
      props: {
        modelValue: "pending",
        items,
        ariaLabel: "订单状态",
        direction: "column",
      },
    });

    const group = wrapper.get('[role="radiogroup"]');
    expect(group.attributes("aria-orientation")).toBe("vertical");
    expect(wrapper.get(".segmented-control").classes()).toContain(
      "segmented-control-direction-column"
    );

    await wrapper.findAll('[role="radio"]')[1].trigger("keydown", {
      key: "ArrowDown",
    });

    expect(wrapper.emitted("update:modelValue")?.at(-1)).toEqual(["done"]);
  });
});
