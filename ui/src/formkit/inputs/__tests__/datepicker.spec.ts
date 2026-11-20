/* eslint-disable vue/require-default-prop -- Test stub props mirror the dropdown without defaults. */
import { createNode } from "@formkit/core";
import { initialValue } from "@formkit/inputs";
import { bindings } from "@formkit/vue";
import { mount } from "@vue/test-utils";
import { describe, expect, it, vi } from "vite-plus/test";
import { defineComponent, h, reactive } from "vue";
import { createI18n } from "vue-i18n";
import { datepicker } from "../datepicker";
import DatepickerInput from "../datepicker/DatepickerInput.vue";

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
    disabled: Boolean,
    calendarLabel: String,
    panelLabel: String,
  },
  emits: ["update:modelValue", "update:open"],
  setup(_, { emit, slots }) {
    return () =>
      h("div", [
        h("button", {
          class: "stub-trigger",
          onClick: () => emit("update:modelValue", "2026-11-20"),
        }),
        slots.trigger?.({ open: false }),
      ]);
  },
});

const messages = {
  "core.formkit.datepicker.calendar_label": "Calendar",
  "core.formkit.datepicker.placeholder": "Select date",
  "core.common.buttons.clear": "Clear",
  "core.common.buttons.confirm": "Confirm",
};

function mountInput(contextOverrides: Record<string, unknown> = {}) {
  const context = reactive({
    _value: "2026-11-19",
    attrs: {},
    classes: { input: "fake-input-class" },
    disabled: false,
    describedBy: "help-id",
    id: "date-1",
    placeholder: undefined,
    calendarLocale: "en",
    weekStartsOn: 1,
    numberOfMonths: 2,
    minDate: "2026-11-01",
    maxDate: "2026-11-30",
    format: undefined,
    clearable: true,
    state: { required: true },
    node: { input: vi.fn(), props: { disabled: false } },
    handlers: { blur: vi.fn() },
    ...contextOverrides,
  });

  const wrapper = mount(DatepickerInput, {
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

describe("datepicker", () => {
  it("registers an input definition with the expected props", () => {
    const node = createNode({
      props: { definition: datepicker, attrs: {} },
      value: "2026-11-19",
      plugins: [bindings],
    });

    expect(datepicker.type).toBe("input");
    expect(datepicker.props).toEqual(
      expect.arrayContaining([
        "minDate",
        "maxDate",
        "weekStartsOn",
        "calendarLocale",
      ])
    );
    expect(datepicker.features).toContain(initialValue);
    expect(node.value).toBe("2026-11-19");

    node.destroy();
  });

  it("passes context props to the dropdown", () => {
    const { wrapper } = mountInput();

    const dropdown = wrapper.findComponent(DropdownStub);
    expect(dropdown.props()).toMatchObject({
      modelValue: "2026-11-19",
      mode: "date",
      locale: "en",
      weekStartsOn: 1,
      numberOfMonths: 2,
      minValue: "2026-11-01",
      maxValue: "2026-11-30",
      disabled: false,
    });
  });

  it("writes dropdown changes to the FormKit node", async () => {
    const { wrapper, context } = mountInput();

    await wrapper.get(".stub-trigger").trigger("click");

    expect(context.node.input).toHaveBeenCalledWith("2026-11-20");
  });

  it("blurs the FormKit node when the dropdown closes", async () => {
    const { wrapper, context } = mountInput();
    const dropdown = wrapper.findComponent(DropdownStub);

    dropdown.vm.$emit("update:open", true);
    expect(context.handlers.blur).not.toHaveBeenCalled();

    dropdown.vm.$emit("update:open", false);
    expect(context.handlers.blur).toHaveBeenCalledTimes(1);
  });

  it("renders the trigger with the value and form attributes", () => {
    const { wrapper } = mountInput();

    const trigger = wrapper.get('button[aria-haspopup="dialog"]');
    expect(trigger.text()).toContain("2026-11-19");
    expect(trigger.classes()).toContain("fake-input-class");
    expect(trigger.attributes()).toMatchObject({
      id: "date-1",
      "aria-describedby": "help-id",
      "aria-required": "true",
    });
  });

  it("renders the formatted value when format is set", () => {
    const { wrapper } = mountInput({
      format: (value: string) => `formatted:${value}`,
    });

    expect(wrapper.get('button[aria-haspopup="dialog"]').text()).toContain(
      "formatted:2026-11-19"
    );
  });

  it("uses the localized placeholder when there is no value", () => {
    const { wrapper } = mountInput({ _value: undefined });

    expect(wrapper.get('button[aria-haspopup="dialog"]').text()).toContain(
      "Select date"
    );
    expect(wrapper.find('button[aria-label="Clear"]').exists()).toBe(false);
  });

  it("clears the FormKit node from the trigger", async () => {
    const { wrapper, context } = mountInput();

    await wrapper.get('button[aria-label="Clear"]').trigger("click");

    expect(context.node.input).toHaveBeenCalledWith(undefined);
  });

  it("hides the clear button when clearable is false", () => {
    const { wrapper } = mountInput({ clearable: false });

    expect(wrapper.find('button[aria-label="Clear"]').exists()).toBe(false);
  });
});
