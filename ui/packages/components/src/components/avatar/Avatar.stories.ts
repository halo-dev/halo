import type { Meta, StoryObj } from "@storybook/vue3-vite";
import { VAvatar } from ".";
import { VSpace } from "../space";

const avatarImage =
  "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 96 96'%3E%3Crect width='96' height='96' rx='20' fill='%234CCBA0'/%3E%3Ccircle cx='48' cy='38' r='18' fill='white' fill-opacity='.9'/%3E%3Cpath d='M18 82c5-18 17-27 30-27s25 9 30 27' fill='white' fill-opacity='.9'/%3E%3C/svg%3E";

const meta: Meta<typeof VAvatar> = {
  title: "Components/Avatar",
  component: VAvatar,
  tags: ["autodocs"],
  render: (args) => ({
    components: { VAvatar },
    setup() {
      return { args };
    },
    template: `
      <VAvatar
        :src="args.src"
        :alt="args.alt"
        :size="args.size"
        :width="args.width"
        :height="args.height"
        :circle="args.circle"
      />
    `,
  }),
  argTypes: {
    size: {
      control: { type: "select" },
      options: ["lg", "md", "sm", "xs"],
    },
  },
  args: {
    src: avatarImage,
    alt: "Halo Admin",
    size: "md",
    circle: false,
  },
};

export default meta;
type Story = StoryObj<typeof VAvatar>;

export const Default: Story = {};

export const Initials: Story = {
  args: {
    src: undefined,
    alt: "Ryan Wang",
  },
};

export const Sizes: Story = {
  render: () => ({
    components: { VAvatar, VSpace },
    setup() {
      return { avatarImage };
    },
    template: `
      <VSpace align="center" spacing="md">
        <VAvatar :src="avatarImage" alt="Large Avatar" size="lg" />
        <VAvatar :src="avatarImage" alt="Medium Avatar" size="md" />
        <VAvatar :src="avatarImage" alt="Small Avatar" size="sm" />
        <VAvatar :src="avatarImage" alt="Extra Small Avatar" size="xs" />
      </VSpace>
    `,
  }),
};

export const Circle: Story = {
  args: {
    circle: true,
  },
};
