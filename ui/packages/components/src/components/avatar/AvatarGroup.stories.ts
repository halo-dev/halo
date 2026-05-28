import type { Meta, StoryObj } from "@storybook/vue3-vite";
import { VAvatar, VAvatarGroup } from ".";

const meta: Meta<typeof VAvatarGroup> = {
  title: "Components/AvatarGroup",
  component: VAvatarGroup,
  tags: ["autodocs"],
  render: (args) => ({
    components: { VAvatar, VAvatarGroup },
    setup() {
      const members = ["Halo Admin", "Plugin QA", "Theme Lead", "Docs Owner"];
      return { args, members };
    },
    template: `
      <VAvatarGroup
        :size="args.size"
        :circle="args.circle"
        :width="args.width"
        :height="args.height"
      >
        <VAvatar v-for="member in members" :key="member" :alt="member" />
      </VAvatarGroup>
    `,
  }),
  argTypes: {
    size: {
      control: { type: "select" },
      options: ["lg", "md", "sm", "xs"],
    },
  },
  args: {
    size: "md",
    circle: true,
  },
};

export default meta;
type Story = StoryObj<typeof VAvatarGroup>;

export const Default: Story = {};

export const Square: Story = {
  args: {
    circle: false,
  },
};

export const SmallTeam: Story = {
  args: {
    size: "sm",
    circle: true,
  },
};
