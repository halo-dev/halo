<script lang="ts" setup>
import {
  IconDeleteBin,
  IconListSettings,
  VButton,
  VCard,
  VMenu,
  VMenuItem,
  VPageHeader,
  VSpace,
  VTag,
} from "@halo-dev/components";
import { ref } from "vue";

const menus = ref([
  {
    id: 1,
    name: "首页",
    url: "https://halo.run",
    priority: 0,
    target: "_self",
    icon: "",
    parentId: 0,
    team: "main",
    children: [],
  },
  {
    id: 4,
    name: "官方文档",
    url: "https://docs.halo.run",
    priority: 1,
    target: "_blank",
    icon: "",
    parentId: 0,
    team: "main",
    children: [],
  },
  {
    id: 3,
    name: "主题仓库",
    url: "/themes.html",
    priority: 2,
    target: "_self",
    icon: "",
    parentId: 0,
    team: "main",
    children: [],
  },
  {
    id: 36,
    name: "博客",
    url: "/blog.html",
    priority: 3,
    target: "_self",
    icon: "",
    parentId: 0,
    team: "main",
    children: [],
  },
  {
    id: 40,
    name: "论坛",
    url: "https://bbs.halo.run",
    priority: 4,
    target: "_blank",
    icon: "",
    parentId: 0,
    team: "main",
    children: [],
  },
  {
    id: 168,
    name: "在线体验",
    url: "https://docs.halo.run/#%E5%9C%A8%E7%BA%BF%E4%BD%93%E9%AA%8C",
    priority: 5,
    target: "_blank",
    icon: "",
    parentId: 0,
    team: "main",
    children: [],
  },
]);
</script>
<template>
  <VPageHeader title="菜单">
    <template #icon>
      <IconListSettings class="mr-2 self-center" />
    </template>
    <template #actions>
      <VButton type="secondary">重建索引</VButton>
    </template>
  </VPageHeader>
  <div class="m-0 md:m-4">
    <div class="flex flex-col gap-4 sm:flex-row">
      <div class="w-96">
        <VCard title="分组">
          <VMenu class="!p-0">
            <VMenuItem id="default" active title="未分组"></VMenuItem>
            <VMenuItem id="community" title="社区"></VMenuItem>
          </VMenu>
          <template #footer>
            <VButton block type="secondary">新增</VButton>
          </template>
        </VCard>
      </div>
      <div class="flex-1">
        <VCard :body-class="['!p-0']">
          <template #header>
            <div class="block w-full bg-gray-50 px-4 py-3">
              <div
                class="relative flex flex-col items-start sm:flex-row sm:items-center"
              >
                <div class="flex w-full flex-1 sm:w-auto">
                  <span class="text-base font-medium">未分组</span>
                </div>
                <div class="mt-4 flex sm:mt-0">
                  <VSpace>
                    <VButton size="xs" type="primary">保存</VButton>
                    <VButton size="xs" type="default">新增</VButton>
                  </VSpace>
                </div>
              </div>
            </div>
          </template>
          <ul
            class="box-border h-full w-full divide-y divide-gray-100"
            role="list"
          >
            <li v-for="(menu, index) in menus" :key="index">
              <div
                class="relative block cursor-pointer px-4 py-3 transition-all hover:bg-gray-50"
              >
                <div class="relative flex flex-row items-center">
                  <div class="mr-4 hidden cursor-move items-center sm:flex">
                    <IconListSettings />
                  </div>
                  <div class="flex-1">
                    <div class="flex flex-row items-center">
                      <span
                        class="mr-2 truncate text-sm font-medium text-gray-900"
                      >
                        {{ menu.name }}
                      </span>
                      <VTag class="sm:hidden">asd</VTag>
                    </div>
                    <div class="mt-1 flex">
                      <VSpace align="start" direction="column" spacing="xs">
                        <a
                          :href="menu.url"
                          class="text-xs text-gray-500 hover:text-gray-900"
                          target="_blank"
                        >
                          {{ menu.url }}
                        </a>
                      </VSpace>
                    </div>
                  </div>
                  <div class="flex">
                    <div
                      class="inline-flex flex-col flex-col-reverse items-end gap-4 sm:flex-row sm:items-center sm:gap-6"
                    >
                      <span class="cursor-pointer text-sm hover:text-red-600">
                        <IconDeleteBin />
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </li>
          </ul>
        </VCard>
      </div>
    </div>
  </div>
</template>
