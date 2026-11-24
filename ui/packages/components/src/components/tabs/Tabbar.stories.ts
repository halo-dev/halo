import type { Meta, StoryObj } from "@storybook/vue3-vite";
import { markRaw, shallowRef } from "vue";
import { VTabbar } from ".";
import { IconComputer, IconPhone, IconTablet } from "../../icons/icons";

const meta: Meta<typeof VTabbar> = {
  title: "Components/Tabbar",
  component: VTabbar,
  tags: ["autodocs"],
  render: (args) => ({
    components: { VTabbar },
    setup() {
      const activeId = shallowRef("general");
      const items = [
        { label: "基础设置", id: "general" },
        { label: "文章设置", id: "post" },
        { label: "SEO 设置", id: "seo" },
        { label: "评论设置", id: "comment" },
        { label: "主题路由", id: "theme-route" },
        { label: "代码注入", id: "code-inject" },
      ];

      return { activeId, args, items };
    },
    template: `
      <div class="max-w-2xl">
        <VTabbar
          v-model:activeId="activeId"
          :direction="args.direction"
          :flush="args.flush"
          :items="items"
          :type="args.type"
        />
      </div>
    `,
  }),
  argTypes: {
    direction: {
      control: { type: "select" },
      options: ["row", "column"],
    },
    flush: {
      control: "boolean",
    },
    type: {
      control: { type: "select" },
      options: ["default", "pills", "outline"],
    },
  },
  args: {
    direction: "row",
    flush: false,
    type: "default",
  },
};

export default meta;
type Story = StoryObj<typeof VTabbar>;

export const Default: Story = {};

const overflowDecorator = () => ({
  template: '<div class="w-72"><story /></div>',
});

export const Overflow: Story = {
  decorators: [overflowDecorator],
};

export const OverflowPills: Story = {
  args: {
    type: "pills",
  },
  decorators: [overflowDecorator],
};

export const OverflowOutline: Story = {
  args: {
    type: "outline",
  },
  decorators: [overflowDecorator],
};

export const FlushCardHeader: Story = {
  render: () => ({
    components: { VTabbar },
    setup() {
      const activeId = shallowRef("general");
      const items = [
        { id: "general", label: "基础设置" },
        { id: "post", label: "文章设置" },
        { id: "seo", label: "SEO 设置" },
        { id: "comment", label: "评论设置" },
        { id: "theme-route", label: "主题路由" },
        { id: "code-inject", label: "代码注入" },
      ];

      return { activeId, items };
    },
    template: `
      <div class="max-w-sm rounded-base bg-white shadow-sm ring-1 ring-[#eaecf0]">
        <div class="flex border-b border-[#eaecf0]">
          <VTabbar
            v-model:activeId="activeId"
            :items="items"
            class="w-full"
            flush
            type="outline"
          />
        </div>
        <div class="p-4 text-sm text-gray-500">卡片内容</div>
      </div>
    `,
  }),
};

export const Pills: Story = {
  args: {
    type: "pills",
  },
};

export const Column: Story = {
  args: {
    direction: "column",
    type: "outline",
  },
};

export const WithIcons: Story = {
  render: (args) => ({
    components: { VTabbar },
    setup() {
      const activeId = shallowRef("desktop");
      const items = [
        { id: "desktop", icon: markRaw(IconComputer), ariaLabel: "桌面端" },
        { id: "tablet", icon: markRaw(IconTablet), ariaLabel: "平板" },
        { id: "phone", icon: markRaw(IconPhone), ariaLabel: "手机" },
      ];

      return { activeId, args, items };
    },
    template: `
      <div class="max-w-md">
        <VTabbar
          v-model:activeId="activeId"
          :items="items"
          :type="args.type"
        />
      </div>
    `,
  }),
  args: {
    type: "outline",
  },
};

export const Disabled: Story = {
  render: () => ({
    components: { VTabbar },
    setup() {
      const activeId = shallowRef("general");
      const items = [
        { id: "general", label: "基础设置" },
        { id: "advanced", label: "高级设置", disabled: true },
        { id: "seo", label: "SEO 设置" },
      ];

      return { activeId, items };
    },
    template: `
      <div class="max-w-2xl">
        <VTabbar v-model:activeId="activeId" :items="items" />
      </div>
    `,
  }),
};

export const WithBadge: Story = {
  render: () => ({
    components: { VTabbar },
    setup() {
      const activeId = shallowRef("published");
      const items = [
        { id: "published", label: "已发布", badge: 12 },
        { id: "draft", label: "草稿", badge: 3 },
        { id: "archived", label: "已归档", badge: 0 },
      ];

      return { activeId, items };
    },
    template: `
      <div class="max-w-2xl">
        <VTabbar v-model:activeId="activeId" :items="items" type="pills" />
      </div>
    `,
  }),
};
