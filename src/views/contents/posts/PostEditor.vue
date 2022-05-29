<script lang="ts" setup>
import { VPageHeader } from "@/components/base/header";
import { VButton } from "@/components/base/button";
import { IconBookRead, IconSave } from "@/core/icons";
</script>

<template>
  <VPageHeader title="文章">
    <template #icon>
      <IconBookRead class="mr-2 self-center" />
    </template>
    <template #actions>
      <VButton :route="{ name: 'PostEditor' }" type="secondary">
        <template #icon>
          <IconSave class="h-full w-full" />
        </template>
        发布
      </VButton>
    </template>
  </VPageHeader>
  <div class="editor">
    <!--mock-->
    <iframe
      class="h-screen w-full border-0"
      src="https://stackedit.io/app"
    ></iframe>
  </div>
</template>
