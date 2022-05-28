<script lang="ts" setup>
import { VPageHeader } from "@/components/base/header";
import { VButton } from "@/components/base/button";
import { VTag } from "@/components/base/tag";
import { VSpace } from "@/components/base/space";
import { VCard } from "@/components/base/card";
import { VMenu, VMenuItem } from "@/components/base/menu";
import { IconDeleteBin, IconListSettings } from "@/core/icons";
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
      <IconListSettings class="self-center mr-2" />
    </template>
    <template #actions>
      <VButton type="secondary">重建索引</VButton>
    </template>
  </VPageHeader>
  <div class="m-0 md:m-4">
    <div class="flex-col flex sm:flex-row gap-4">
      <div class="w-full sm:w-96">
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
            <div class="px-4 py-3 block w-full bg-gray-50">
              <div
                class="flex flex-col sm:flex-row items-start sm:items-center relative"
              >
                <div class="w-full sm:w-auto flex flex-1">
                  <span class="text-base font-medium">未分组</span>
                </div>
                <div class="mt-4 sm:mt-0 flex">
                  <VSpace>
                    <VButton size="xs" type="primary">保存</VButton>
                    <VButton size="xs" type="default">新增</VButton>
                  </VSpace>
                </div>
              </div>
            </div>
          </template>
          <ul
            class="divide-y divide-gray-100 box-border w-full h-full"
            role="list"
          >
            <li v-for="(menu, index) in menus" :key="index">
              <div
                class="px-4 py-3 block hover:bg-gray-50 cursor-pointer transition-all relative"
              >
                <div class="flex flex-row items-center relative">
                  <div class="hidden mr-4 sm:flex items-center cursor-move">
                    <IconListSettings />
                  </div>
                  <div class="flex-1">
                    <div class="flex flex-row items-center">
                      <span
                        class="mr-2 text-sm font-medium truncate text-gray-900"
                      >
                        {{ menu.name }}
                      </span>
                      <VTag class="sm:hidden">asd</VTag>
                    </div>
                    <div class="flex mt-1">
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
                      class="inline-flex flex-col items-end gap-4 flex-col-reverse sm:flex-row sm:items-center sm:gap-6"
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
