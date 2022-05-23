<script lang="ts" setup>
import { VPageHeader } from "@/components/base/header";
import { VButton } from "@/components/base/button";
import { VModal } from "@/components/base/modal";
import { IconPalette, IconSettings } from "@/core/icons";
import { VCard } from "@/components/base/card";
import { VSpace } from "@/components/base/space";
import { ref } from "vue";

const strategyVisible = ref(false);
const attachmentSelectVisible = ref(false);

const strategies = ref([
  {
    id: "1",
    name: "本地存储",
    description: "~/.halo/uploads",
  },
  {
    id: "2",
    name: "阿里云 OSS",
    description: "bucket/blog-attachments",
  },
  {
    id: "3",
    name: "阿里云 OSS",
    description: "bucket/blog-photos",
  },
]);

const selected = ref(strategies.value[0].id);
</script>
<template>
  <VPageHeader title="附件库">
    <template #icon>
      <IconPalette class="self-center mr-2" />
    </template>
    <template #actions>
      <VSpace>
        <VButton type="default" @click="attachmentSelectVisible = true"
          >选择</VButton
        >
        <VButton type="secondary">上传</VButton>
      </VSpace>
    </template>
  </VPageHeader>

  <VModal v-model:visible="strategyVisible" title="添加存储策略"></VModal>

  <VModal
    v-model:visible="attachmentSelectVisible"
    :width="1240"
    title="选择附件"
  >
    <div class="w-full">
      <ul
        class="grid grid-cols-2 gap-x-2 gap-y-3 sm:grid-cols-3 md:grid-cols-2 xl:grid-cols-8 2xl:grid-cols-8"
        role="list"
      >
        <li v-for="i in 40" :key="i" class="relative">
          <VCard :body-class="['!p-0']">
            <div
              class="group block w-full aspect-w-10 aspect-h-7 bg-gray-100 overflow-hidden cursor-pointer"
            >
              <img
                :src="`https://picsum.photos/1000/700?random=${i}`"
                alt=""
                class="object-cover pointer-events-none group-hover:opacity-75"
              />
            </div>
            <p
              class="block text-sm font-medium text-gray-700 truncate pointer-events-none px-2 py-1"
            >
              {{ i * 100 }}
            </p>
          </VCard>
        </li>
      </ul>
    </div>
    <template #footer>
      <VButton type="secondary">确定</VButton>
    </template>
  </VModal>

  <div class="m-4">
    <div class="flex-col flex sm:flex-row gap-2">
      <div class="w-full sm:w-96">
        <VCard :bodyClass="['!p-0']" title="存储策略">
          <fieldset>
            <div class="bg-white -space-y-px divide-y divide-gray-100">
              <label
                v-for="(strategy, index) in strategies"
                :key="index"
                :class="{
                  'bg-gray-50': selected === strategy.id,
                }"
                class="relative p-4 flex cursor-pointer focus:outline-none"
              >
                <input
                  v-model="selected"
                  :value="strategy.id"
                  class="h-4 w-4 mt-0.5 cursor-pointer shrink-0 text-indigo-600 border-gray-300 focus:ring-indigo-500"
                  name="privacy"
                  type="radio"
                />
                <span class="ml-3 flex flex-col flex-1">
                  <span
                    :class="{ 'font-bold': selected === strategy.id }"
                    class="block text-sm font-medium"
                  >
                    {{ strategy.name }}
                  </span>
                  <span class="block text-sm text-gray-400">
                    {{ strategy.description }}
                  </span>
                </span>
                <div class="self-center">
                  <IconSettings
                    class="hover:text-blue-600 cursor-pointer transition-all"
                    @click.stop="strategyVisible = true"
                  />
                </div>
              </label>
            </div>
          </fieldset>

          <template #footer>
            <VButton block type="secondary" @click="strategyVisible = true">
              添加策略
            </VButton>
          </template>
        </VCard>
      </div>
      <div class="w-full">
        <ul
          class="grid grid-cols-2 gap-x-2 gap-y-3 sm:grid-cols-3 md:grid-cols-2 xl:grid-cols-8 2xl:grid-cols-12"
          role="list"
        >
          <li v-for="i in 40" :key="i" class="relative">
            <VCard :body-class="['!p-0']">
              <div
                class="group block w-full aspect-w-10 aspect-h-7 bg-gray-100 overflow-hidden cursor-pointer"
              >
                <img
                  :src="`https://picsum.photos/1000/700?random=${i}`"
                  alt=""
                  class="object-cover pointer-events-none group-hover:opacity-75"
                />
              </div>
              <p
                class="block text-sm font-medium text-gray-700 truncate pointer-events-none px-2 py-1"
              >
                {{ i * 100 }}
              </p>
            </VCard>
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>
