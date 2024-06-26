import {
  IconAddCircle,
  IconBookRead,
  IconDashboard,
  IconFolder,
  IconMessage,
  IconPages,
} from "@/icons/icons";
import type { Meta, StoryObj } from "@storybook/vue3";
import { VMenu, VMenuItem, VMenuLabel } from "./index";

const meta: Meta<typeof VMenu> = {
  title: "Menu",
  component: VMenu,
  tags: ["autodocs"],
  render: (args) => ({
    components: {
      VMenu,
      VMenuItem,
      VMenuLabel,
      IconBookRead,
      IconDashboard,
      IconMessage,
      IconFolder,
      IconPages,
      IconAddCircle,
    },
    setup() {
      return {
        args,
      };
    },
    template: `
      <div class="w-1/3">
        <VMenu>
          <VMenuItem active title="仪表盘">
            <template #icon>
              <IconDashboard />
            </template>
          </VMenuItem>
          <VMenuLabel>内容</VMenuLabel>
          <VMenuItem title="文章">
            <template #icon>
              <IconBookRead />
            </template>
            <VMenuItem title="新文章">
              <template #icon>
                <IconBookRead />
              </template>
            </VMenuItem>
          </VMenuItem>
          <VMenuItem title="页面">
            <template #icon>
              <IconPages />
            </template>
          </VMenuItem>
          <VMenuItem title="评论">
            <template #icon>
              <IconMessage />
            </template>
          </VMenuItem>
          <VMenuItem title="附件">
            <template #icon>
              <IconFolder />
            </template>
          </VMenuItem>
        </VMenu>
      </div>
    `,
  }),
  argTypes: {},
};

export default meta;
type Story = StoryObj<typeof VMenu>;

export const Default: Story = {
  args: {},
};
