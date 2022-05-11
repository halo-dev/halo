<script lang="ts" setup>
import { FilledLayout } from "@/layouts";
import { VPageHeader } from "@/components/base/header";
import { VButton } from "@/components/base/button";
import { VCard } from "@/components/base/card";
import { IconPalette } from "@/core/icons";
import { themes } from "@/views/interface/themes-mock";
</script>
<template>
  <FilledLayout>
    <VPageHeader title="主题">
      <template #icon>
        <IconPalette class="self-center mr-2" />
      </template>
      <template #actions>
        <VButton type="secondary">安装</VButton>
      </template>
    </VPageHeader>

    <div class="m-4">
      <ul
        class="grid grid-cols-1 gap-x-2 gap-y-3 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6"
        role="list"
      >
        <li v-for="(theme, index) in themes" :key="index" class="relative">
          <VCard :body-class="['!p-0']">
            <div
              class="group block w-full aspect-w-10 aspect-h-7 bg-gray-100 overflow-hidden cursor-pointer"
            >
              <img
                :src="theme.screenshots"
                alt=""
                class="object-cover pointer-events-none group-hover:opacity-75"
              />
            </div>
            <p
              class="block text-sm font-medium text-gray-700 truncate pointer-events-none px-2 py-1"
            >
              {{ theme.name }}
            </p>
          </VCard>
        </li>
      </ul>
    </div>
  </FilledLayout>
</template>
