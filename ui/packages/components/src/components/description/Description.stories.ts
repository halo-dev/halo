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
