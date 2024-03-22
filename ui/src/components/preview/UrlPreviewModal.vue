<script lang="ts" setup>
import { axiosInstance } from "@/utils/api-client";
import {
  VModal,
  IconLink,
  VTabbar,
  IconComputer,
  IconTablet,
  IconPhone,
  VLoading,
} from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import { toRefs } from "vue";
import { computed, markRaw, ref } from "vue";

const props = withDefaults(
  defineProps<{
    title?: string;
    url?: string;
  }>(),
  {
    title: undefined,
    url: "",
  }
);

const { url } = toRefs(props);

const emit = defineEmits<{
  (event: "close"): void;
}>();

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

const { data: html, isLoading } = useQuery({
  queryKey: ["url-preview", url],
  queryFn: async () => {
    const { data } = await axiosInstance.get(url.value, {
      headers: {
        Accept: "text/html",
        "Cache-Control": "no-cache",
        Pragma: "no-cache",
        Expires: "0",
      },
    });
    return data;
  },
  enabled: computed(() => !!url.value),
});
</script>
<template>
  <VModal
    :body-class="['!p-0']"
    fullscreen
    :title="title"
    :layer-closable="true"
    @close="emit('close')"
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
      <VLoading v-if="isLoading" />
      <iframe
        v-else
        class="border-none transition-all duration-500"
        :class="iframeClasses"
        :srcdoc="html"
      ></iframe>
    </div>
  </VModal>
</template>
