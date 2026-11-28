import type { Meta, StoryObj } from "@storybook/vue3-vite";
import { markRaw, shallowRef } from "vue";
import { VTabItem, VTabs } from ".";
import { IconComputer, IconPhone, IconTablet } from "../../icons/icons";
import { VAlert } from "../alert";

const meta: Meta<typeof VTabs> = {
  title: "Components/Tabs",
  component: VTabs,
  tags: ["autodocs"],
  render: (args) => ({
    components: { VAlert, VTabItem, VTabs },
    setup() {
      const activeId = shallowRef("general");
      return { activeId, args };
    },
    template: `
      <div class="max-w-3xl">
        <VTabs
          v-model:activeId="activeId"
          :type="args.type"
          :direction="args.direction"
          :background="args.background"
          :centered="args.centered"
          :divider="args.divider"
          :size="args.size"
          :stretch="args.stretch"
        >
          <VTabItem id="general" label="基础设置">
            <VAlert
              title="站点名称与地址"
              description="用于控制台、前台页面和系统邮件中的站点展示信息。"
              type="info"
              :closable="false"
            />
          </VTabItem>
          <VTabItem id="seo" label="SEO">
            <VAlert
              title="搜索引擎信息"
              description="设置站点描述、关键词和默认 Open Graph 信息。"
              type="success"
              :closable="false"
            />
          </VTabItem>
          <VTabItem id="comment" label="评论">
            <VAlert
              title="评论策略"
              description="配置审核、通知和匿名评论等行为。"
              type="warning"
              :closable="false"
            />
          </VTabItem>
        </VTabs>
      </div>
    `,
  }),
  argTypes: {
    direction: {
      control: { type: "select" },
      options: ["row", "column"],
    },
    type: {
      control: { type: "select" },
      options: ["default", "pills", "outline"],
    },
    size: {
      control: { type: "select" },
      options: ["sm", "md"],
    },
    stretch: {
      control: "boolean",
    },
    centered: {
      control: "boolean",
    },
    divider: {
      control: "boolean",
    },
    background: {
      control: "boolean",
    },
  },
  args: {
    direction: "row",
    type: "default",
    size: "md",
    stretch: false,
    centered: false,
    divider: true,
    background: true,
  },
};

export default meta;
type Story = StoryObj<typeof VTabs>;

export const Default: Story = {};

export const Pills: Story = {
  args: {
    type: "pills",
  },
};

export const Outline: Story = {
  args: {
    type: "outline",
  },
};

export const Vertical: Story = {
  args: {
    direction: "column",
    type: "pills",
  },
};

export const WithIcons: Story = {
  render: () => ({
    components: { VAlert, VTabItem, VTabs },
    setup() {
      const activeId = shallowRef("desktop");
      const icons = {
        desktop: markRaw(IconComputer),
        tablet: markRaw(IconTablet),
        phone: markRaw(IconPhone),
      };

      return { activeId, icons };
    },
    template: `
      <div class="max-w-3xl">
        <VTabs v-model:activeId="activeId" type="outline">
          <VTabItem id="desktop" label="桌面端" :icon="icons.desktop">
            <VAlert title="桌面端预览" :closable="false" />
          </VTabItem>
          <VTabItem id="tablet" label="平板" :icon="icons.tablet">
            <VAlert title="平板预览" :closable="false" />
          </VTabItem>
          <VTabItem id="phone" label="手机" :icon="icons.phone">
            <VAlert title="手机预览" :closable="false" />
          </VTabItem>
        </VTabs>
      </div>
    `,
  }),
};

export const Compact: Story = {
  args: {
    size: "sm",
  },
};

export const Stretch: Story = {
  args: {
    size: "sm",
    stretch: true,
  },
};

export const Centered: Story = {
  args: {
    centered: true,
  },
};

export const NoDivider: Story = {
  args: {
    divider: false,
  },
};

export const OutlineNoBackground: Story = {
  args: {
    type: "outline",
    background: false,
  },
};

export const WithExtraContent: Story = {
  render: () => ({
    components: { VAlert, VTabItem, VTabs },
    setup() {
      const activeId = shallowRef("general");

      return { activeId };
    },
    template: `
      <div class="max-w-3xl">
        <VTabs v-model:activeId="activeId" centered>
          <template #left>
            <button
              class="rounded-base border border-gray-200 px-3 py-1.5 text-sm text-gray-700"
              type="button"
            >
              Left Extra Action
            </button>
          </template>
          <template #right>
            <button
              class="rounded-base border border-gray-200 px-3 py-1.5 text-sm text-gray-700"
              type="button"
            >
              Right Extra Action
            </button>
          </template>
          <VTabItem id="general" label="基础设置">
            <VAlert title="基础设置" :closable="false" />
          </VTabItem>
          <VTabItem id="seo" label="SEO">
            <VAlert title="SEO 设置" type="success" :closable="false" />
          </VTabItem>
          <VTabItem id="comment" label="评论">
            <VAlert title="评论设置" type="warning" :closable="false" />
          </VTabItem>
        </VTabs>
      </div>
    `,
  }),
};

export const DisabledAndPersistent: Story = {
  render: () => ({
    components: { VAlert, VTabItem, VTabs },
    setup() {
      const activeId = shallowRef("general");

      return { activeId };
    },
    template: `
      <div class="max-w-3xl">
        <VTabs v-model:activeId="activeId">
          <VTabItem id="general" label="基础设置" badge="新" persistent>
            <VAlert title="切换 tab 后本面板保持挂载" :closable="false" />
          </VTabItem>
          <VTabItem id="advanced" label="高级设置" disabled>
            <VAlert title="不可访问" type="warning" :closable="false" />
          </VTabItem>
          <VTabItem id="seo" label="SEO">
            <VAlert title="SEO 设置" type="success" :closable="false" />
          </VTabItem>
        </VTabs>
      </div>
    `,
  }),
};
