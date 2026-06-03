import type { Meta, StoryObj } from "@storybook/vue3-vite";
import { IconAddCircle, IconRefreshLine } from "@/icons/icons";
import { VEmpty } from ".";
import { VButton } from "../button";
import { VSpace } from "../space";

const meta: Meta<typeof VEmpty> = {
  title: "Components/Empty",
  component: VEmpty,
  tags: ["autodocs"],
  render: (args) => ({
    components: { IconAddCircle, IconRefreshLine, VButton, VEmpty, VSpace },
    setup() {
      return { args };
    },
    template: `
      <VEmpty
        :title="args.title"
        :message="args.message"
        :image="args.image"
      >
        <template v-if="args.showActions" #actions>
          <VSpace spacing="sm">
            <VButton>
              <template #icon>
                <IconRefreshLine />
              </template>
              清空筛选
            </VButton>
            <VButton type="primary">
              <template #icon>
                <IconAddCircle />
              </template>
              新建文章
            </VButton>
          </VSpace>
        </template>
      </VEmpty>
    `,
  }),
  argTypes: {
    showActions: {
      control: { type: "boolean" },
    },
  },
  args: {
    title: "没有找到匹配的文章",
    message: "调整关键词、分类或状态筛选后再试，或者直接创建一篇新文章。",
    showActions: true,
  },
};

export default meta;
type Story = StoryObj<typeof VEmpty>;

export const SearchResult: Story = {};

export const FirstRun: Story = {
  args: {
    title: "还没有创建任何页面",
    message: "页面适合用于关于我们、友情链接、隐私政策等固定内容。",
    showActions: true,
  },
};

export const WithoutActions: Story = {
  args: {
    title: "暂无评论",
    message: "当读者提交评论后，会显示在这里等待你审核。",
    showActions: false,
  },
};
