<script lang="ts" setup>
import { VPageHeader } from "@/components/base/header";
import { VButton } from "@/components/base/button";
import { IconBookRead, IconSave } from "@/core/icons";
</script>

<template>
  <VPageHeader title="文章">
    <template #icon>
      <IconBookRead class="self-center mr-2" />
    </template>
    <template #actions>
      <VButton :route="{ name: 'PostEditor' }" type="secondary">
        <template #icon>
          <IconSave class="w-full h-full" />
        </template>
        发布
      </VButton>
    </template>
  </VPageHeader>
  <div class="editor">
    <!--mock-->
    <iframe
      class="w-full h-screen border-0"
      src="https://stackedit.io/app"
    ></iframe>
  </div>
</template>
