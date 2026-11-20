<script lang="ts" setup>
import {
  IconGrid,
  IconMore,
  IconRefreshLine,
  Toast,
  VButton,
  VCard,
  VDropdown,
  VDropdownItem,
  VPageHeader,
  VTable,
  VTag,
  createTableColumnHelper,
  type ColumnPinningState,
  type ColumnVisibilityState,
  type RowSelectionState,
  type SortingState,
  type TagTheme,
} from "@halo-dev/components";
import { computed, ref } from "vue";

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

const basicColumns = column.columns([
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

const stateColumns = column.columns([
  column.accessor("title", {
    header: "标题",
    meta: { minWidth: 200 },
  }),
  column.accessor("author", {
    header: "作者",
    meta: { width: 120 },
  }),
  column.accessor("status", {
    header: "状态",
    cell: (info) => statusText(info.getValue() as Post["status"]),
    meta: { width: 100 },
  }),
]);

const featureColumns = column.columns([
  column.accessor("title", {
    header: "标题",
    enableSorting: true,
    sortFn: "text",
    meta: { width: 300, minWidth: 180 },
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

const customColumns = column.columns([
  ...basicColumns,
  column.display({
    id: "actions",
    header: "操作",
    enableHiding: false,
    enableSorting: false,
    meta: { align: "right", width: 80 },
  }),
]);

const visibilityOptions = [
  { id: "title", label: "标题" },
  { id: "author", label: "作者" },
  { id: "status", label: "状态" },
  { id: "createdAt", label: "创建时间" },
  { id: "views", label: "浏览量" },
];

const sorting = ref<SortingState>([]);
const rowSelection = ref<RowSelectionState>({});
const columnVisibility = ref<ColumnVisibilityState>({});
const columnPinning = ref<ColumnPinningState>({
  start: ["title"],
  end: ["views"],
});

const manyPosts: Post[] = Array.from({ length: 1000 }, (_, index) => {
  const post = posts[index % posts.length];
  return {
    ...post,
    id: `post-${index + 1}`,
    title: `${post.title} #${index + 1}`,
  };
});

const selectedCount = computed(() => Object.keys(rowSelection.value).length);

const sortingText = computed(() => {
  const current = sorting.value[0];

  if (!current) {
    return "未排序";
  }

  const labels: Record<string, string> = {
    title: "标题",
    createdAt: "创建时间",
    views: "浏览量",
  };

  return `${labels[current.id] ?? current.id}${current.desc ? "降序" : "升序"}`;
});

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

function onRowClick(post: Post) {
  Toast.info(`点击了「${post.title}」`);
}

function toggleColumn(id: string) {
  columnVisibility.value = {
    ...columnVisibility.value,
    [id]: columnVisibility.value[id] === false,
  };
}

function resetColumns() {
  columnVisibility.value = {};
}
</script>

<template>
  <VPageHeader title="表格组件预览">
    <template #icon>
      <IconGrid />
    </template>
  </VPageHeader>

  <div class="m-4 space-y-4">
    <VCard title="基础表格">
      <VTable
        :columns="basicColumns"
        :data="posts"
        caption="文章列表"
        row-key="id"
        @row-click="onRowClick"
      />
    </VCard>

    <VCard title="排序">
      <template #actions>
        <span class="px-3 text-xs text-gray-400">
          {{ sortingText }}
        </span>
      </template>
      <VTable
        v-model:sorting="sorting"
        :columns="basicColumns"
        :data="posts"
        row-key="id"
      />
    </VCard>

    <VCard title="多选与批量操作">
      <template #actions>
        <div class="flex items-center gap-3 px-3">
          <span class="text-xs text-gray-400">
            已选择 {{ selectedCount }} 项
          </span>
          <VButton
            :disabled="selectedCount === 0"
            size="sm"
            @click="Toast.success(`已选择 ${selectedCount} 项`)"
          >
            批量操作
          </VButton>
        </div>
      </template>
      <VTable
        v-model:row-selection="rowSelection"
        :columns="basicColumns"
        :data="posts"
        row-key="id"
        selectable
      />
    </VCard>

    <VCard title="展开行详情">
      <VTable :columns="basicColumns" :data="posts" expandable row-key="id">
        <template #expanded="{ row }">
          <div class="grid gap-1">
            <span class="text-xs text-gray-400">摘要</span>
            <p class="text-sm text-gray-600">{{ row.original.summary }}</p>
          </div>
        </template>
      </VTable>
    </VCard>

    <VCard title="多级表头与固定表头">
      <VTable
        :columns="groupedColumns"
        :data="posts"
        :ui="{ root: 'max-h-72' }"
        row-key="id"
        sticky
      />
    </VCard>

    <VCard title="列宽拖拽与表头菜单">
      <VTable
        :columns="featureColumns"
        :data="posts"
        :ui="{ root: 'max-w-3xl' }"
        header-menu
        resizable
        row-key="id"
      />
    </VCard>

    <VCard title="固定列">
      <VTable
        v-model:column-pinning="columnPinning"
        :columns="featureColumns"
        :data="posts"
        :ui="{ root: 'max-w-3xl' }"
        header-menu
        pinnable
        resizable
        row-key="id"
      />
    </VCard>

    <VCard title="虚拟滚动（1000 行）">
      <VTable
        :columns="basicColumns"
        :data="manyPosts"
        :ui="{ root: 'max-h-96' }"
        :virtualize="{ estimateSize: 45 }"
        row-key="id"
        sticky
      />
    </VCard>

    <VCard title="自定义单元格与行操作">
      <VTable :columns="customColumns" :data="posts" row-key="id">
        <template #header-views>阅读量</template>
        <template #cell-title="{ original }">
          <a
            class="font-medium text-gray-900 hover:text-primary"
            href="#"
            @click.prevent
          >
            {{ original.title }}
          </a>
        </template>
        <template #cell-status="{ original }">
          <VTag :theme="statusTheme(original.status)">
            {{ statusText(original.status) }}
          </VTag>
        </template>
        <template #cell-actions>
          <VDropdown>
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
    </VCard>

    <VCard title="列显隐">
      <template #actions>
        <VButton size="sm" type="secondary" @click="resetColumns">
          <template #icon>
            <IconRefreshLine />
          </template>
          重置
        </VButton>
      </template>
      <div class="mb-3 flex flex-wrap gap-4">
        <label
          v-for="item in visibilityOptions"
          :key="item.id"
          class="inline-flex items-center gap-2 text-sm text-gray-600"
        >
          <input
            :checked="columnVisibility[item.id] !== false"
            class="h-4 w-4 rounded border-gray-300 text-primary"
            type="checkbox"
            @change="toggleColumn(item.id)"
          />
          {{ item.label }}
        </label>
      </div>
      <VTable
        v-model:column-visibility="columnVisibility"
        :columns="basicColumns"
        :data="posts"
        row-key="id"
      />
    </VCard>

    <div class="grid gap-4 lg:grid-cols-2">
      <VCard title="空数据">
        <VTable :columns="stateColumns" :data="[]" />
      </VCard>
      <VCard title="加载中">
        <VTable :columns="stateColumns" :data="[]" loading />
      </VCard>
    </div>
  </div>
</template>
