import type { Meta, StoryObj } from "@storybook/vue3-vite";
import { ref } from "vue";
import { VFilterDateRangeDropdown, VFilterDropdownGroup } from ".";
import type { DateRangeValue } from "../date-picker/types";

const meta = {
  title: "Components/FilterDateRangeDropdown",
  component: VFilterDateRangeDropdown,
  tags: ["autodocs"],
  args: {
    label: "Created at",
    defaultPickerValue: "2026-11-01",
  },
  render: (args) => ({
    components: { VFilterDateRangeDropdown, VFilterDropdownGroup },
    setup() {
      const value = ref<DateRangeValue | undefined>(args.modelValue);
      return { args, value };
    },
    template: `
      <div class="h-96 p-4">
        <VFilterDropdownGroup>
          <VFilterDateRangeDropdown v-bind="args" v-model="value" />
        </VFilterDropdownGroup>
      </div>
    `,
  }),
} satisfies Meta<typeof VFilterDateRangeDropdown>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};

export const Outline: Story = {
  args: {
    variant: "outline",
  },
};

export const WithValue: Story = {
  args: {
    modelValue: { start: "2026-11-05", end: "2026-11-19" },
  },
};

export const DateTimeRange: Story = {
  args: {
    mode: "datetime-range",
    modelValue: {
      start: "2026-11-05T08:00",
      end: "2026-11-19T18:00",
    },
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
        label: "This month",
        value: { start: "2026-11-01", end: "2026-11-19" },
      },
    ],
  },
};
