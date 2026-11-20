/* eslint-disable vue/require-default-prop -- Test stub props mirror the dropdown without defaults. */
import { createNode } from "@formkit/core";
import { initialValue } from "@formkit/inputs";
import { bindings } from "@formkit/vue";
import { mount } from "@vue/test-utils";
import { describe, expect, it, vi } from "vite-plus/test";
import { defineComponent, h, reactive } from "vue";
import { createI18n } from "vue-i18n";
import { datetimepicker } from "../datetimepicker";
import DatetimepickerInput from "../datetimepicker/DatetimepickerInput.vue";

const DropdownStub = defineComponent({
  name: "VDatePickerDropdown",
  props: {
    modelValue: String,
    mode: String,
    locale: String,
    weekStartsOn: Number,
    numberOfMonths: Number,
    minValue: String,
    maxValue: String,
    defaultTime: String,
    hourCycle: Number,
    timeLabel: String,
    disabled: Boolean,
    calendarLabel: String,
    panelLabel: String,
    clearLabel: String,
    confirmLabel: String,
  },
  emits: ["update:modelValue", "update:open"],
  setup(_, { emit, slots }) {
    return () =>
      h("div", [
        h("button", {
          class: "stub-trigger",
          onClick: () => emit("update:modelValue", "2026-11-20T10:30"),
        }),
        slots.trigger?.({ open: false }),
      ]);
  },
});

const messages = {
  "core.formkit.datepicker.calendar_label": "Calendar",
  "core.formkit.datetimepicker.placeholder": "Select date and time",
  "core.formkit.datetimepicker.time": "Time",
  "core.common.buttons.clear": "Clear",
  "core.common.buttons.confirm": "Confirm",
};

function mountInput(contextOverrides: Record<string, unknown> = {}) {
  const context = reactive({
    _value: "2026-11-19T09:30",
    attrs: {},
    classes: { input: "fake-input-class" },
    disabled: false,
    describedBy: undefined,
    id: "datetime-1",
    placeholder: undefined,
    calendarLocale: "en",
    weekStartsOn: undefined,
    numberOfMonths: undefined,
    minDate: undefined,
    maxDate: undefined,
    format: undefined,
    clearable: true,
    defaultTime: "08:00",
    hourCycle: 12,
    state: { required: false },
    node: { input: vi.fn(), props: { disabled: false } },
    handlers: { blur: vi.fn() },
    ...contextOverrides,
  });

  const wrapper = mount(DatetimepickerInput, {
    props: { context },
    global: {
      plugins: [
        createI18n({
          legacy: false,
          locale: "en",
          messages: { en: messages },
        }),
      ],
      stubs: { DatePickerDropdown: DropdownStub },
    },
  });

  return { wrapper, context };
}

describe("datetimepicker", () => {
  it("registers an input definition with the expected props", () => {
    const node = createNode({
      props: { definition: datetimepicker, attrs: {} },
      value: "2026-11-19T09:30",
      plugins: [bindings],
    });

    expect(datetimepicker.type).toBe("input");
    expect(datetimepicker.props).toEqual(
      expect.arrayContaining([
        "defaultTime",
        "hourCycle",
        "minDate",
        "calendarLocale",
      ])
    );
    expect(datetimepicker.features).toContain(initialValue);
    expect(node.value).toBe("2026-11-19T09:30");

    node.destroy();
  });

  it("passes datetime props to the dropdown", () => {
    const { wrapper } = mountInput();

    const dropdown = wrapper.findComponent(DropdownStub);
    expect(dropdown.props()).toMatchObject({
      modelValue: "2026-11-19T09:30",
      mode: "datetime",
      locale: "en",
      defaultTime: "08:00",
      hourCycle: 12,
      timeLabel: "Time",
      clearLabel: "Clear",
      confirmLabel: "Confirm",
      disabled: false,
    });
  });

  it("writes dropdown changes to the FormKit node", async () => {
    const { wrapper, context } = mountInput();

    await wrapper.get(".stub-trigger").trigger("click");

    expect(context.node.input).toHaveBeenCalledWith("2026-11-20T10:30");
  });

  it("blurs the FormKit node when the dropdown closes", () => {
    const { wrapper, context } = mountInput();
    const dropdown = wrapper.findComponent(DropdownStub);

    dropdown.vm.$emit("update:open", true);
    expect(context.handlers.blur).not.toHaveBeenCalled();

    dropdown.vm.$emit("update:open", false);
    expect(context.handlers.blur).toHaveBeenCalledTimes(1);
  });

  it("renders the trigger with the datetime value and attributes", () => {
    const { wrapper } = mountInput();

    const trigger = wrapper.get('button[aria-haspopup="dialog"]');
    expect(trigger.text()).toContain("2026-11-19 09:30");
    expect(trigger.classes()).toContain("fake-input-class");
    expect(trigger.attributes()).toMatchObject({
      id: "datetime-1",
    });
  });

  it("uses the localized placeholder when there is no value", () => {
    const { wrapper } = mountInput({ _value: undefined });

    expect(wrapper.get('button[aria-haspopup="dialog"]').text()).toContain(
      "Select date and time"
    );
  });

  it("clears the FormKit node from the trigger", async () => {
    const { wrapper, context } = mountInput();

    await wrapper.get('button[aria-label="Clear"]').trigger("click");

    expect(context.node.input).toHaveBeenCalledWith(undefined);
  });
});
