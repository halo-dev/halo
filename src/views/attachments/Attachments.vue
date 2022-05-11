<script lang="ts" setup>
import { BasicLayout } from "@/layouts";
import { VPageHeader } from "@/components/base/header";
import { VButton } from "@/components/base/button";
import { IconPalette } from "@/core/icons";
import { VCard } from "@/components/base/card";
</script>
<template>
  <BasicLayout>
    <VPageHeader title="附件库">
      <template #icon>
        <IconPalette class="self-center mr-2" />
      </template>
      <template #actions>
        <VButton type="secondary">上传</VButton>
      </template>
    </VPageHeader>

    <div class="m-4">
      <ul
        class="grid grid-cols-2 gap-x-2 gap-y-3 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6"
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
  </BasicLayout>
</template>
