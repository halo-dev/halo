import type { Meta, StoryObj } from "@storybook/vue3";
import { VTabbar } from ".";
import { ref } from "vue";

const meta: Meta<typeof VTabbar> = {
  title: "Tabbar",
  component: VTabbar,
  tags: ["autodocs"],
  render: (args) => ({
    components: { VTabbar },
    setup() {
      const activeId = ref("general");
      const items = [
        { label: "基本设置", id: "general" },
        { label: "文章设置", id: "post" },
        { label: "SEO 设置", id: "seo" },
        { label: "评论设置", id: "comment" },
        { label: "主题路由设置", id: "theme-route" },
        { label: "代码注入", id: "code-inject" },
      ];

      return {
        args,
        activeId,
        items,
      };
    },
    template: `
      <div class="p-3">
        <VTabbar 
          v-model:activeId="activeId" 
          :items="items" 
          :type="args.type"
          :direction="args.direction"
        />
      </div>
 
    `,
  }),
  argTypes: {
    type: {
      control: { type: "select" },
      options: ["default", "pills", "outline"],
    },
    direction: {
      control: { type: "select" },
      options: ["row", "column"],
    },
  },
};

export default meta;
type Story = StoryObj<typeof VTabbar>;

export const Default: Story = {
  args: {
    type: "default",
  },
};

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
