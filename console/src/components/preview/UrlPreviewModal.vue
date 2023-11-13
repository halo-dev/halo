<script lang="ts" setup>
import {
  VModal,
  IconLink,
  VTabbar,
  IconComputer,
  IconTablet,
  IconPhone,
} from "@halo-dev/components";
import { computed, markRaw, ref } from "vue";

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

const mockDevices = [
  {
    id: "desktop",
    icon: markRaw(IconComputer),
  },
  {
    id: "tablet",
    icon: markRaw(IconTablet),
  },
  {
    id: "phone",
    icon: markRaw(IconPhone),
  },
];
const deviceActiveId = ref(mockDevices[0].id);

const iframeClasses = computed(() => {
  if (deviceActiveId.value === "desktop") {
    return "w-full h-full";
  }
  if (deviceActiveId.value === "tablet") {
    return "w-2/3 h-2/3 ring-2 rounded ring-gray-300";
  }
  return "w-96 h-[50rem] ring-2 rounded ring-gray-300";
});
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
    <template #center>
      <!-- TODO: Reactor VTabbar component to support icon prop -->
      <VTabbar
        v-model:active-id="deviceActiveId"
        :items="mockDevices as any"
        type="outline"
      ></VTabbar>
    </template>
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
        class="border-none transition-all duration-500"
        :class="iframeClasses"
        :src="url"
      ></iframe>
    </div>
  </VModal>
</template>
