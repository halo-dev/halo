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
        :size="args.size"
        :color="args.color"
        :label="args.label"
        :title="args.title"
      />
    `,
  }),
  argTypes: {
    state: {
      control: { type: "select" },
      options: ["default", "success", "warning", "error", "info", "processing"],
    },
    size: {
      control: { type: "select" },
      options: ["sm", "md"],
    },
    animate: {
      control: { type: "boolean" },
    },
    color: {
      control: { type: "color" },
    },
    label: {
      control: { type: "text" },
      description:
        "Accessible name of the dot, which is also rendered as its tooltip. Only output to the DOM when no visible text is provided.",
    },
  },
  args: {
    state: "success",
    text: "运行中",
    animate: false,
    size: "md",
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
        <VStatusDot state="processing" text="发布中" />
        <VStatusDot state="warning" text="等待审核" />
        <VStatusDot state="error" text="同步失败" />
        <VStatusDot state="info" text="已暂停" />
        <VStatusDot state="default" text="已归档" />
      </VSpace>
    `,
  }),
};

export const Processing: Story = {
  args: {
    state: "processing",
    text: "发布中",
  },
};

export const Sizes: Story = {
  render: () => ({
    components: { VSpace, VStatusDot },
    template: `
      <VSpace direction="column" align="start" spacing="sm">
        <VStatusDot size="sm" text="小尺寸" />
        <VStatusDot size="md" text="默认尺寸" />
      </VSpace>
    `,
  }),
};

export const CustomColor: Story = {
  args: {
    state: "default",
    color: "#8B5CF6",
    text: "自定义颜色",
  },
};

export const Label: Story = {
  render: () => ({
    components: { VSpace, VStatusDot },
    template: `
      <VSpace direction="column" align="start" spacing="sm">
        <VStatusDot state="processing" label="发布中，悬停查看提示" />
        <VStatusDot state="warning" label="删除中，悬停查看提示" />
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
