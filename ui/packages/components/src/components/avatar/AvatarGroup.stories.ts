import type { Meta, StoryObj } from "@storybook/vue3";

import { VAvatar, VAvatarGroup } from ".";

const meta: Meta<typeof VAvatarGroup> = {
  title: "AvatarGroup",
  component: VAvatarGroup,
  tags: ["autodocs"],
  render: (args) => ({
    components: { VAvatarGroup, VAvatar },
    setup() {
      return { args };
    },
    template: `<VAvatarGroup v-bind="args">
      <VAvatar src="https://avatar.iran.liara.run/public?id=1" />
      <VAvatar src="https://avatar.iran.liara.run/public?id=2" />
      <VAvatar src="https://avatar.iran.liara.run/public?id=3" />
      <VAvatar src="https://avatar.iran.liara.run/public?id=4" />
    </VAvatarGroup>`,
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
type Story = StoryObj<typeof VAvatarGroup>;

export const Default: Story = {
  args: {},
};

export const Circle: Story = {
  args: {
    circle: true,
  },
};
