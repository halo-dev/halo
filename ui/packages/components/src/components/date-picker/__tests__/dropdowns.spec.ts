/* eslint-disable vue/require-default-prop -- Test stub props mirror VPopover without defaults. */
/* eslint-disable vue/one-component-per-file -- Test-local host and dropdown stubs keep these integration tests isolated. */
import { mount } from "@vue/test-utils";
import { describe, expect, it, vi } from "vite-plus/test";
import { defineComponent, h, nextTick, ref } from "vue";
import { VDatePickerDropdown, VDateRangePickerDropdown } from "..";
import { VFilterDateRangeDropdown, VFilterDropdownGroup } from "../../filter";
import type { DateRangeValue } from "../types";

const VPopoverStub = defineComponent({
  name: "VPopover",
  props: {
    open: Boolean,
    disabled: Boolean,
    placement: String,
    offset: [String, Number],
    arrow: Boolean,
    contentClass: [String, Object, Array],
  },
  emits: ["update:open"],
  setup(_, { slots, expose, emit }) {
    expose({
      show: () => emit("update:open", true),
      hide: () => emit("update:open", false),
    });

    return () => h("div", [slots.default?.(), slots.content?.()]);
  },
});

function cellSelector(value: string) {
  return `[data-reka-calendar-cell-trigger][data-value="${value}"]`;
}

const global = {
  stubs: {
    // VPopover's component name is inferred from its file name.
    Popover: VPopoverStub,
  },
};

async function openDropdown(wrapper: ReturnType<typeof mount>) {
  wrapper.findComponent(VPopoverStub).vm.$emit("update:open", true);
  await nextTick();
}

describe("DatePickerDropdown", () => {
  it("commits date mode values immediately and closes", async () => {
    const wrapper = mount(VDatePickerDropdown, {
      props: {
        modelValue: "2026-11-10",
        defaultPickerValue: "2026-11-01",
      },
      global,
    });

    await openDropdown(wrapper);
    await wrapper.get(cellSelector("2026-11-15")).trigger("click");

    expect(wrapper.emitted("update:modelValue")?.[0]).toEqual(["2026-11-15"]);
    expect(wrapper.emitted("select")?.[0]).toEqual(["2026-11-15"]);
    expect(wrapper.emitted("update:open")?.at(-1)).toEqual([false]);
  });

  it("keeps datetime changes in a draft until confirmed", async () => {
    const wrapper = mount(VDatePickerDropdown, {
      props: {
        mode: "datetime",
        modelValue: "2026-11-10T09:30",
        defaultPickerValue: "2026-11-01",
      },
      global,
    });

    await openDropdown(wrapper);
    await wrapper.get(cellSelector("2026-11-15")).trigger("click");

    expect(wrapper.emitted("update:modelValue")).toBeUndefined();

    const confirm = wrapper
      .findAll("button")
      .find((button) => button.text() === "Confirm");
    await confirm?.trigger("click");

    expect(wrapper.emitted("update:modelValue")?.[0]).toEqual([
      "2026-11-15T09:30",
    ]);
    expect(wrapper.emitted("update:open")?.at(-1)).toEqual([false]);
  });
});

