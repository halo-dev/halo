<script lang="ts" setup>
import { VPageHeader } from "@/components/base/header";
import { VButton } from "@/components/base/button";
import { VCard } from "@/components/base/card";
import { IconPalette } from "@/core/icons";
import { themes } from "@/views/interface/themes/themes-mock";
</script>
<template>
  <VPageHeader title="主题">
    <template #icon>
      <IconPalette class="mr-2 self-center" />
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
            class="group aspect-w-10 aspect-h-7 block w-full cursor-pointer overflow-hidden bg-gray-100"
          >
            <img
              :src="theme.screenshots"
              alt=""
              class="pointer-events-none object-cover group-hover:opacity-75"
            />
          </div>
          <p
            class="pointer-events-none block truncate px-2 py-1 text-sm font-medium text-gray-700"
          >
            {{ theme.name }}
          </p>
        </VCard>
      </li>
    </ul>
  </div>
</template>
