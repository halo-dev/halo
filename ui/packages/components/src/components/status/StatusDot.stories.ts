import type { Meta, StoryObj } from "@storybook/vue3-vite";
import { VStatusDot } from ".";
import { VSpace } from "../space";

const meta: Meta<typeof VStatusDot> = {
  title: "Components/StatusDot",
  component: VStatusDot,
  tags: ["autodocs"],
  render: (args) => ({
    components: { VStatusDot },
    setup() {
      return { args };
    },
    template: `
      <VStatusDot
        :state="args.state"
        :text="args.text"
        :animate="args.animate"
      />
    `,
  }),
  argTypes: {
    state: {
      control: { type: "select" },
      options: ["default", "success", "warning", "error"],
    },
    animate: {
      control: { type: "boolean" },
    },
  },
  args: {
    state: "success",
    text: "运行中",
    animate: false,
  },
};

export default meta;
type Story = StoryObj<typeof VStatusDot>;

export const Default: Story = {};

export const States: Story = {
  render: () => ({
    components: { VSpace, VStatusDot },
    template: `
      <VSpace direction="column" align="start" spacing="sm">
        <VStatusDot state="success" text="已发布" />
        <VStatusDot state="warning" text="等待审核" />
        <VStatusDot state="error" text="同步失败" />
        <VStatusDot state="default" text="已归档" />
      </VSpace>
    `,
  }),
};

export const Realtime: Story = {
  args: {
    state: "success",
    text: "实时同步中",
    animate: true,
  },
};
