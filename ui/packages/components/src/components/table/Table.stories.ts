import type { Meta, StoryObj } from "@storybook/vue3-vite";
import { computed, ref } from "vue";
import {
  VTable,
  createTableColumnHelper,
  type RowSelectionState,
  type SortingState,
} from ".";
import { IconMore } from "../../icons/icons";
import { VButton } from "../button";
import { VDropdown, VDropdownItem } from "../dropdown";
import { VTag, type TagTheme } from "../tag";

interface Post {
  id: string;
  title: string;
  author: string;
  status: "published" | "draft" | "archived";
  createdAt: string;
  views: number;
  summary: string;
}

const posts: Post[] = [
  {
    id: "theme-intro",
    title: "Halo 主题开发入门",
    author: "Ryan",
    status: "published",
    createdAt: "2026-11-01",
    views: 1280,
    summary: "从目录结构、模板到 Finder API，梳理主题开发的完整流程。",
  },
  {
    id: "plugin-migration",
    title: "插件迁移注意事项",
    author: "Lilian",
    status: "draft",
    createdAt: "2026-10-28",
    views: 642,
    summary: "升级到新版本前需要检查的扩展点、依赖和配置项。",
  },
  {
    id: "release-notes",
    title: "2.28 版本发布说明",
    author: "Ryan",
    status: "published",
    createdAt: "2026-10-24",
    views: 2310,
    summary: "本版本聚焦编辑器体验、插件兼容性和性能改进。",
  },
  {
    id: "content-api",
    title: "内容 API 使用指南",
    author: "John",
    status: "published",
    createdAt: "2026-10-21",
    views: 987,
    summary: "查询、分页和权限过滤的常见写法。",
  },
  {
    id: "comment-widget",
    title: "评论组件接入说明",
    author: "Lilian",
    status: "archived",
    createdAt: "2026-10-18",
    views: 415,
    summary: "旧版评论组件的接入方式已归档，请使用新版本。",
  },
  {
    id: "search-config",
    title: "搜索功能配置",
    author: "John",
    status: "draft",
    createdAt: "2026-10-12",
    views: 203,
    summary: "索引、分词和搜索服务商配置的说明。",
  },
  {
    id: "security-policy",
    title: "安全策略与权限模型",
    author: "Ryan",
    status: "published",
    createdAt: "2026-10-05",
    views: 1530,
    summary: "角色、权限和资源访问控制的设计说明。",
  },
  {
    id: "backup-restore",
    title: "备份与恢复实践",
    author: "Lilian",
    status: "published",
    createdAt: "2026-09-30",
    views: 764,
    summary: "数据库与附件的备份、校验和恢复步骤。",
  },
];

const column = createTableColumnHelper<Post>();

const columns = column.columns([
  column.accessor("title", {
    header: "标题",
    enableSorting: true,
    sortFn: "text",
    meta: { minWidth: 240 },
  }),
  column.accessor("author", {
    header: "作者",
    meta: { width: 140 },
  }),
  column.accessor("status", {
    header: "状态",
    cell: (info) => statusText(info.getValue() as Post["status"]),
    meta: { width: 120 },
  }),
  column.accessor("createdAt", {
    header: "创建时间",
    enableSorting: true,
    sortFn: "text",
    meta: { width: 140 },
  }),
  column.accessor("views", {
    header: "浏览量",
    enableSorting: true,
    sortFn: "basic",
    meta: { align: "right", width: 120 },
  }),
]);

const groupedColumns = [
  column.group({
    id: "content",
    header: "内容",
    columns: column.columns([
      column.accessor("title", {
        header: "标题",
        meta: { minWidth: 240 },
      }),
      column.accessor("author", {
        header: "作者",
        meta: { width: 140 },
      }),
    ]),
  }),
  column.group({
    id: "metrics",
    header: "数据",
    columns: column.columns([
      column.accessor("views", {
        header: "浏览量",
        meta: { align: "right", width: 120 },
      }),
      column.accessor("status", {
        header: "状态",
        cell: (info) => statusText(info.getValue() as Post["status"]),
        meta: { width: 120 },
      }),
    ]),
  }),
  column.accessor("createdAt", {
    header: "创建时间",
    meta: { width: 140 },
  }),
];

function statusTheme(status: Post["status"]): TagTheme {
  if (status === "published") {
    return "primary";
  }

  if (status === "draft") {
    return "secondary";
  }

  return "default";
}

function statusText(status: Post["status"]) {
  if (status === "published") {
    return "已发布";
  }

  if (status === "draft") {
    return "草稿";
  }

  return "已归档";
}

const meta: Meta<typeof VTable> = {
  title: "Components/Table",
  component: VTable,
  tags: ["autodocs"],
};

export default meta;
type Story = StoryObj<typeof VTable>;

export const Basic: Story = {
  render: () => ({
    components: { VTable },
    setup() {
      return { columns, posts };
    },
    template: `
      <div class="max-w-5xl rounded border border-gray-100 bg-white">
        <VTable :columns="columns" :data="posts" row-key="id" />
      </div>
    `,
  }),
};

