import type { Meta, StoryObj } from "@storybook/vue3-vite";
import { ref } from "vue";
import { VDateTimePickerPanel } from ".";

const meta = {
  title: "Components/DateTimePickerPanel",
  component: VDateTimePickerPanel,
  tags: ["autodocs"],
  args: {
    modelValue: "2026-11-19T09:30",
    defaultPickerValue: "2026-11-01",
  },
  render: (args) => ({
    components: { VDateTimePickerPanel },
    setup() {
      const value = ref(args.modelValue);
      return { args, value };
    },
    template: `
      <div class="inline-block rounded-lg border border-gray-200 bg-white shadow-sm">
        <VDateTimePickerPanel v-bind="args" v-model="value" />
      </div>
    `,
  }),
} satisfies Meta<typeof VDateTimePickerPanel>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};

export const TwelveHour: Story = {
  args: {
    hourCycle: 12,
  },
};

export const CustomDefaultTime: Story = {
  args: {
    modelValue: undefined,
    defaultTime: "18:00",
  },
};
