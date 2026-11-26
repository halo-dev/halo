import type { Meta, StoryObj } from "@storybook/vue3-vite";
import { IconAddCircle, IconBookRead, IconRefreshLine } from "@/icons/icons";
import { VPageHeader } from ".";
import { VButton } from "../button";
import { VTag } from "../tag";

const meta: Meta<typeof VPageHeader> = {
  title: "Components/PageHeader",
  component: VPageHeader,
  tags: ["autodocs"],
  render: (args) => ({
    components: {
      IconAddCircle,
      IconBookRead,
      IconRefreshLine,
      VButton,
      VPageHeader,
    },
    setup() {
      return { args };
    },
    template: `
      <div class="max-w-4xl overflow-hidden rounded border border-gray-100 bg-white">
        <VPageHeader
          :bordered="args.bordered"
          :loading="args.loading"
          :sticky="args.sticky"
          :subtitle="args.subtitle"
          :title="args.title"
          :truncate="args.truncate"
        >
          <template #icon>
            <IconBookRead />
          </template>
          <template #actions>
            <VButton size="sm">
              <template #icon>
                <IconRefreshLine />
              </template>
              刷新
            </VButton>
            <VButton size="sm" type="primary">
              <template #icon>
                <IconAddCircle />
              </template>
              新建文章
            </VButton>
          </template>
        </VPageHeader>
        <div class="min-h-40 bg-gray-50 p-4">
          <div class="h-24 rounded-base border border-gray-100 bg-white"></div>
        </div>
      </div>
    `,
  }),
  args: {
    title: "文章管理",
    subtitle: "共 128 篇 · 最近更新于 2 分钟前",
    bordered: true,
    loading: false,
    sticky: false,
    truncate: true,
  },
};

export default meta;
type Story = StoryObj<typeof VPageHeader>;

export const Default: Story = {};

export const WithoutIcon: Story = {
  render: (args) => ({
    components: { VButton, VPageHeader },
    setup() {
      return { args };
    },
    template: `
      <div class="max-w-4xl overflow-hidden rounded border border-gray-100 bg-white">
        <VPageHeader :bordered="args.bordered" :title="args.title">
          <template #actions>
            <VButton size="sm" type="primary">新建文章</VButton>
          </template>
        </VPageHeader>
        <div class="min-h-40 bg-gray-50 p-4"></div>
      </div>
    `,
  }),
};

export const Detail: Story = {
  args: {
    title: "Markdown 编辑器",
    subtitle: "halo-dev · v2.1.0 · 3 天前更新",
  },
  render: (args) => ({
    components: { IconRefreshLine, VButton, VPageHeader, VTag },
    setup() {
      return { args };
    },
    template: `
      <div class="max-w-4xl overflow-hidden rounded border border-gray-100 bg-white">
        <VPageHeader
          :bordered="args.bordered"
          :subtitle="args.subtitle"
          :title="args.title"
        >
          <template #icon>
            <div class="grid h-8 w-8 place-items-center rounded-lg bg-primary text-sm font-bold text-white">
              M
            </div>
          </template>
          <template #tags>
            <VTag theme="primary">已启用</VTag>
          </template>
          <template #actions>
            <VButton size="sm">
              <template #icon>
                <IconRefreshLine />
              </template>
              刷新
            </VButton>
            <VButton size="sm">停用</VButton>
          </template>
        </VPageHeader>
        <div class="min-h-40 bg-gray-50 p-4">
          <div class="h-24 rounded-base border border-gray-100 bg-white"></div>
        </div>
      </div>
    `,
  }),
};

export const Loading: Story = {
  args: {
    title: "Markdown 编辑器",
    subtitle: "halo-dev · v2.1.0 · 3 天前更新",
    loading: true,
  },
};

export const LongTitle: Story = {
  args: {
    title: "Halo 官方 Markdown 编辑器（增强版） · 支持实时预览与自定义快捷键",
    subtitle: "halo-dev · v2.28.0 · 最近更新于 2026-11-25",
  },
};

export const WrappedTitle: Story = {
  args: {
    title: "Halo 官方 Markdown 编辑器（增强版） · 支持实时预览与自定义快捷键",
    subtitle: "halo-dev · v2.28.0 · 最近更新于 2026-11-25",
    truncate: false,
  },
};

export const Sticky: Story = {
  args: {
    sticky: true,
  },
  render: (args) => ({
    components: { IconAddCircle, IconBookRead, VButton, VPageHeader },
    setup() {
      return { args };
    },
    template: `
      <div class="max-w-4xl rounded border border-gray-100 bg-white">
        <VPageHeader
          :bordered="args.bordered"
          :sticky="args.sticky"
          :subtitle="args.subtitle"
          :title="args.title"
        >
          <template #icon>
            <IconBookRead />
          </template>
          <template #actions>
            <VButton size="sm" type="primary">
              <template #icon>
                <IconAddCircle />
              </template>
              新建文章
            </VButton>
          </template>
        </VPageHeader>
        <div class="space-y-3 bg-gray-50 p-4">
          <div
            v-for="index in 12"
            :key="index"
            class="h-10 rounded-base border border-gray-100 bg-white"
          ></div>
        </div>
      </div>
    `,
  }),
};
