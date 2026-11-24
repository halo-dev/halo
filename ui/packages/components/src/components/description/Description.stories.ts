import type { Meta, StoryObj } from "@storybook/vue3-vite";
import { VDescription, VDescriptionItem } from ".";
import { VButton } from "../button";
import { VSpace } from "../space";
import { VTag } from "../tag";

const meta: Meta<typeof VDescription> = {
  title: "Components/Description",
  component: VDescription,
  tags: ["autodocs"],
  render: () => ({
    components: { VButton, VDescription, VDescriptionItem, VSpace, VTag },
    template: `
      <div class="max-w-3xl overflow-hidden rounded border border-gray-100 bg-white">
        <VDescription>
          <VDescriptionItem label="站点名称" content="Halo 官方博客" />
          <VDescriptionItem label="访问地址">
            <a class="text-primary hover:underline" href="https://www.halo.run">https://www.halo.run</a>
          </VDescriptionItem>
          <VDescriptionItem label="运行状态" vertical-center>
            <VSpace spacing="sm">
              <VTag theme="primary">运行中</VTag>
              <span class="text-gray-500">最近检查于 2 分钟前</span>
            </VSpace>
          </VDescriptionItem>
          <VDescriptionItem label="维护操作" vertical-center>
            <VSpace spacing="sm">
              <VButton size="sm">刷新缓存</VButton>
              <VButton size="sm" type="secondary">查看日志</VButton>
            </VSpace>
          </VDescriptionItem>
        </VDescription>
      </div>
    `,
  }),
};

export default meta;
type Story = StoryObj<typeof VDescription>;

export const SiteProfile: Story = {};

export const PluginRelease: Story = {
  render: () => ({
    components: { VDescription, VDescriptionItem, VTag },
    template: `
      <div class="max-w-3xl overflow-hidden rounded border border-gray-100 bg-white">
        <VDescription>
          <VDescriptionItem label="插件" content="Sitemap 生成器" />
          <VDescriptionItem label="版本" content="1.4.2" />
          <VDescriptionItem label="兼容范围">
            <VTag theme="secondary">Halo 2.20+</VTag>
          </VDescriptionItem>
          <VDescriptionItem
            label="发布说明"
            content="优化大站点的索引拆分逻辑，并修复定时生成任务的异常重试。"
          />
        </VDescription>
      </div>
    `,
  }),
};

export const Columns: Story = {
  render: () => ({
    components: { VDescription, VDescriptionItem },
    template: `
      <div class="max-w-3xl overflow-hidden rounded border border-gray-100 bg-white">
        <VDescription :columns="2" empty-text="—">
          <VDescriptionItem label="站点名称" content="Halo 官方博客" />
          <VDescriptionItem label="版本" content="2.28.0" />
          <VDescriptionItem label="访问地址" content="https://www.halo.run" />
          <VDescriptionItem label="创建时间" content="2024-01-01 12:00" />
          <VDescriptionItem label="发布说明" :span="2">
            优化大站点的索引拆分逻辑，并修复定时生成任务的异常重试。
          </VDescriptionItem>
        </VDescription>
      </div>
    `,
  }),
};

export const Bordered: Story = {
  render: () => ({
    components: { VDescription, VDescriptionItem },
    template: `
      <div class="max-w-3xl overflow-hidden rounded border border-gray-100 bg-white">
        <VDescription bordered title="站点信息">
          <VDescriptionItem label="站点名称" content="Halo 官方博客" />
          <VDescriptionItem label="访问地址" content="https://www.halo.run" />
          <VDescriptionItem label="运行状态" content="运行中" />
        </VDescription>
      </div>
    `,
  }),
};

export const Vertical: Story = {
  render: () => ({
    components: { VDescription, VDescriptionItem },
    template: `
      <div class="max-w-3xl overflow-hidden rounded border border-gray-100 bg-white">
        <VDescription layout="vertical" :columns="3" bordered>
          <VDescriptionItem label="站点名称" content="Halo 官方博客" />
          <VDescriptionItem label="版本" content="2.28.0" />
          <VDescriptionItem label="运行状态" content="运行中" />
        </VDescription>
      </div>
    `,
  }),
};

export const Inline: Story = {
  render: () => ({
    components: { VDescription, VDescriptionItem },
    template: `
      <div class="max-w-3xl overflow-hidden rounded border border-gray-100 bg-white">
        <VDescription layout="inline" :columns="3" separator="：">
          <VDescriptionItem label="站点名称" content="Halo 官方博客" />
          <VDescriptionItem label="版本" content="2.28.0" />
          <VDescriptionItem label="运行状态" content="运行中" />
        </VDescription>
      </div>
    `,
  }),
};

export const Sizes: Story = {
  render: () => ({
    components: { VDescription, VDescriptionItem },
    template: `
      <div class="flex max-w-3xl flex-col gap-4">
        <div
          v-for="size in ['xs', 'sm', 'md', 'lg']"
          :key="size"
          class="overflow-hidden rounded border border-gray-100 bg-white"
        >
          <VDescription :size="size">
            <VDescriptionItem label="尺寸" :content="size" />
            <VDescriptionItem label="访问地址" content="https://www.halo.run" />
          </VDescription>
        </div>
      </div>
    `,
  }),
};

export const LabelSlot: Story = {
  render: () => ({
    components: { VDescription, VDescriptionItem },
    template: `
      <div class="max-w-3xl overflow-hidden rounded border border-gray-100 bg-white">
        <VDescription label-width="120">
          <VDescriptionItem>
            <template #label>
              <span class="inline-flex items-center gap-1">
                <span class="h-1.5 w-1.5 rounded-full bg-primary"></span>
                运行状态
              </span>
            </template>
            运行中
          </VDescriptionItem>
          <VDescriptionItem label="站点名称" content="Halo 官方博客" />
        </VDescription>
      </div>
    `,
  }),
};
