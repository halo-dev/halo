<script lang="ts" setup>
import type { Post } from "@halo-dev/api-client";
import { VModal, IconLink } from "@halo-dev/components";

withDefaults(
  defineProps<{
    visible: boolean;
    post?: Post | null;
  }>(),
  {
    visible: false,
    post: null,
  }
);

const emit = defineEmits<{
  (event: "update:visible", visible: boolean): void;
  (event: "close"): void;
}>();

const onVisibleChange = (visible: boolean) => {
  emit("update:visible", visible);
  if (!visible) {
    emit("close");
  }
};
</script>
<template>
  <VModal
    :body-class="['!p-0']"
    :visible="visible"
    :layer-closable="true"
    fullscreen
    title="文章预览"
    @update:visible="onVisibleChange"
  >
    <template #actions>
      <span>
        <a
          href="https://halo.run/archives/halo-154-released.html"
          target="_blank"
        >
          <IconLink />
        </a>
      </span>
    </template>
    <div class="flex h-full items-center justify-center">
      <iframe
        v-if="visible"
        class="h-full w-full border-none transition-all duration-300"
        src="https://halo.run/archives/halo-154-released.html"
      ></iframe>
    </div>
  </VModal>
</template>
