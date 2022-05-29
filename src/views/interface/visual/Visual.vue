<script lang="ts" setup>
import { VButton } from "@/components/base/button";
import { VInput } from "@/components/base/input";
import { VOption, VSelect } from "@/components/base/select";
import { VTextarea } from "@/components/base/textarea";
import { VTabbar, VTabItem, VTabs } from "@/components/base/tabs";
import { computed, ref } from "vue";
import { IconComputer, IconPhone, IconTablet } from "@/core/icons";

const activeId = ref("general");
const deviceActiveId = ref("desktop");

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
</script>
<template>
  <div class="flex h-screen">
    <div class="h-full w-96 overflow-y-auto bg-white drop-shadow-sm">
      <VTabs v-model:active-id="activeId">
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
            :items="[
              {
                id: 'desktop',
                icon: IconComputer,
              },
              {
                id: 'tablet',
                icon: IconTablet,
              },
              {
                id: 'phone',
                icon: IconPhone,
              },
            ]"
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