describe("DateRangePickerDropdown", () => {
  it("only commits a complete date range and closes", async () => {
    const wrapper = mount(VDateRangePickerDropdown, {
      props: { defaultPickerValue: "2026-11-01" },
      global,
    });

    await openDropdown(wrapper);
    await wrapper.get(cellSelector("2026-11-10")).trigger("click");
    await nextTick();

    expect(wrapper.emitted("update:modelValue")).toBeUndefined();

    await wrapper.get(cellSelector("2026-11-15")).trigger("mouseenter");
    await wrapper.get(cellSelector("2026-11-15")).trigger("click");

    expect(wrapper.emitted("update:modelValue")?.[0]).toEqual([
      { start: "2026-11-10", end: "2026-11-15" },
    ]);
    expect(wrapper.emitted("select")?.[0]).toEqual([
      { start: "2026-11-10", end: "2026-11-15" },
    ]);
    expect(wrapper.emitted("update:open")?.at(-1)).toEqual([false]);
  });

  it("keeps a partial datetime range in a draft until confirmed", async () => {
    const wrapper = mount(VDateRangePickerDropdown, {
      props: {
        mode: "datetime-range",
        defaultPickerValue: "2026-11-01",
        defaultStartTime: "08:00",
        defaultEndTime: "18:00",
      },
      global,
    });

    await openDropdown(wrapper);
    await wrapper.get(cellSelector("2026-11-10")).trigger("click");
    await nextTick();

    const confirm = wrapper
      .findAll("button")
      .find((button) => button.text() === "Confirm");

    expect(confirm?.attributes("disabled")).toBeDefined();
    expect(wrapper.emitted("update:modelValue")).toBeUndefined();

    await openDropdown(wrapper);
    await wrapper.get(cellSelector("2026-11-15")).trigger("mouseenter");
    await wrapper.get(cellSelector("2026-11-15")).trigger("click");
    await nextTick();

    const updatedConfirm = wrapper
      .findAll("button")
      .find((button) => button.text() === "Confirm");

    expect(updatedConfirm?.attributes("disabled")).toBeUndefined();
    await updatedConfirm?.trigger("click");

    expect(wrapper.emitted("update:modelValue")?.[0]).toEqual([
      { start: "2026-11-10T08:00", end: "2026-11-15T18:00" },
    ]);
  });
});

describe("FilterDateRangeDropdown", () => {
  function mountFilter(initial?: DateRangeValue) {
    const value = ref<DateRangeValue | undefined>(initial);

    const Host = defineComponent({
      setup() {
        return () =>
          h(VFilterDropdownGroup, null, {
            default: () => [
              h(VFilterDateRangeDropdown, {
                label: "Created at",
                modelValue: value.value,
                "onUpdate:modelValue": (next?: DateRangeValue) => {
                  value.value = next;
                },
                defaultPickerValue: "2026-11-01",
              }),
            ],
          });
      },
    });

    const wrapper = mount(Host, { global });

    return { wrapper, value };
  }

  it("renders the label and the selected range", () => {
    const { wrapper } = mountFilter({
      start: "2026-11-01",
      end: "2026-11-19",
    });

    expect(wrapper.get('button[aria-haspopup="dialog"]').text()).toContain(
      "Created at: 2026-11-01 ~ 2026-11-19"
    );
  });

  it("updates the value after a complete range and closes", async () => {
    const { wrapper, value } = mountFilter();

    await openDropdown(wrapper);
    await wrapper.get(cellSelector("2026-11-10")).trigger("click");
    await nextTick();
    await wrapper.get(cellSelector("2026-11-15")).trigger("mouseenter");
    await wrapper.get(cellSelector("2026-11-15")).trigger("click");

    expect(value.value).toEqual({
      start: "2026-11-10",
      end: "2026-11-15",
    });
  });

  it("registers with the filter group and is cleared with it", async () => {
    const { wrapper, value } = mountFilter({
      start: "2026-11-01",
      end: "2026-11-19",
    });

    await nextTick();

    const clearButton = wrapper.get('button[aria-label="Reset filters"]');
    expect(clearButton.exists()).toBe(true);

    await clearButton.trigger("click");
    expect(value.value).toBeUndefined();
  });

  it("does not emit when a partial range is discarded", async () => {
    const onChange = vi.fn();
    const wrapper = mount(VFilterDateRangeDropdown, {
      props: {
        label: "Created at",
        defaultPickerValue: "2026-11-01",
        "onUpdate:modelValue": onChange,
      },
      global,
    });

    await openDropdown(wrapper);
    await wrapper.get(cellSelector("2026-11-10")).trigger("click");
    await nextTick();

    wrapper.findComponent(VPopoverStub).vm.$emit("update:open", false);
    await nextTick();
    await openDropdown(wrapper);

    expect(onChange).not.toHaveBeenCalled();
  });
});
