import type { Meta, StoryObj } from "@storybook/vue3-vite";
import { IconAddCircle, IconDashboard, IconRefreshLine } from "@/icons/icons";
import { VPageHeader } from ".";
import { VButton } from "../button";

const meta: Meta<typeof VPageHeader> = {
  title: "Components/PageHeader",
  component: VPageHeader,
  tags: ["autodocs"],
  render: (args) => ({
    components: {
      IconAddCircle,
      IconDashboard,
      IconRefreshLine,
      VButton,
      VPageHeader,
    },
    setup() {
      return { args };
    },
    template: `
      <div class="max-w-4xl overflow-hidden rounded border border-gray-100 bg-white">
        <VPageHeader :title="args.title">
          <template v-if="args.showIcon" #icon>
            <IconDashboard class="text-primary" />
          </template>
          <template #actions>
            <VButton>
              <template #icon>
                <IconRefreshLine />
              </template>
              刷新
            </VButton>
            <VButton type="primary">
              <template #icon>
                <IconAddCircle />
              </template>
              新建文章
            </VButton>
          </template>
        </VPageHeader>
      </div>
    `,
  }),
  argTypes: {
    showIcon: {
      control: { type: "boolean" },
    },
  },
  args: {
    title: "文章管理",
    showIcon: true,
  },
};

export default meta;
type Story = StoryObj<typeof VPageHeader>;

export const Default: Story = {};

export const WithoutIcon: Story = {
  args: {
    showIcon: false,
  },
};
