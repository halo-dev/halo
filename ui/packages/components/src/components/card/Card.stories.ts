import type { Meta, StoryObj } from "@storybook/vue3-vite";
import { VCard } from ".";
import { VButton } from "../button";
import { VSpace } from "../space";
import { VStatusDot } from "../status";
import { VTag } from "../tag";

const meta: Meta<typeof VCard> = {
  title: "Components/Card",
  component: VCard,
  tags: ["autodocs"],
  render: (args) => ({
    components: { VButton, VCard, VSpace, VStatusDot, VTag },
    setup() {
      return { args };
    },
    template: `
      <div class="max-w-xl">
        <VCard :title="args.title">
          <template #actions>
            <VButton size="xs">查看日志</VButton>
          </template>

          <div class="space-y-4">
            <div class="flex items-center justify-between gap-4">
              <div>
                <div class="text-sm font-medium text-gray-900">www.halo.run</div>
                <div class="mt-1 text-xs text-gray-500">主站点 · 最近发布于 12 分钟前</div>
              </div>
              <VStatusDot state="success" text="运行中" />
            </div>

            <div class="grid grid-cols-3 gap-3">
              <div class="rounded bg-gray-50 p-3">
                <div class="text-xs text-gray-500">文章</div>
                <div class="mt-1 text-lg font-semibold text-gray-900">248</div>
              </div>
              <div class="rounded bg-gray-50 p-3">
                <div class="text-xs text-gray-500">评论</div>
                <div class="mt-1 text-lg font-semibold text-gray-900">1,482</div>
              </div>
              <div class="rounded bg-gray-50 p-3">
                <div class="text-xs text-gray-500">附件</div>
                <div class="mt-1 text-lg font-semibold text-gray-900">6.8 GB</div>
              </div>
            </div>
          </div>

          <template #footer>
            <VSpace spacing="sm">
              <VTag theme="primary">生产环境</VTag>
              <VTag>Halo 2.24</VTag>
            </VSpace>
          </template>
        </VCard>
      </div>
    `,
  }),
  args: {
    title: "站点概览",
  },
};

export default meta;
type Story = StoryObj<typeof VCard>;

export const Default: Story = {};

export const PlainContent: Story = {
  render: () => ({
    components: { VCard },
    template: `
      <div class="max-w-md">
        <VCard>
          <div class="space-y-2">
            <div class="text-sm font-medium text-gray-900">自动保存已开启</div>
            <p class="text-sm leading-6 text-gray-600">
              编辑器会在内容变化后自动保存草稿，避免刷新页面时丢失未发布内容。
            </p>
          </div>
        </VCard>
      </div>
    `,
  }),
};

export const MetricGrid: Story = {
  render: () => ({
    components: { VCard, VStatusDot },
    setup() {
      const metrics = [
        {
          label: "今日访问",
          value: "18,240",
          delta: "+12.4%",
          state: "success",
        },
        {
          label: "待审核评论",
          value: "36",
          delta: "需要处理",
          state: "warning",
        },
        { label: "同步失败", value: "2", delta: "最近 1 小时", state: "error" },
      ];

      return { metrics };
    },
    template: `
      <div class="grid max-w-4xl gap-4 md:grid-cols-3">
        <VCard v-for="metric in metrics" :key="metric.label">
          <div class="flex items-start justify-between gap-4">
            <div>
              <div class="text-xs text-gray-500">{{ metric.label }}</div>
              <div class="mt-2 text-2xl font-semibold text-gray-900">{{ metric.value }}</div>
            </div>
            <VStatusDot :state="metric.state" />
          </div>
          <div class="mt-3 text-xs text-gray-500">{{ metric.delta }}</div>
        </VCard>
      </div>
    `,
  }),
};
