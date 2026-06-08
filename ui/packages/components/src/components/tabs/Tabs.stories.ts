import type { Meta, StoryObj } from "@storybook/vue3-vite";
import { shallowRef } from "vue";
import { VTabItem, VTabs } from ".";
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
  },
  args: {
    direction: "row",
    type: "default",
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
