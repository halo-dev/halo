import type { Meta, StoryObj } from "@storybook/vue3";

import { VLoading } from ".";

const meta: Meta<typeof VLoading> = {
  title: "Loading",
  component: VLoading,
  tags: ["autodocs"],
  render: (args) => ({
    components: { VLoading },
    setup() {
      return { args };
    },
    template: `<VLoading v-bind="args" />`,
  }),
};

export default meta;
type Story = StoryObj<typeof VLoading>;

export const Default: Story = {
  args: {},
};
