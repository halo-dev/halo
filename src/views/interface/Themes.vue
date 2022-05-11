<script lang="ts" setup>
import { FilledLayout } from "@/layouts";
import { VPageHeader } from "@/components/base/header";
import { VButton } from "@/components/base/button";
import { IconPalette } from "@/core/icons";
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
  </FilledLayout>
</template>
