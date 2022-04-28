<script lang="ts" setup>
import { VAlert } from "./index";
import { VButton } from "@/components/base/button";
import { IconMessage } from "@/core/icons";

function initState() {
  return {
    type: "default",
    title: "提示",
    description: "这是一个提示",
    closable: true,
  };
}
</script>

<template>
  <Story title="Alert">
    <Variant title="Show Cases">
      <template #default>
        <div class="p-3">
          <VAlert type="default" title="default" />
        </div>
        <div class="p-3">
          <VAlert type="success" title="default" />
        </div>
        <div class="p-3">
          <VAlert type="info" title="default" />
        </div>
        <div class="p-3">
          <VAlert type="warning" title="default" />
        </div>
        <div class="p-3">
          <VAlert type="error" title="default" />
        </div>
        <div class="p-3">
          <VAlert type="default" title="提示">
            <template #description>
              目前仅支持远程 Git 仓库和 ZIP 下载链接。更多主题可以访问：
              <a href="https://halo.run/themes">https://halo.run/themes</a>
            </template>
            <template #actions>
              <div class="flex justify-end">
                <VButton type="default">访问</VButton>
              </div>
            </template>
          </VAlert>
        </div>
      </template>
    </Variant>

    <Variant title="Playground" :init-state="initState">
      <template #default="{ state }">
        <div class="p-3">
          <VAlert
            :type="state.type"
            :title="state.title"
            :description="state.description"
            :closable="state.closable"
          />
        </div>
      </template>

      <template #controls="{ state }">
        <HstText v-model="state.type" title="Type" />
        <HstText v-model="state.title" title="Title" />
        <HstText v-model="state.description" title="Description" />
        <HstCheckbox v-model="state.closable" title="Closable" />
      </template>
    </Variant>

    <Variant title="Slots">
      <template #default>
        <div class="p-3">
          <VAlert type="default" title="你有新的消息">
            <template #icon>
              <IconMessage />
            </template>
            <template #description>
              @Ryan Wang 在你的站点创建了新评论
            </template>
            <template #actions>
              <div class="flex justify-end">
                <VButton type="default">查看</VButton>
              </div>
            </template>
          </VAlert>
        </div>
      </template>
    </Variant>
  </Story>
</template>
