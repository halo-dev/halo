/* eslint-disable vue/one-component-per-file -- Test-local host components keep the v-model flow isolated. */
import { mount } from "@vue/test-utils";
import { describe, expect, it, vi } from "vite-plus/test";
import { defineComponent, h, ref } from "vue";
import {
  VDatePickerPanel,
  VDateTimePickerPanel,
  VDateTimeRangePickerPanel,
  VDateRangePickerPanel,
} from "..";
import TimeField from "../TimeField.vue";
import type { DateRangeValue } from "../types";

function cellSelector(value: string) {
  return `[data-reka-calendar-cell-trigger][data-value="${value}"]`;
}

describe("DatePickerPanel", () => {
  it("emits YYYY-MM-DD when a date is selected", async () => {
    const wrapper = mount(VDatePickerPanel, {
      props: {
        modelValue: "2026-11-10",
        defaultPickerValue: "2026-11-01",
      },
    });

    await wrapper.get(cellSelector("2026-11-15")).trigger("click");

    expect(wrapper.emitted("update:modelValue")?.[0]).toEqual(["2026-11-15"]);
  });

  it("forwards min and max values as disabled dates", () => {
    const wrapper = mount(VDatePickerPanel, {
      props: {
        defaultPickerValue: "2026-11-01",
        minValue: "2026-11-10",
        maxValue: "2026-11-20",
      },
    });

    expect(wrapper.get(cellSelector("2026-11-09")).attributes()).toHaveProperty(
      "data-disabled"
    );
    expect(wrapper.get(cellSelector("2026-11-21")).attributes()).toHaveProperty(
      "data-disabled"
    );
    expect(
      wrapper.get(cellSelector("2026-11-15")).attributes()
    ).not.toHaveProperty("data-disabled");
  });

  it("passes YYYY-MM-DD to isDateDisabled", () => {
    const isDateDisabled = vi.fn((date: string) => date === "2026-11-15");

    const wrapper = mount(VDatePickerPanel, {
      props: {
        defaultPickerValue: "2026-11-01",
        isDateDisabled,
      },
    });

    expect(isDateDisabled).toHaveBeenCalledWith("2026-11-15");
    expect(wrapper.get(cellSelector("2026-11-15")).attributes()).toHaveProperty(
      "data-disabled"
    );
  });
});

describe("DateRangePickerPanel", () => {
  it("emits one endpoint after the first click and the full range after the second", async () => {
    const value = ref<DateRangeValue>();
    const wrapper = mount(
      defineComponent({
        setup() {
          return () =>
            h(VDateRangePickerPanel, {
              modelValue: value.value,
              "onUpdate:modelValue": (next: DateRangeValue | undefined) => {
                value.value = next;
              },
              defaultPickerValue: "2026-11-01",
            });
        },
      })
    );

    await wrapper.get(cellSelector("2026-11-10")).trigger("click");
    await wrapper.vm.$nextTick();
    expect(value.value).toEqual({ start: "2026-11-10" });

    await wrapper.get(cellSelector("2026-11-15")).trigger("mouseenter");
    await wrapper.get(cellSelector("2026-11-15")).trigger("click");
    expect(value.value).toEqual({
      start: "2026-11-10",
      end: "2026-11-15",
    });
  });

  it("applies presets and supports only one endpoint", async () => {
    const wrapper = mount(VDateRangePickerPanel, {
      props: {
        defaultPickerValue: "2026-11-01",
        presets: [
          { label: "Only start", value: { start: "2026-11-05" } },
          {
            label: "Full range",
            value: () => ({ start: "2026-11-01", end: "2026-11-19" }),
          },
        ],
      },
    });

    await wrapper.get("button").trigger("click");
    expect(wrapper.emitted("update:modelValue")?.[0]).toEqual([
      { start: "2026-11-05" },
    ]);

    const buttons = wrapper.findAll("button");
    await buttons[1].trigger("click");
    expect(wrapper.emitted("update:modelValue")?.[1]).toEqual([
      { start: "2026-11-01", end: "2026-11-19" },
    ]);
  });
});

describe("DateTimePickerPanel", () => {
  it("keeps the time when a date is selected", async () => {
    const wrapper = mount(VDateTimePickerPanel, {
      props: {
        modelValue: "2026-11-10T09:30",
        defaultPickerValue: "2026-11-01",
      },
    });

    await wrapper.get(cellSelector("2026-11-15")).trigger("click");

    expect(wrapper.emitted("update:modelValue")?.[0]).toEqual([
      "2026-11-15T09:30",
    ]);
  });

  it("does not emit when the time changes without a date", async () => {
    const wrapper = mount(VDateTimePickerPanel, {
      props: {
        defaultPickerValue: "2026-11-01",
      },
    });

    const timeField = wrapper.findComponent(TimeField);
    timeField.vm.$emit("update:modelValue", "10:45");
    await wrapper.vm.$nextTick();

    expect(wrapper.emitted("update:modelValue")).toBeUndefined();

    await wrapper.get(cellSelector("2026-11-15")).trigger("click");
    expect(wrapper.emitted("update:modelValue")?.[0]).toEqual([
      "2026-11-15T10:45",
    ]);
  });
});

describe("DateTimeRangePickerPanel", () => {
  it("uses the configured default times for new endpoints", async () => {
    const value = ref<DateRangeValue>();
    const wrapper = mount(
      defineComponent({
        setup() {
          return () =>
            h(VDateTimeRangePickerPanel, {
              modelValue: value.value,
              "onUpdate:modelValue": (next: DateRangeValue | undefined) => {
                value.value = next;
              },
              defaultPickerValue: "2026-11-01",
              defaultStartTime: "08:00",
              defaultEndTime: "18:00",
            });
        },
      })
    );

    await wrapper.get(cellSelector("2026-11-10")).trigger("click");
    await wrapper.vm.$nextTick();
    expect(value.value).toEqual({ start: "2026-11-10T08:00" });

    await wrapper.get(cellSelector("2026-11-15")).trigger("mouseenter");
    await wrapper.get(cellSelector("2026-11-15")).trigger("click");
    expect(value.value).toEqual({
      start: "2026-11-10T08:00",
      end: "2026-11-15T18:00",
    });
  });

  it("updates the time of an existing endpoint", async () => {
    const wrapper = mount(VDateTimeRangePickerPanel, {
      props: {
        modelValue: { start: "2026-11-10T08:00" },
        defaultPickerValue: "2026-11-01",
      },
    });

    const timeFields = wrapper.findAllComponents(TimeField);
    expect(timeFields).toHaveLength(2);
    expect(timeFields[1].props("disabled")).toBe(true);

    timeFields[0].vm.$emit("update:modelValue", "09:30");
    await wrapper.vm.$nextTick();

    expect(wrapper.emitted("update:modelValue")?.[0]).toEqual([
      { start: "2026-11-10T09:30" },
    ]);
  });
});
