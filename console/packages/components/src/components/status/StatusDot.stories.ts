import type { Meta, StoryObj } from "@storybook/vue3";
import { VStatusDot } from ".";

const meta: Meta<typeof VStatusDot> = {
  title: "StatusDot",
  component: VStatusDot,
  tags: ["autodocs"],
  render: (args) => ({
    components: { VStatusDot },
    setup() {
      return {
        args,
      };
    },
    template: `
        <VStatusDot v-bind="args" />
    `,
  }),
  argTypes: {
    state: {
      control: { type: "select" },
      options: ["default", "success", "warning", "error"],
    },
  },
};

export default meta;
type Story = StoryObj<typeof VStatusDot>;

export const Default: Story = {
  args: {
    state: "default",
    text: "默认",
  },
};

export const Success: Story = {
  args: {
    state: "success",
    text: "成功",
  },
};

export const Warning: Story = {
  args: {
    state: "warning",
    text: "警告",
  },
};

export const Error: Story = {
  args: {
    state: "error",
    text: "错误",
  },
};
