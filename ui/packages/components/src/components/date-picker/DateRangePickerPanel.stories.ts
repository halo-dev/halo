import type { Meta, StoryObj } from "@storybook/vue3-vite";
import { ref } from "vue";
import { VDateRangePickerPanel } from ".";
import type { DateRangeValue } from "./types";

const meta = {
  title: "Components/DateRangePickerPanel",
  component: VDateRangePickerPanel,
  tags: ["autodocs"],
  args: {
    modelValue: { start: "2026-11-05", end: "2026-11-19" },
    defaultPickerValue: "2026-11-01",
  },
  render: (args) => ({
    components: { VDateRangePickerPanel },
    setup() {
      const value = ref<DateRangeValue | undefined>(args.modelValue);
      return { args, value };
    },
    template: `
      <div class="inline-block rounded-lg border border-gray-200 bg-white shadow-sm">
        <VDateRangePickerPanel v-bind="args" v-model="value" />
      </div>
    `,
  }),
} satisfies Meta<typeof VDateRangePickerPanel>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};

export const OneMonth: Story = {
  args: {
    numberOfMonths: 1,
  },
};

export const WithPresets: Story = {
  args: {
    presets: [
      {
        label: "Today",
        value: () => ({ start: "2026-11-19", end: "2026-11-19" }),
      },
      {
        label: "Last 7 days",
        value: { start: "2026-11-13", end: "2026-11-19" },
      },
      {
        label: "Last 30 days",
        value: { start: "2026-10-21", end: "2026-11-19" },
      },
      {
        label: "This month",
        value: { start: "2026-11-01", end: "2026-11-19" },
      },
    ],
  },
};

export const MaximumDays: Story = {
  args: {
    maximumDays: 7,
  },
};
