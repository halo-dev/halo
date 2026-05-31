import type { Meta, StoryObj } from "@storybook/vue3-vite";
import { shallowRef } from "vue";
import {
  IconBookRead,
  IconDashboard,
  IconFolder,
  IconMessage,
  IconPages,
  IconSettings,
} from "@/icons/icons";
import { VMenu, VMenuItem, VMenuLabel } from "./index";

const meta: Meta<typeof VMenu> = {
  title: "Components/Menu",
  component: VMenu,
  tags: ["autodocs"],
  render: (args) => ({
    components: {
      IconBookRead,
      IconDashboard,
      IconFolder,
      IconMessage,
      IconPages,
      IconSettings,
      VMenu,
      VMenuItem,
      VMenuLabel,
    },
    setup() {
      const activeId = shallowRef("posts");
      return { activeId, args };
    },
    template: `
      <div class="w-72 rounded border border-gray-100 bg-white">
        <VMenu :open-ids="args.openIds">
          <VMenuItem id="dashboard" title="仪表盘" :active="activeId === 'dashboard'" @select="activeId = $event">
            <template #icon>
              <IconDashboard />
            </template>
          </VMenuItem>

          <VMenuLabel>内容管理</VMenuLabel>

          <VMenuItem id="content" title="内容" @select="activeId = $event">
            <template #icon>
              <IconBookRead />
            </template>
            <VMenuItem id="posts" title="文章" :active="activeId === 'posts'" @select="activeId = $event" />
            <VMenuItem id="pages" title="页面" :active="activeId === 'pages'" @select="activeId = $event" />
          </VMenuItem>

          <VMenuItem id="comments" title="评论" :active="activeId === 'comments'" @select="activeId = $event">
            <template #icon>
              <IconMessage />
            </template>
          </VMenuItem>
          <VMenuItem id="attachments" title="附件" :active="activeId === 'attachments'" @select="activeId = $event">
            <template #icon>
              <IconFolder />
            </template>
          </VMenuItem>

          <VMenuLabel>系统</VMenuLabel>

          <VMenuItem id="appearance" title="外观" :active="activeId === 'appearance'" @select="activeId = $event">
            <template #icon>
              <IconPages />
            </template>
          </VMenuItem>
          <VMenuItem id="settings" title="设置" :active="activeId === 'settings'" @select="activeId = $event">
            <template #icon>
              <IconSettings />
            </template>
          </VMenuItem>
        </VMenu>
      </div>
    `,
  }),
  args: {
    openIds: ["content"],
  },
};

export default meta;
type Story = StoryObj<typeof VMenu>;

export const ConsoleNavigation: Story = {};

export const ClosedGroups: Story = {
  args: {
    openIds: [],
  },
};
