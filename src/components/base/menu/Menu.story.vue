<template>
  <Story title="Menu">
    <Variant title="Playground">
      <template #default>
        <div class="w-1/3">
          <VMenu>
            <VMenuItem title="仪表盘" active>
              <template #icon>
                <Component :is="IconDashboard" />
              </template>
            </VMenuItem>
            <VMenuLabel>内容</VMenuLabel>
            <VMenuItem title="文章">
              <template #icon>
                <Component :is="IconBookRead" />
              </template>
            </VMenuItem>
            <VMenuItem title="页面">
              <template #icon>
                <Component :is="IconPages" />
              </template>
            </VMenuItem>
            <VMenuItem title="评论">
              <template #icon>
                <Component :is="IconMessage" />
              </template>
            </VMenuItem>
            <VMenuItem title="附件">
              <template #icon>
                <Component :is="IconFolder" />
              </template>
            </VMenuItem>
          </VMenu>
        </div>
      </template>
    </Variant>
  </Story>
</template>
<script lang="ts" setup>
import { VMenu, VMenuItem, VMenuLabel } from "./index";
import {
  IconDashboard,
  IconBookRead,
  IconPages,
  IconMessage,
  IconFolder,
} from "@/core/icons";
</script>
