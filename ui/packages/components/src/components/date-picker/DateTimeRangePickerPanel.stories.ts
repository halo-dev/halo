import type { Meta, StoryObj } from "@storybook/vue3-vite";
import { ref } from "vue";
import { VDateTimeRangePickerPanel } from ".";
import type { DateRangeValue } from "./types";

const meta = {
  title: "Components/DateTimeRangePickerPanel",
  component: VDateTimeRangePickerPanel,
  tags: ["autodocs"],
  args: {
    modelValue: {
      start: "2026-11-05T08:00",
      end: "2026-11-19T18:00",
    },
    defaultPickerValue: "2026-11-01",
  },
  render: (args) => ({
    components: { VDateTimeRangePickerPanel },
    setup() {
      const value = ref<DateRangeValue | undefined>(args.modelValue);
      return { args, value };
    },
    template: `
      <div class="inline-block rounded-lg border border-gray-200 bg-white shadow-sm">
        <VDateTimeRangePickerPanel v-bind="args" v-model="value" />
      </div>
    `,
  }),
} satisfies Meta<typeof VDateTimeRangePickerPanel>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};

export const TwelveHour: Story = {
  args: {
    hourCycle: 12,
  },
};

export const CustomDefaultTimes: Story = {
  args: {
    modelValue: undefined,
    defaultStartTime: "08:00",
    defaultEndTime: "18:00",
  },
};
