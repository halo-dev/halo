<script lang="ts" setup>
import { VAlert } from "./index";
import { VButton } from "./../button";
import { IconMessage } from "../../icons/icons";

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
          <VAlert title="default" type="default" />
        </div>
        <div class="p-3">
          <VAlert title="default" type="success" />
        </div>
        <div class="p-3">
          <VAlert title="default" type="info" />
        </div>
        <div class="p-3">
          <VAlert title="default" type="warning" />
        </div>
        <div class="p-3">
          <VAlert title="default" type="error" />
        </div>
        <div class="p-3">
          <VAlert title="提示" type="default">
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

    <Variant :init-state="initState" title="Playground">
      <template #default="{ state }">
        <div class="p-3">
          <VAlert
            :closable="state.closable"
            :description="state.description"
            :title="state.title"
            :type="state.type"
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
          <VAlert title="你有新的消息" type="default">
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
