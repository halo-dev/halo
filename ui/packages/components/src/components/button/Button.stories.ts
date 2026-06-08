import type { Meta, StoryObj } from "@storybook/vue3-vite";
import { IconAddCircle, IconRefreshLine } from "@/icons/icons";
import { VButton } from ".";
import { VSpace } from "../space";

const meta: Meta<typeof VButton> = {
  title: "Components/Button",
  component: VButton,
  tags: ["autodocs"],
  render: (args) => ({
    components: { IconAddCircle, VButton },
    setup() {
      return { args };
    },
    template: `
      <VButton
        :type="args.type"
        :size="args.size"
        :block="args.block"
        :circle="args.circle"
        :disabled="args.disabled"
        :loading="args.loading"
        :ghost="args.ghost"
      >
        <template v-if="args.withIcon" #icon>
          <IconAddCircle />
        </template>
        {{ args.label }}
      </VButton>
    `,
  }),
  argTypes: {
    type: {
      control: { type: "select" },
      options: ["default", "primary", "secondary", "danger"],
    },
    size: {
      control: { type: "select" },
      options: ["lg", "md", "sm", "xs"],
    },
    label: {
      control: { type: "text" },
    },
    withIcon: {
      control: { type: "boolean" },
    },
  },
  args: {
    label: "发布文章",
    type: "primary",
    size: "md",
    block: false,
    circle: false,
    disabled: false,
    loading: false,
    ghost: false,
    withIcon: false,
  },
};

export default meta;
type Story = StoryObj<typeof VButton>;

export const Default: Story = {};

export const Variants: Story = {
  render: () => ({
    components: { VButton, VSpace },
    template: `
      <VSpace spacing="sm">
        <VButton>取消</VButton>
        <VButton type="primary">保存设置</VButton>
        <VButton type="secondary">预览站点</VButton>
        <VButton type="danger">删除</VButton>
      </VSpace>
    `,
  }),
};

export const Sizes: Story = {
  render: () => ({
    components: { VButton, VSpace },
    template: `
      <VSpace align="center" spacing="sm">
        <VButton size="lg" type="primary">新建页面</VButton>
        <VButton size="md" type="primary">新建页面</VButton>
        <VButton size="sm" type="primary">新建页面</VButton>
        <VButton size="xs" type="primary">新建页面</VButton>
      </VSpace>
    `,
  }),
};

export const WithIconAndLoading: Story = {
  render: () => ({
    components: { IconAddCircle, IconRefreshLine, VButton, VSpace },
    template: `
      <VSpace spacing="sm">
        <VButton type="primary">
          <template #icon>
            <IconAddCircle />
          </template>
          新建文章
        </VButton>
        <VButton loading type="secondary">
          同步中
        </VButton>
        <VButton ghost type="primary">
          <template #icon>
            <IconRefreshLine />
          </template>
          刷新缓存
        </VButton>
      </VSpace>
    `,
  }),
};
