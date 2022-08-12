<script lang="ts" setup>
import {
  IconBookRead,
  IconSave,
  VButton,
  VPageHeader,
} from "@halo-dev/components";
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
    <RichTextEditor />
  </div>
</template>
