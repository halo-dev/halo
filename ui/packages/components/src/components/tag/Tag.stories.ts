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
        :size="args.size"
        :variant="args.variant"
        :rounded="args.rounded"
        :closable="args.closable"
        :disabled="args.disabled"
        :clickable="args.clickable"
        :checkable="args.checkable"
        :max-width="args.maxWidth"
        :color="args.color"
        :title="args.title"
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
      options: [
        "default",
        "primary",
        "secondary",
        "danger",
        "success",
        "warning",
        "info",
      ],
    },
    size: {
      control: { type: "select" },
      options: ["sm", "md", "lg"],
    },
    variant: {
      control: { type: "select" },
      options: ["solid", "soft"],
    },
    rounded: {
      control: { type: "boolean" },
    },
    closable: {
      control: { type: "boolean" },
    },
    disabled: {
      control: { type: "boolean" },
    },
    clickable: {
      control: { type: "boolean" },
    },
    checkable: {
      control: { type: "boolean" },
    },
    maxWidth: {
      control: { type: "text" },
    },
    color: {
      control: { type: "color" },
    },
    title: {
      control: { type: "text" },
    },
    withIcon: {
      control: { type: "boolean" },
    },
  },
  args: {
    label: "默认",
    theme: "default",
    size: "sm",
    variant: "solid",
    rounded: false,
    closable: false,
    disabled: false,
    clickable: false,
    checkable: false,
    maxWidth: "12rem",
    color: undefined,
    title: undefined,
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
      <VSpace direction="column" align="start" spacing="sm">
        <VSpace spacing="sm">
          <VTag>草稿</VTag>
          <VTag theme="primary">已发布</VTag>
          <VTag theme="secondary">置顶</VTag>
          <VTag theme="danger">审核失败</VTag>
          <VTag theme="success">运行中</VTag>
          <VTag theme="warning">待审核</VTag>
          <VTag theme="info">计划发布</VTag>
        </VSpace>
        <VSpace spacing="sm">
          <VTag variant="soft">草稿</VTag>
          <VTag theme="primary" variant="soft">已发布</VTag>
          <VTag theme="secondary" variant="soft">置顶</VTag>
          <VTag theme="danger" variant="soft">审核失败</VTag>
          <VTag theme="success" variant="soft">运行中</VTag>
          <VTag theme="warning" variant="soft">待审核</VTag>
          <VTag theme="info" variant="soft">计划发布</VTag>
        </VSpace>
      </VSpace>
    `,
  }),
};

export const Sizes: Story = {
  render: () => ({
    components: { IconSettings, VSpace, VTag },
    template: `
      <VSpace direction="column" align="start" spacing="sm">
        <VSpace align="center" spacing="sm">
          <VTag size="sm">默认尺寸</VTag>
          <VTag size="md">中等尺寸</VTag>
          <VTag size="lg">大尺寸</VTag>
        </VSpace>
        <VSpace align="center" spacing="sm">
          <VTag size="sm" theme="primary">
            <template #leftIcon><IconSettings /></template>
            默认尺寸
          </VTag>
          <VTag size="md" theme="primary">
            <template #leftIcon><IconSettings /></template>
            中等尺寸
          </VTag>
          <VTag size="lg" theme="primary">
            <template #leftIcon><IconSettings /></template>
            大尺寸
          </VTag>
        </VSpace>
      </VSpace>
    `,
  }),
};

export const LongText: Story = {
  render: () => ({
    components: { VSpace, VTag },
    template: `
      <VSpace direction="column" align="start" spacing="sm">
        <VTag max-width="8rem">一个非常非常长的文章标签名称会撑破布局</VTag>
        <VTag max-width="16rem">一个非常非常长的文章标签名称会撑破布局</VTag>
        <VTag max-width="8rem" theme="primary">一个非常非常长的文章标签名称会撑破布局</VTag>
      </VSpace>
    `,
  }),
};

export const States: Story = {
  render: () => ({
    components: { VSpace, VTag },
    template: `
      <VSpace direction="column" align="start" spacing="sm">
        <VSpace align="center" spacing="sm">
          <VTag clickable>可点击</VTag>
          <VTag closable>可关闭</VTag>
          <VTag closable theme="primary">可关闭</VTag>
          <VTag disabled>不可用</VTag>
          <VTag rounded theme="secondary">圆角</VTag>
        </VSpace>
        <VSpace align="center" spacing="sm">
          <VTag checkable>未选中</VTag>
          <VTag checkable default-checked>已选中</VTag>
          <VTag checkable default-checked theme="success" variant="soft">已选中</VTag>
        </VSpace>
      </VSpace>
    `,
  }),
};

export const CustomColor: Story = {
  render: () => ({
    components: { VSpace, VTag },
    template: `
      <VSpace spacing="sm">
        <VTag color="#FDE68A">浅色背景</VTag>
        <VTag color="#312E81">深色背景</VTag>
        <VTag color="#4CCBA0" rounded>品牌色</VTag>
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
