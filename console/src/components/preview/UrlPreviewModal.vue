<script lang="ts" setup>
import { VModal, IconLink } from "@halo-dev/components";

withDefaults(
  defineProps<{
    visible: boolean;
    title?: string;
    url?: string;
  }>(),
  {
    visible: false,
    title: undefined,
    url: "",
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
    fullscreen
    :title="title"
    :layer-closable="true"
    @update:visible="onVisibleChange"
  >
    <template #actions>
      <slot name="actions"></slot>
      <span>
        <a :href="url" target="_blank">
          <IconLink />
        </a>
      </span>
    </template>
    <div class="flex h-full items-center justify-center">
      <iframe
        v-if="visible"
        class="h-full w-full border-none transition-all duration-300"
        :src="url"
      ></iframe>
    </div>
  </VModal>
</template>
