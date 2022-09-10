<script lang="ts" setup>
import {
  IconComputer,
  IconPhone,
  IconSave,
  IconSettings,
  IconTablet,
  VButton,
  VSpace,
  VTabbar,
  VTabItem,
  VTabs,
} from "@halo-dev/components";
import AttachmentSelectorModal from "@/modules/contents/attachments/components/AttachmentSelectorModal.vue";
import { computed, onMounted, ref, shallowRef } from "vue";

const activeId = ref("general");
const deviceActiveId = ref("desktop");
const attachmentSelectVisible = ref(false);
const settingRootVisible = ref(false);
const settingVisible = ref(false);
const devices = shallowRef([
  {
    id: "desktop",
    icon: IconComputer,
  },
  {
    id: "tablet",
    icon: IconTablet,
  },
  {
    id: "phone",
    icon: IconPhone,
  },
]);

const iframeClasses = computed(() => {
  if (deviceActiveId.value === "desktop") {
    return "w-full h-full";
  }
  if (deviceActiveId.value === "tablet") {
    return "w-2/3 h-2/3";
  }
  // phone
  return "w-96 h-[50rem]";
});

onMounted(() => {
  window.addEventListener(
    "message",
    function receiveMessageFromIframePage(event) {
      if (event.data === "select_image") {
        attachmentSelectVisible.value = true;
      }
    },
    false
  );
});
</script>
<template>
  <AttachmentSelectorModal
    v-model:visible="attachmentSelectVisible"
  ></AttachmentSelectorModal>
  <div class="flex h-screen">
    <div class="flex-1">
      <div
        class="grid h-14 grid-cols-2 items-center bg-white px-4 drop-shadow-sm sm:grid-cols-3"
      >
        <div>
          <h2 class="truncate text-xl font-bold text-gray-800">
            <span>Anatole</span>
          </h2>
        </div>
        <div class="hidden justify-center sm:flex">
          <VTabbar
            v-model:active-id="deviceActiveId"
            :items="devices"
            type="outline"
          ></VTabbar>
        </div>
        <div class="flex justify-end">
          <VSpace>
            <VButton size="sm" type="default" @click="settingVisible = true">
              <template #icon>
                <IconSettings class="h-full w-full" />
              </template>
              设置
            </VButton>
            <VButton type="secondary">
              <template #icon>
                <IconSave class="h-full w-full" />
              </template>
              保存
            </VButton>
          </VSpace>
        </div>
      </div>
      <div
        class="flex h-full items-center justify-center"
        style="height: calc(100vh - 4rem)"
      >
        <iframe
          :class="iframeClasses"
          class="border-none transition-all duration-300"
          src="http://localhost:8090"
        ></iframe>
      </div>
    </div>
  </div>

  <Teleport to="body">
    <div
      v-show="settingRootVisible"
      class="drawer-wrapper fixed top-0 left-0 z-[99999] flex h-full w-full flex-row items-end justify-center"
    >
      <transition
        enter-active-class="ease-out duration-200"
        enter-from-class="opacity-0"
        enter-to-class="opacity-100"
        leave-active-class="ease-in duration-100"
        leave-from-class="opacity-100"
        leave-to-class="opacity-0"
        @before-enter="settingRootVisible = true"
        @after-leave="settingRootVisible = false"
      >
        <div
          v-show="settingVisible"
          class="drawer-layer absolute top-0 left-0 h-full w-full flex-none bg-gray-500 bg-opacity-75 transition-opacity"
          @click="settingVisible = false"
        ></div>
      </transition>
      <transition
        enter-active-class="transform transition ease-in-out duration-300"
        enter-from-class="translate-y-full"
        enter-to-class="translate-y-0"
        leave-active-class="transform transition ease-in-out duration-300"
        leave-from-class="translate-y-0"
        leave-to-class="translate-y-full"
      >
        <div
          v-show="settingVisible"
          class="drawer-content relative flex h-3/4 w-screen flex-col items-stretch overflow-y-auto rounded-t-md bg-white shadow-xl"
        >
          <div class="drawer-body">
            <div class="h-full w-full overflow-y-auto bg-white drop-shadow-sm">
              <VTabs v-model:active-id="activeId" type="outline">
                <VTabItem id="general" class="p-3" label="基础设置">
                  <FormKit :actions="false" type="form">
                    <FormKit label="Halo 当前版本" type="text"></FormKit>
                    <FormKit label="首页图片" type="text"></FormKit>
                  </FormKit>
                </VTabItem>
                <VTabItem id="style" class="p-3" label="样式设置">
                  <FormKit :actions="false" type="form">
                    <FormKit label="文章代码高亮语言" type="text"></FormKit>
                    <FormKit
                      :options="[
                        { label: 'Java', value: 'java' },
                        { label: 'C', value: 'c' },
                        { label: 'Go', value: 'go' },
                        { label: 'JavaScript', value: 'javascript' },
                      ]"
                      label="文章代码高亮主题"
                      type="select"
                    ></FormKit>
                  </FormKit>
                </VTabItem>
              </VTabs>
            </div>
          </div>
        </div>
      </transition>
    </div>
  </Teleport>
</template>