export const Sortable: Story = {
  render: () => ({
    components: { VTable },
    setup() {
      const sorting = ref<SortingState>([]);
      return { columns, posts, sorting };
    },
    template: `
      <div class="max-w-5xl rounded border border-gray-100 bg-white">
        <VTable
          v-model:sorting="sorting"
          :columns="columns"
          :data="posts"
          row-key="id"
        />
      </div>
    `,
  }),
};

export const Selectable: Story = {
  render: () => ({
    components: { VButton, VTable },
    setup() {
      const rowSelection = ref<RowSelectionState>({});
      const selectedCount = computed(
        () => Object.keys(rowSelection.value).length
      );
      return { columns, posts, rowSelection, selectedCount };
    },
    template: `
      <div class="max-w-5xl overflow-hidden rounded border border-gray-100 bg-white">
        <div class="flex items-center justify-between border-b border-gray-100 px-4 py-3">
          <span class="text-sm text-gray-500">已选择 {{ selectedCount }} 项</span>
          <VButton :disabled="selectedCount === 0" size="sm">批量删除</VButton>
        </div>
        <VTable
          v-model:row-selection="rowSelection"
          :columns="columns"
          :data="posts"
          row-key="id"
          selectable
        />
      </div>
    `,
  }),
};

export const Expandable: Story = {
  render: () => ({
    components: { VTable },
    setup() {
      return { columns, posts };
    },
    template: `
      <div class="max-w-5xl rounded border border-gray-100 bg-white">
        <VTable
          :columns="columns"
          :data="posts"
          expandable
          row-key="id"
        >
          <template #expanded="{ row }">
            <div class="grid gap-1">
              <div class="text-xs text-gray-400">摘要</div>
              <p class="text-sm text-gray-600">{{ row.original.summary }}</p>
            </div>
          </template>
        </VTable>
      </div>
    `,
  }),
};

export const Loading: Story = {
  render: () => ({
    components: { VTable },
    setup() {
      return { columns };
    },
    template: `
      <div class="max-w-5xl rounded border border-gray-100 bg-white">
        <VTable :columns="columns" :data="[]" loading />
      </div>
    `,
  }),
};

export const Empty: Story = {
  render: () => ({
    components: { VTable },
    setup() {
      return { columns };
    },
    template: `
      <div class="max-w-5xl rounded border border-gray-100 bg-white">
        <VTable :columns="columns" :data="[]" />
      </div>
    `,
  }),
};

export const StickyHeader: Story = {
  render: () => ({
    components: { VTable },
    setup() {
      return { columns, posts };
    },
    template: `
      <div class="max-w-5xl rounded border border-gray-100 bg-white">
        <VTable
          :columns="columns"
          :data="posts"
          :ui="{ root: 'max-h-64' }"
          row-key="id"
          sticky
        />
      </div>
    `,
  }),
};

export const StickyMultiLevelHeader: Story = {
  render: () => ({
    components: { VTable },
    setup() {
      return { groupedColumns, posts };
    },
    template: `
      <div class="max-w-5xl rounded border border-gray-100 bg-white">
        <VTable
          :columns="groupedColumns"
          :data="posts"
          :ui="{ root: 'max-h-64' }"
          row-key="id"
          sticky
        />
      </div>
    `,
  }),
};

const customCellColumns = column.columns([
  ...columns.slice(0, 3),
  column.display({
    id: "actions",
    header: "操作",
    enableHiding: false,
    enableSorting: false,
    meta: { align: "right", width: 80 },
  }),
]);

export const CustomCells: Story = {
  render: () => ({
    components: { IconMore, VDropdown, VDropdownItem, VTable, VTag },
    setup() {
      return {
        customCellColumns,
        posts,
        statusText,
        statusTheme,
      };
    },
    template: `
      <div class="max-w-5xl rounded border border-gray-100 bg-white">
        <VTable :columns="customCellColumns" :data="posts" row-key="id">
          <template #cell-title="{ original }">
            <a class="font-medium text-gray-900 hover:text-primary" href="#" @click.prevent>
              {{ original.title }}
            </a>
          </template>
          <template #cell-status="{ value }">
            <VTag :theme="statusTheme(value)">{{ statusText(value) }}</VTag>
          </template>
          <template #cell-actions>
            <VDropdown :dispose-timeout="null">
              <button
                aria-label="更多操作"
                class="inline-flex cursor-pointer rounded p-1 text-gray-500 transition-colors hover:bg-gray-100 hover:text-gray-900"
                type="button"
              >
                <IconMore />
              </button>
              <template #popper>
                <VDropdownItem>编辑</VDropdownItem>
                <VDropdownItem>预览</VDropdownItem>
                <VDropdownItem type="danger">移入回收站</VDropdownItem>
              </template>
            </VDropdown>
          </template>
        </VTable>
      </div>
    `,
  }),
};

export const MultiLevelHeader: Story = {
  render: () => ({
    components: { VTable },
    setup() {
      return { groupedColumns, posts };
    },
    template: `
      <div class="max-w-5xl rounded border border-gray-100 bg-white">
        <VTable :columns="groupedColumns" :data="posts" row-key="id" />
      </div>
    `,
  }),
};
