import type { Meta, StoryObj } from "@storybook/vue3";

import { VButton } from ".";

const meta: Meta<typeof VButton> = {
  title: "Button",
  component: VButton,
  tags: ["autodocs"],
  render: (args) => ({
    components: { VButton },
    setup() {
      return { args };
    },
    template: `<VButton v-bind="args">${args.default}</VButton>`,
  }),
  argTypes: {
    default: {
      control: { type: "text" },
    },
    type: {
      control: { type: "select" },
      options: ["default", "primary", "secondary", "danger"],
    },
    size: {
      control: {
        type: "select",
      },
      options: ["lg", "md", "sm", "xs"],
    },
  },
  args: {
    default: "Button",
  },
};

export default meta;
type Story = StoryObj<typeof VButton>;

export const Default: Story = {
  args: {
    type: "default",
  },
};

export const Primary: Story = {
  args: {
    type: "primary",
  },
};

export const Secondary: Story = {
  args: {
    type: "secondary",
  },
};

export const Danger: Story = {
  args: {
    type: "danger",
  },
};
