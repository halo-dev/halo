import type { Meta, StoryObj } from "@storybook/vue3-vite";
import { IconSettings } from "@/icons/icons";
import { VTag } from ".";
import { VSpace } from "../space";

const meta: Meta<typeof VTag> = {
  title: "Components/Tag",
  component: VTag,
  tags: ["autodocs"],
  render: (args) => ({
    components: { IconSettings, VTag },
    setup() {
      return { args };
    },
    template: `
      <VTag
        :theme="args.theme"
        :rounded="args.rounded"
      >
        <template v-if="args.withIcon" #leftIcon>
          <IconSettings />
        </template>
        {{ args.label }}
      </VTag>
    `,
  }),
  argTypes: {
    theme: {
      control: { type: "select" },
      options: ["default", "primary", "secondary", "danger"],
    },
    rounded: {
      control: { type: "boolean" },
    },
    withIcon: {
      control: { type: "boolean" },
    },
  },
  args: {
    label: "默认",
    theme: "default",
    rounded: false,
    withIcon: false,
  },
};

export default meta;
type Story = StoryObj<typeof VTag>;

export const Default: Story = {};

export const Themes: Story = {
  render: () => ({
    components: { VSpace, VTag },
    template: `
      <VSpace spacing="sm">
        <VTag>草稿</VTag>
        <VTag theme="primary">已发布</VTag>
        <VTag theme="secondary">置顶</VTag>
        <VTag theme="danger">审核失败</VTag>
      </VSpace>
    `,
  }),
};

export const RoundedWithIcon: Story = {
  args: {
    label: "系统设置",
    theme: "primary",
    rounded: true,
    withIcon: true,
  },
};
