import type { Meta, StoryObj } from "@storybook/vue3";

import { VAvatar } from ".";

const meta: Meta<typeof VAvatar> = {
  title: "Avatar",
  component: VAvatar,
  tags: ["autodocs"],
  render: (args) => ({
    components: { VAvatar },
    setup() {
      return { args };
    },
    template: `<VAvatar v-bind="args" />`,
  }),
  argTypes: {
    size: {
      control: { type: "select" },
      options: ["lg", "md", "sm", "xs"],
      defaultValue: "md",
    },
  },
};

export default meta;
type Story = StoryObj<typeof VAvatar>;

export const Default: Story = {
  args: {
    src: "https://www.halo.run/logo",
    alt: "Hello",
  },
};

export const Circle: Story = {
  args: {
    src: "https://www.halo.run/logo",
    alt: "Hello",
    circle: true,
  },
};

export const Text: Story = {
  args: {
    alt: "Ryan Wang",
  },
};
