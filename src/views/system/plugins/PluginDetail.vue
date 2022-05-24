<script lang="ts" setup>
import { VPageHeader } from "@/components/base/header";
import { VButton } from "@/components/base/button";
import { useRoute } from "vue-router";
import { plugins } from "./plugins-mock";
import { VTag } from "@/components/base/tag";
import { VCard } from "@/components/base/card";

const { params } = useRoute();

const plugin = plugins.find((item) => {
  return item.spec.pluginClass === params.id;
});

console.log(plugin);
</script>

<template>
  <VPageHeader :title="plugin.metadata.name">
    <template #icon>
      <img :src="plugin.spec.logo" class="w-8 h-8 mr-2" />
    </template>
    <template #actions>
      <VButton type="secondary"> 安装</VButton>
    </template>
  </VPageHeader>

  <div class="m-0 md:m-4">
    <VCard :body-class="['!p-0']">
      <div class="px-4 py-5 sm:px-6">
        <h3 class="text-lg leading-6 font-medium text-gray-900">插件信息</h3>
        <p class="mt-1 max-w-2xl text-sm text-gray-500 flex items-center gap-2">
          <span>{{ plugin.spec.version }}</span>
          <VTag>
            {{ plugin.metadata.enabled ? "已启用" : "未启用" }}
          </VTag>
        </p>
      </div>
      <div class="border-t border-gray-200">
        <dl>
          <div
            class="bg-white px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
          >
            <dt class="text-sm font-medium text-gray-900">名称</dt>
            <dd class="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">
              {{ plugin.metadata.name }}
            </dd>
          </div>
          <div
            class="bg-gray-50 px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
          >
            <dt class="text-sm font-medium text-gray-900">插件类别</dt>
            <dd class="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">
              <VTag>
                extensions.halo.run/category/{{
                  plugin.metadata.labels["extensions.halo.run/category"]
                }}
              </VTag>
            </dd>
          </div>
          <div
            class="bg-white px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
          >
            <dt class="text-sm font-medium text-gray-900">版本</dt>
            <dd class="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">
              {{ plugin.spec.version }}
            </dd>
          </div>
          <div
            class="bg-gray-50 px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
          >
            <dt class="text-sm font-medium text-gray-900">Halo 版本要求</dt>
            <dd class="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">
              {{ plugin.spec.requires }}
            </dd>
          </div>
          <div
            class="bg-white px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
          >
            <dt class="text-sm font-medium text-gray-900">提供方</dt>
            <dd class="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">
              <a :href="plugin.spec.homepage" target="_blank">
                @{{ plugin.spec.author }}
              </a>
            </dd>
          </div>
          <div
            class="bg-gray-50 px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
          >
            <dt class="text-sm font-medium text-gray-900">协议</dt>
            <dd class="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">
              {{ plugin.spec.license }}
            </dd>
          </div>
          <div
            class="bg-white px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
          >
            <dt class="text-sm font-medium text-gray-900">模型定义</dt>
            <dd class="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">无</dd>
          </div>
        </dl>
      </div>
    </VCard>
  </div>
</template>

<style lang="scss" scoped></style>
