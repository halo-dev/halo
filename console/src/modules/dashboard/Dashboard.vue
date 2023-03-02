<template>
  <VPageHeader title="仪表盘">
    <template #icon>
      <IconDashboard class="mr-2 self-center" />
    </template>
    <template #actions>
      <VSpace>
        <VButton v-if="settings" @click="widgetsModal = true">
          <template #icon>
            <IconAddCircle class="h-full w-full" />
          </template>
          添加组件
        </VButton>
        <VButton type="secondary" @click="settings = !settings">
          <template #icon>
            <IconSettings v-if="!settings" class="h-full w-full" />
            <IconSave v-else class="h-full w-full" />
          </template>
          {{ settings ? "完成" : "设置" }}
        </VButton>
      </VSpace>
    </template>
  </VPageHeader>

  <div class="dashboard m-4">
    <grid-layout
      v-model:layout="layout"
      :col-num="12"
      :is-draggable="settings"
      :is-resizable="settings"
      :margin="[10, 10]"
      :responsive="false"
      :row-height="30"
      :use-css-transforms="true"
      :vertical-compact="true"
    >
      <grid-item
        v-for="(item, index) in layout"
        :key="index"
        :h="item.h"
        :i="item.i"
        :w="item.w"
        :x="item.x"
        :y="item.y"
      >
        <component :is="item.widget" />
        <div v-if="settings" class="absolute top-2 right-2">
          <IconCloseCircle
            class="cursor-pointer text-lg text-gray-500 hover:text-gray-900"
            @click="handleRemove(item)"
          />
        </div>
      </grid-item>
    </grid-layout>
  </div>

  <VModal
    v-model:visible="widgetsModal"
    height="calc(100vh - 20px)"
    :width="1280"
    :layer-closable="true"
    title="小组件"
  >
    <VTabbar
      v-model:active-id="activeId"
      :items="
        widgetsGroup.map((group) => {
          return { id: group.id, label: group.label };
        })
      "
      type="outline"
    ></VTabbar>

    <template v-for="(group, groupIndex) in widgetsGroup" :key="groupIndex">
      <div v-if="activeId === group.id" class="mt-4">
        <grid-layout
          :col-num="12"
          :is-draggable="false"
          :is-resizable="false"
          :layout="group.widgets"
          :margin="[10, 10]"
          :responsive="true"
          :row-height="30"
          :use-css-transforms="true"
          :vertical-compact="true"
        >
          <grid-item
            v-for="(item, index) in group.widgets"
            :key="index"
            :h="item.h"
            :i="item.i"
            :w="item.w"
            :x="item.x"
            :y="item.y"
            class="cursor-pointer"
            @click="handleAddWidget(item)"
          >
            <component :is="item.widget" />
          </grid-item>
        </grid-layout>
      </div>
    </template>
  </VModal>
</template>
<script lang="ts" setup>
import {
  IconAddCircle,
  IconCloseCircle,
  IconDashboard,
  IconSave,
  IconSettings,
  VButton,
  VModal,
  VPageHeader,
  VSpace,
  VTabbar,
} from "@halo-dev/components";
import { onMounted, provide, ref, type Ref } from "vue";
import { useStorage } from "@vueuse/core";
import cloneDeep from "lodash.clonedeep";
import { apiClient } from "@/utils/api-client";
import type { DashboardStats } from "@halo-dev/api-client/index";

const widgetsGroup = [
  {
    id: "post",
    label: "文章",
    widgets: [
      { x: 0, y: 0, w: 3, h: 3, i: 0, widget: "PostStatsWidget" },
      { x: 0, y: 0, w: 6, h: 10, i: 1, widget: "RecentPublishedWidget" },
    ],
  },
  {
    id: "page",
    label: "页面",
    widgets: [
      { x: 0, y: 0, w: 3, h: 3, i: 0, widget: "SinglePageStatsWidget" },
    ],
  },
  {
    id: "comment",
    label: "评论",
    widgets: [{ x: 0, y: 0, w: 3, h: 3, i: 0, widget: "CommentStatsWidget" }],
  },
  {
    id: "user",
    label: "用户",
    widgets: [
      { x: 0, y: 0, w: 3, h: 3, i: 0, widget: "UserStatsWidget" },
      { x: 0, y: 0, w: 3, h: 3, i: 1, widget: "UserProfileWidget" },
    ],
  },
  {
    id: "other",
    label: "其他",
    widgets: [
      { x: 0, y: 0, w: 3, h: 3, i: 0, widget: "ViewsStatsWidget" },
      { x: 0, y: 0, w: 6, h: 10, i: 1, widget: "QuickLinkWidget" },
    ],
  },
];

const settings = ref(false);
const widgetsModal = ref(false);
const activeId = ref(widgetsGroup[0].id);

const layout = useStorage("widgets", [
  { x: 0, y: 0, w: 3, h: 3, i: 0, widget: "PostStatsWidget" },
  { x: 3, y: 0, w: 3, h: 3, i: 1, widget: "UserStatsWidget" },
  { x: 6, y: 0, w: 3, h: 3, i: 2, widget: "CommentStatsWidget" },
  { x: 9, y: 0, w: 3, h: 3, i: 3, widget: "ViewsStatsWidget" },
  { x: 0, y: 3, w: 4, h: 10, i: 4, widget: "QuickLinkWidget" },
  {
    x: 4,
    y: 3,
    w: 4,
    h: 10,
    i: 5,
    widget: "RecentPublishedWidget",
  },
]);

// eslint-disable-next-line
function handleAddWidget(widget: any) {
  layout.value = [
    ...layout.value,
    {
      ...widget,
      i: layout.value.length,
    },
  ];
}

// eslint-disable-next-line
function handleRemove(item: any) {
  const cloneWidgets = cloneDeep(layout.value);
  cloneWidgets.splice(item.i, 1);
  // eslint-disable-next-line
  layout.value = cloneWidgets.map((widget: any, index: number) => {
    return {
      ...widget,
      i: index,
    };
  });
}

// Dashboard basic stats

const dashboardStats = ref<DashboardStats>({
  posts: 0,
  comments: 0,
  approvedComments: 0,
  users: 0,
  visits: 0,
});

provide<Ref<DashboardStats>>("dashboardStats", dashboardStats);

const handleFetchStats = async () => {
  const { data } = await apiClient.stats.getStats();
  dashboardStats.value = data;
};

onMounted(handleFetchStats);
</script>

<style>
.vue-grid-layout {
  @apply -m-[10px];
}

.vue-grid-item {
  transition: none !important;
}

.vue-grid-item.vue-grid-placeholder {
  @apply bg-gray-200 !important;
  @apply opacity-100 !important;
}
</style>
