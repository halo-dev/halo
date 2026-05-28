import type { Meta, StoryObj } from "@storybook/vue3-vite";
import { VAlert } from ".";
import { VButton } from "../button";
import { VSpace } from "../space";

const meta: Meta<typeof VAlert> = {
  title: "Components/Alert",
  component: VAlert,
  tags: ["autodocs"],
  render: (args) => ({
    components: { VAlert, VButton, VSpace },
    setup() {
      return { args };
    },
    template: `
      <div class="max-w-2xl">
        <VAlert
          :type="args.type"
          :title="args.title"
          :description="args.description"
          :closable="args.closable"
        >
          <template v-if="args.showActions" #actions>
            <VSpace spacing="sm">
              <VButton size="sm" type="primary">查看详情</VButton>
              <VButton size="sm">稍后处理</VButton>
            </VSpace>
          </template>
        </VAlert>
      </div>
    `,
  }),
  argTypes: {
    type: {
      control: { type: "select" },
      options: ["default", "success", "info", "warning", "error"],
    },
    closable: {
      control: { type: "boolean" },
    },
    showActions: {
      control: { type: "boolean" },
    },
  },
  args: {
    type: "info",
    title: "内容已保存为草稿",
    description: "发布前仍可继续编辑，草稿会保留最近一次保存的内容。",
    closable: true,
    showActions: false,
  },
};

export default meta;
type Story = StoryObj<typeof VAlert>;

export const Default: Story = {};

export const Success: Story = {
  args: {
    type: "success",
    title: "站点配置已更新",
    description: "新的 SEO 和缓存设置已经生效。",
  },
};

export const Warning: Story = {
  args: {
    type: "warning",
    title: "插件需要重新加载",
    description: "检测到插件配置发生变化，重新加载后才能应用到前台页面。",
    showActions: true,
  },
};

export const Error: Story = {
  args: {
    type: "error",
    title: "同步失败",
    description: "远程内容源暂时不可用，请检查网络连接后重试。",
    showActions: true,
  },
};
