<script lang="ts" setup>
import {
  IconComputer,
  IconPhone,
  IconSave,
  IconSettings,
  IconTablet,
  VButton,
  VCard,
  VInput,
  VModal,
  VOption,
  VSelect,
  VSpace,
  VTabbar,
  VTabItem,
  VTabs,
  VTextarea,
} from "@halo-dev/components";
import { computed, onMounted, ref } from "vue";

const activeId = ref("general");
const deviceActiveId = ref("desktop");
const attachmentSelectVisible = ref(false);
const settingRootVisible = ref(false);
const settingVisible = ref(false);
const devices = ref([
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

const attachments = Array.from(new Array(50), (_, index) => index).map(
  (index) => {
    return {
      id: index,
      name: `attachment-${index}`,
      url: `https://picsum.photos/1000/700?random=${index}`,
      size: "1.2MB",
      type: "image/png",
      strategy: "本地存储",
    };
  }
);

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
  <VModal
    v-model:visible="attachmentSelectVisible"
    :width="1240"
    title="选择附件"
  >
    <div class="w-full">
      <ul
        class="grid grid-cols-2 gap-x-2 gap-y-3 sm:grid-cols-3 md:grid-cols-2 xl:grid-cols-8 2xl:grid-cols-8"
        role="list"
      >
        <li
          v-for="(attachment, index) in attachments"
          :key="index"
          class="relative"
        >
          <VCard :body-class="['!p-0']">
            <div
              class="group aspect-w-10 aspect-h-7 block w-full cursor-pointer overflow-hidden bg-gray-100"
            >
              <img
                :src="attachment.url"
                alt=""
                class="pointer-events-none object-cover group-hover:opacity-75"
              />
            </div>
            <p
              class="pointer-events-none block truncate px-2 py-1 text-sm font-medium text-gray-700"
            >
              {{ attachment.name }}
            </p>
          </VCard>
        </li>
      </ul>
    </div>
    <template #footer>
      <VButton type="secondary">确定</VButton>
    </template>
  </VModal>
  <div class="flex h-screen">
    <div class="flex-1">
      <div
        class="grid h-16 grid-cols-2 items-center bg-white py-2 px-4 drop-shadow-sm sm:grid-cols-3"
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
            <VButton type="default" @click="settingVisible = true">
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
                  <form>
                    <div
                      class="space-y-8 divide-y divide-gray-200 sm:space-y-5"
                    >
                      <div class="space-y-6 sm:space-y-5">
                        <div class="space-y-2">
                          <label
                            class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
                            for="first-name"
                          >
                            Halo 当前版本：
                          </label>
                          <div class="mt-1 sm:col-span-2 sm:mt-0">
                            <VInput model-value="1.5.3"></VInput>
                          </div>
                        </div>

                        <div class="space-y-2">
                          <label
                            class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
                            for="last-name"
                          >
                            首页图片：
                          </label>
                          <div class="mt-1 sm:col-span-2 sm:mt-0">
                            <VInput
                              model-value="https://halo.run/upload/2022/03/support-team.svg"
                            ></VInput>
                          </div>
                        </div>
                      </div>
                    </div>
                  </form>
                </VTabItem>
                <VTabItem id="style" class="p-3" label="样式设置">
                  <form>
                    <div
                      class="space-y-8 divide-y divide-gray-200 sm:space-y-5"
                    >
                      <div class="space-y-6 sm:space-y-5">
                        <div class="space-y-2">
                          <label
                            class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
                            for="first-name"
                          >
                            文章代码高亮语言：
                          </label>
                          <div class="mt-1 sm:col-span-2 sm:mt-0">
                            <VTextarea></VTextarea>
                          </div>
                        </div>

                        <div class="space-y-2">
                          <label
                            class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
                            for="last-name"
                          >
                            文章代码高亮主题：
                          </label>
                          <div class="mt-1 sm:col-span-2 sm:mt-0">
                            <VSelect>
                              <VOption value="java">Java</VOption>
                              <VOption value="c">C</VOption>
                              <VOption value="go">Go</VOption>
                              <VOption value="javascript">JavaScript</VOption>
                            </VSelect>
                          </div>
                        </div>
                      </div>
                    </div>
                  </form>
                </VTabItem>
              </VTabs>
            </div>
          </div>
        </div>
      </transition>
    </div>
  </Teleport>
</template>
