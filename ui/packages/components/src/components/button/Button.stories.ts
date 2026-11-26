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
        :variant="args.variant"
        :size="args.size"
        :shape="args.shape"
        :block="args.block"
        :circle="args.circle"
        :disabled="args.disabled"
        :loading="args.loading"
      >
        <template v-if="args.withIcon" #icon>
          <IconAddCircle />
        </template>
        {{ args.text }}
      </VButton>
    `,
  }),
  argTypes: {
    type: {
      control: { type: "select" },
      options: ["default", "primary", "secondary", "danger"],
    },
    variant: {
      control: { type: "select" },
      options: ["solid", "soft", "ghost"],
    },
    size: {
      control: { type: "select" },
      options: ["lg", "md", "sm", "xs"],
    },
    shape: {
      control: { type: "select" },
      options: ["default", "circle", "round"],
    },
    text: {
      control: { type: "text" },
    },
    withIcon: {
      control: { type: "boolean" },
    },
  },
  args: {
    text: "发布文章",
    type: "primary",
    variant: "solid",
    size: "md",
    shape: "default",
    block: false,
    circle: false,
    disabled: false,
    loading: false,
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
      <VSpace direction="column" align="start" spacing="md">
        <VSpace spacing="sm">
          <VButton>取消</VButton>
          <VButton type="primary">保存设置</VButton>
          <VButton type="secondary">预览站点</VButton>
          <VButton type="danger">删除</VButton>
        </VSpace>
        <VSpace spacing="sm">
          <VButton variant="soft">取消</VButton>
          <VButton variant="soft" type="primary">保存设置</VButton>
          <VButton variant="soft" type="secondary">预览站点</VButton>
          <VButton variant="soft" type="danger">删除</VButton>
        </VSpace>
        <VSpace spacing="sm">
          <VButton variant="ghost">取消</VButton>
          <VButton variant="ghost" type="primary">保存设置</VButton>
          <VButton variant="ghost" type="secondary">预览站点</VButton>
          <VButton variant="ghost" type="danger">删除</VButton>
        </VSpace>
      </VSpace>
    `,
  }),
};

export const Shapes: Story = {
  render: () => ({
    components: { IconAddCircle, VButton, VSpace },
    template: `
      <VSpace align="center" spacing="sm">
        <VButton type="primary">默认</VButton>
        <VButton shape="round" type="primary">胶囊</VButton>
        <VButton :label="'新建'" shape="circle" type="primary">
          <template #icon><IconAddCircle /></template>
        </VButton>
      </VSpace>
    `,
  }),
};

export const AsLink: Story = {
  render: () => ({
    components: { IconAddCircle, VButton, VSpace },
    template: `
      <VSpace align="center" spacing="sm">
        <VButton as="a" href="https://halo.run" target="_blank">
          在新窗口打开
        </VButton>
        <VButton as="a" href="https://halo.run" target="_blank" variant="ghost">
          <template #icon><IconAddCircle /></template>
          在新窗口打开
        </VButton>
        <VButton as="a" href="https://halo.run" target="_blank" disabled>
          禁用状态
        </VButton>
      </VSpace>
    `,
  }),
};

export const Sizes: Story = {
  render: () => ({
    components: { IconAddCircle, VButton, VSpace },
    template: `
      <VSpace direction="column" align="start" spacing="md">
        <VSpace align="center" spacing="sm">
          <VButton size="lg" type="primary">新建页面</VButton>
          <VButton size="md" type="primary">新建页面</VButton>
          <VButton size="sm" type="primary">新建页面</VButton>
          <VButton size="xs" type="primary">新建页面</VButton>
        </VSpace>
        <VSpace align="center" spacing="sm">
          <VButton size="lg" type="primary">
            <template #icon><IconAddCircle /></template>
            新建页面
          </VButton>
          <VButton size="md" type="primary">
            <template #icon><IconAddCircle /></template>
            新建页面
          </VButton>
          <VButton size="sm" type="primary">
            <template #icon><IconAddCircle /></template>
            新建页面
          </VButton>
          <VButton size="xs" type="primary">
            <template #icon><IconAddCircle /></template>
            新建页面
          </VButton>
        </VSpace>
      </VSpace>
    `,
  }),
};

export const IconOnly: Story = {
  render: () => ({
    components: { IconAddCircle, IconRefreshLine, VButton, VSpace },
    template: `
      <VSpace direction="column" align="start" spacing="md">
        <VSpace align="center" spacing="sm">
          <VButton label="新建" size="lg" type="primary">
            <template #icon><IconAddCircle /></template>
          </VButton>
          <VButton label="新建" size="md" type="primary">
            <template #icon><IconAddCircle /></template>
          </VButton>
          <VButton label="新建" size="sm" type="primary">
            <template #icon><IconAddCircle /></template>
          </VButton>
          <VButton label="新建" size="xs" type="primary">
            <template #icon><IconAddCircle /></template>
          </VButton>
        </VSpace>
        <VSpace align="center" spacing="sm">
          <VButton label="新建" shape="circle" size="lg" type="primary">
            <template #icon><IconAddCircle /></template>
          </VButton>
          <VButton label="新建" shape="circle" size="md" type="primary">
            <template #icon><IconAddCircle /></template>
          </VButton>
          <VButton label="新建" shape="circle" size="sm" type="primary">
            <template #icon><IconAddCircle /></template>
          </VButton>
          <VButton label="新建" shape="circle" size="xs" type="primary">
            <template #icon><IconAddCircle /></template>
          </VButton>
        </VSpace>
        <VSpace align="center" spacing="sm">
          <VButton label="刷新" variant="ghost" size="md">
            <template #icon><IconRefreshLine /></template>
          </VButton>
          <VButton label="刷新" variant="ghost" size="sm">
            <template #icon><IconRefreshLine /></template>
          </VButton>
          <VButton label="刷新" variant="ghost" shape="circle" size="sm" type="danger">
            <template #icon><IconRefreshLine /></template>
          </VButton>
        </VSpace>
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
        <VButton variant="ghost" type="primary">
          <template #icon>
            <IconRefreshLine />
          </template>
          刷新缓存
        </VButton>
      </VSpace>
    `,
  }),
};
