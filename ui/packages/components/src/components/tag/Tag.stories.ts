import type { Meta, StoryObj } from "@storybook/vue3";

import { VTag } from ".";

const meta: Meta<typeof VTag> = {
  title: "Tag",
  component: VTag,
  tags: ["autodocs"],
  render: (args) => ({
    components: { VTag },
    setup() {
      return { args };
    },
    template: `<VTag v-bind="args">${args.default}<template v-if="${
      "leftIcon" in args
    }" #leftIcon>${args.leftIcon}</template></VTag>`,
  }),
  argTypes: {
    default: {
      control: { type: "text" },
    },
    theme: {
      control: { type: "select" },
      options: ["default", "primary", "secondary", "danger"],
    },
  },
  args: {
    default: "Tag",
    theme: "default",
  },
};

export default meta;
type Story = StoryObj<typeof VTag>;

export const Default: Story = {
  args: {
    theme: "default",
  },
};

export const Primary: Story = {
  args: {
    theme: "primary",
  },
};

export const Secondary: Story = {
  args: {
    theme: "secondary",
  },
};

export const Danger: Story = {
  args: {
    theme: "danger",
  },
};

export const Icon: Story = {
  args: {
    leftIcon: `<IconSettings />`,
  },
};
