import type { Meta, StoryObj } from "@storybook/vue3-vite";
import { VEntity, VEntityContainer, VEntityField } from ".";
import { VAvatar } from "../avatar";
import { VDropdownDivider, VDropdownItem } from "../dropdown";
import { VStatusDot } from "../status";
import { VTag } from "../tag";

const meta: Meta<typeof VEntity> = {
  title: "Components/Entity",
  component: VEntity,
  tags: ["autodocs"],
  render: (args) => ({
    components: {
      VAvatar,
      VDropdownDivider,
      VDropdownItem,
      VEntity,
      VEntityContainer,
      VEntityField,
      VStatusDot,
      VTag,
    },
    setup() {
      const posts = [
        {
          id: "intro",
          title: "Halo 主题开发入门",
          description: "docs / theme-development",
          status: "success",
          statusText: "已发布",
          tag: "置顶",
          selected: true,
        },
        {
          id: "release",
          title: "2.24 版本发布说明",
          description: "release / changelog",
          status: "warning",
          statusText: "待审核",
          tag: "草稿",
          selected: false,
        },
        {
          id: "migration",
          title: "插件迁移注意事项",
          description: "plugin / migration",
          status: "default",
          statusText: "已归档",
          tag: "文档",
          selected: false,
        },
      ];

      return { args, posts };
    },
    template: `
      <div class="max-w-4xl rounded border border-gray-100 bg-white">
        <VEntityContainer>
          <VEntity
            v-for="post in posts"
            :key="post.id"
            :is-selected="args.highlightSelected && post.selected"
          >
            <template #checkbox>
              <input class="h-4 w-4 rounded border-gray-300 text-primary" type="checkbox" :checked="post.selected" />
            </template>
            <template #start>
              <VAvatar :alt="post.title" circle size="sm" />
              <VEntityField :title="post.title" :description="post.description" :max-width="260">
                <template #title>
                  <div class="inline-flex min-w-0 items-center gap-2">
                    <span class="truncate text-sm font-medium text-gray-900">
                      {{ post.title }}
                    </span>
                    <VTag theme="secondary">{{ post.tag }}</VTag>
                  </div>
                </template>
              </VEntityField>
            </template>
            <template #end>
              <VStatusDot :state="post.status" :text="post.statusText" />
              <span class="hidden text-xs text-gray-400 md:inline">刚刚更新</span>
            </template>
            <template #dropdownItems>
              <VDropdownItem>编辑</VDropdownItem>
              <VDropdownItem>预览</VDropdownItem>
              <VDropdownDivider />
              <VDropdownItem type="danger">移入回收站</VDropdownItem>
            </template>
          </VEntity>
        </VEntityContainer>
      </div>
    `,
  }),
  argTypes: {
    highlightSelected: {
      control: { type: "boolean" },
    },
  },
  args: {
    highlightSelected: true,
  },
};

export default meta;
type Story = StoryObj<typeof VEntity>;

export const PostList: Story = {};

export const WithoutSelection: Story = {
  args: {
    highlightSelected: false,
  },
};
