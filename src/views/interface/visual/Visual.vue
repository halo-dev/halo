<script lang="ts" setup>
import { VButton } from "@/components/base/button";
import { VInput } from "@/components/base/input";
import { VOption, VSelect } from "@/components/base/select";
import { VModal } from "@/components/base/modal";
import { VTextarea } from "@/components/base/textarea";
import { VCard } from "@/components/base/card";
import { VTabbar, VTabItem, VTabs } from "@/components/base/tabs";
import { computed, onMounted, ref } from "vue";
import { IconComputer, IconPhone, IconTablet } from "@/core/icons";

const activeId = ref("general");
const deviceActiveId = ref("desktop");
const attachmentSelectVisible = ref(false);
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
    <div class="h-full w-96 overflow-y-auto bg-white drop-shadow-sm">
      <VTabs v-model:active-id="activeId" type="outline">
        <VTabItem id="general" class="p-3" label="基础设置">
          <form>
            <div class="space-y-8 divide-y divide-gray-200 sm:space-y-5">
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
            <div class="space-y-8 divide-y divide-gray-200 sm:space-y-5">
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
    <div class="flex-1">
      <div
        class="flex h-16 items-center justify-between bg-white p-2 drop-shadow-sm"
      >
        <div>
          <h2 class="truncate text-xl font-bold text-gray-800">
            <span>Anatole</span>
          </h2>
        </div>
        <div>
          <VTabbar
            v-model:active-id="deviceActiveId"
            :items="devices"
            type="outline"
          ></VTabbar>
        </div>
        <div>
          <VButton type="secondary">保存</VButton>
        </div>
      </div>
      <div
        class="flex h-full items-center justify-center"
        style="height: calc(100vh - 4rem)"
      >
        <iframe
          :class="iframeClasses"
          class="border-none transition-all duration-200"
          src="http://localhost:8090"
        ></iframe>
      </div>
    </div>
  </div>
</template>
