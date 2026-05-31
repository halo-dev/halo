import type { Meta, StoryObj } from "@storybook/vue3-vite";
import { VSpace } from ".";
import { VButton } from "../button";

const meta: Meta<typeof VSpace> = {
  title: "Components/Space",
  component: VSpace,
  tags: ["autodocs"],
  render: (args) => ({
    components: { VButton, VSpace },
    setup() {
      return { args };
    },
    template: `
      <VSpace
        :align="args.align"
        :direction="args.direction"
        :spacing="args.spacing"
      >
        <VButton type="primary">保存</VButton>
        <VButton>预览</VButton>
        <VButton type="danger" ghost>删除</VButton>
      </VSpace>
    `,
  }),
  argTypes: {
    align: {
      control: { type: "select" },
      options: ["start", "end", "center", "stretch"],
    },
    direction: {
      control: { type: "select" },
      options: ["row", "column"],
    },
    spacing: {
      control: { type: "select" },
      options: ["xs", "sm", "md", "lg"],
    },
  },
  args: {
    align: "center",
    direction: "row",
    spacing: "sm",
  },
};

export default meta;
type Story = StoryObj<typeof VSpace>;

export const Row: Story = {};

export const Column: Story = {
  args: {
    align: "stretch",
    direction: "column",
    spacing: "xs",
  },
};
