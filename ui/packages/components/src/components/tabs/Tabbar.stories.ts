import type { Meta, StoryObj } from "@storybook/vue3-vite";
import { shallowRef } from "vue";
import { VTabbar } from ".";

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
type Story = StoryObj<typeof VTabbar>;

export const Default: Story = {};

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
