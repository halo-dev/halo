<template>
  <BasicLayout>
    <header class="bg-white">
      <div class="h-48 bg-gradient-to-r from-gray-800 to-red-500"></div>
      <div class="px-4 sm:px-6 lg:px-8">
        <div class="-mt-12 sm:-mt-16 flex items-end space-x-5">
          <div class="flex">
            <img
              alt="Avatar"
              class="h-24 w-24 rounded-full ring-4 ring-white sm:h-32 sm:w-32 drop-shadow-lg"
              src="https://ryanc.cc/avatar"
            />
          </div>
          <div
            class="mt-6 sm:flex-1 sm:min-w-0 sm:flex sm:items-center sm:justify-end sm:space-x-6 sm:pb-1"
          >
            <div class="block mt-6 min-w-0 flex-1">
              <h1 class="text-xl font-bold text-gray-900 truncate">
                <span class="mr-1">Ryan Wang</span>
                <VTag theme="default">
                  <template #leftIcon>
                    <IconUserSettings />
                  </template>
                  管理员
                </VTag>
              </h1>
            </div>
            <div
              class="hidden mt-6 md:flex flex-col justify-stretch space-y-3 sm:flex-row sm:space-y-0 sm:space-x-4"
            >
              <VButton type="default">退出登录</VButton>
            </div>
          </div>
        </div>
      </div>
    </header>
    <section class="bg-white p-4 sm:px-6 lg:px-8">
      <VTabs v-model:activeId="activeTab">
        <VTabItem id="general" label="基本资料">
          <form class="space-y-8 divide-y divide-gray-200">
            <div class="space-y-8 divide-y divide-gray-200 sm:space-y-5">
              <div class="mt-6 sm:mt-5 space-y-6 sm:space-y-5">
                <div
                  class="sm:grid sm:grid-cols-3 sm:gap-4 sm:items-start sm:border-t sm:border-gray-200 sm:pt-5"
                >
                  <label
                    class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
                  >
                    用户名
                  </label>
                  <div class="mt-1 sm:mt-0 sm:col-span-2">
                    <div class="max-w-lg flex shadow-sm">
                      <VInput modelValue="ryanwang" />
                    </div>
                  </div>
                </div>
                <div
                  class="sm:grid sm:grid-cols-3 sm:gap-4 sm:items-start sm:border-t sm:border-gray-200 sm:pt-5"
                >
                  <label
                    class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
                  >
                    昵称
                  </label>
                  <div class="mt-1 sm:mt-0 sm:col-span-2">
                    <div class="max-w-lg flex shadow-sm">
                      <VInput modelValue="Ryan Wang" />
                    </div>
                  </div>
                </div>

                <div
                  class="sm:grid sm:grid-cols-3 sm:gap-4 sm:items-start sm:border-t sm:border-gray-200 sm:pt-5"
                >
                  <label
                    class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
                  >
                    电子邮箱
                  </label>
                  <div class="mt-1 sm:mt-0 sm:col-span-2">
                    <div class="max-w-lg flex shadow-sm">
                      <VInput modelValue="i@ryanc.cc" />
                    </div>
                  </div>
                </div>
                <div
                  class="sm:grid sm:grid-cols-3 sm:gap-4 sm:items-start sm:border-t sm:border-gray-200 sm:pt-5"
                >
                  <label
                    class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
                  >
                    个人说明
                  </label>
                  <div class="mt-1 sm:mt-0 sm:col-span-2">
                    <div class="max-w-lg flex shadow-sm">
                      <VTextarea modelValue="Halo" />
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="pt-5">
              <div class="flex justify-start">
                <VButton type="secondary"> 保存</VButton>
              </div>
            </div>
          </form>
        </VTabItem>
        <VTabItem id="password" label="密码">
          <form class="space-y-8 divide-y divide-gray-200">
            <div class="space-y-8 divide-y divide-gray-200 sm:space-y-5">
              <div class="mt-6 sm:mt-5 space-y-6 sm:space-y-5">
                <div
                  class="sm:grid sm:grid-cols-3 sm:gap-4 sm:items-start sm:border-t sm:border-gray-200 sm:pt-5"
                >
                  <label
                    class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
                  >
                    原密码
                  </label>
                  <div class="mt-1 sm:mt-0 sm:col-span-2">
                    <div class="max-w-lg flex shadow-sm">
                      <VInput />
                    </div>
                  </div>
                </div>
                <div
                  class="sm:grid sm:grid-cols-3 sm:gap-4 sm:items-start sm:border-t sm:border-gray-200 sm:pt-5"
                >
                  <label
                    class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
                  >
                    新密码
                  </label>
                  <div class="mt-1 sm:mt-0 sm:col-span-2">
                    <div class="max-w-lg flex shadow-sm">
                      <VInput />
                    </div>
                  </div>
                </div>

                <div
                  class="sm:grid sm:grid-cols-3 sm:gap-4 sm:items-start sm:border-t sm:border-gray-200 sm:pt-5"
                >
                  <label
                    class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
                  >
                    确认密码
                  </label>
                  <div class="mt-1 sm:mt-0 sm:col-span-2">
                    <div class="max-w-lg flex shadow-sm">
                      <VInput />
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="pt-5">
              <div class="flex justify-start">
                <VButton type="secondary">修改密码</VButton>
              </div>
            </div>
          </form>
        </VTabItem>
      </VTabs>
    </section>
  </BasicLayout>
</template>
<script lang="ts" setup>
import { BasicLayout } from "@/layouts";
import { VButton } from "@/components/base/button";
import { VInput } from "@/components/base/input";
import { VTextarea } from "@/components/base/textarea";
import { VTag } from "@/components/base/tag";
import { VTabItem, VTabs } from "@/components/base/tabs";
import { IconUserSettings } from "@/core/icons";
import { ref } from "vue";

const activeTab = ref("general");
</script>
