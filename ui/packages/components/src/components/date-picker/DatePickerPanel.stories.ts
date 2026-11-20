import type { Meta, StoryObj } from "@storybook/vue3-vite";
import { ref } from "vue";
import { VDatePickerPanel } from ".";

const meta = {
  title: "Components/DatePickerPanel",
  component: VDatePickerPanel,
  tags: ["autodocs"],
  args: {
    modelValue: "2026-11-19",
    defaultPickerValue: "2026-11-01",
  },
  render: (args) => ({
    components: { VDatePickerPanel },
    setup() {
      const value = ref(args.modelValue);
      return { args, value };
    },
    template: `
      <div class="inline-block rounded-lg border border-gray-200 bg-white shadow-sm">
        <VDatePickerPanel v-bind="args" v-model="value" />
      </div>
    `,
  }),
} satisfies Meta<typeof VDatePickerPanel>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};

export const WithoutValue: Story = {
  args: {
    modelValue: undefined,
  },
};

export const DisabledDates: Story = {
  args: {
    isDateDisabled: (date: string) =>
      date === "2026-11-20" || date === "2026-11-21",
  },
};

export const TwoMonths: Story = {
  args: {
    numberOfMonths: 2,
  },
};

export const Disabled: Story = {
  args: {
    disabled: true,
  },
};
