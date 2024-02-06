import type { Meta, StoryObj } from "@storybook/vue3";

import { VSpace } from ".";
import { VButton } from "../button";

const meta: Meta<typeof VSpace> = {
  title: "Space",
  component: VSpace,
  tags: ["autodocs"],
  render: (args) => ({
    components: { VSpace, VButton },
    setup() {
      return { args };
    },
    template: `
      <VSpace
        v-bind="args"
      >
        <div>Control：</div>
        <VButton type="primary">确定</VButton>
        <VButton>取消</VButton>
      </VSpace>
    `,
  }),
  argTypes: {
    align: {
      control: { type: "select" },
      options: ["start", "end", "center", "stretch"],
    },
    spacing: {
      control: { type: "select" },
      options: ["xs", "sm", "md", "lg"],
    },
    direction: {
      control: { type: "select" },
      options: ["row", "column"],
    },
  },
  args: {
    align: "center",
    direction: "row",
    spacing: "xs",
  },
};

export default meta;
type Story = StoryObj<typeof VSpace>;

export const Default: Story = {
  args: {},
};
