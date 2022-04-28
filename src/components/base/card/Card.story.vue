<script lang="ts" setup>
import { VCard } from "./index";
import { VInput } from "@/components/base/input";
import { VSpace } from "@/components/base/space";
import { VButton } from "@/components/base/button";
import { IconSettings } from "@/core/icons";
</script>

<template>
  <Story title="Card">
    <template #default>
      <div class="p-3">
        <VCard title="登录">
          <VSpace direction="column" class="w-full">
            <VInput placeholder="用户名"></VInput>
            <VInput placeholder="密码"></VInput>
          </VSpace>

          <template #actions>
            <div style="padding: 12px 16px">
              <IconSettings />
            </div>
          </template>

          <template #footer>
            <VSpace>
              <VButton type="secondary">登录</VButton>
              <VButton type="default">取消</VButton>
            </VSpace>
          </template>
        </VCard>
      </div>
      <!--      https://lofiui.co/-->
      <div class="p-3">
        <VCard title="Page Views">
          <div class="flex flex-col items-center w-full bg-white">
            <div class="flex items-end flex-grow w-full space-x-2 sm:space-x-3">
              <div
                class="relative flex flex-col items-center flex-grow pb-5 group"
              >
                <span
                  class="absolute top-0 hidden -mt-6 text-xs font-bold group-hover:block"
                  >$37,500</span
                >
                <div class="flex items-end w-full">
                  <div
                    class="relative flex justify-center flex-grow h-8 bg-indigo-200"
                  ></div>
                  <div
                    class="relative flex justify-center flex-grow h-6 bg-indigo-300"
                  ></div>
                  <div
                    class="relative flex justify-center flex-grow h-16 bg-indigo-400"
                  ></div>
                </div>
                <span class="absolute bottom-0 text-xs font-bold">Jan</span>
              </div>
              <div
                class="relative flex flex-col items-center flex-grow pb-5 group"
              >
                <span
                  class="absolute top-0 hidden -mt-6 text-xs font-bold group-hover:block"
                  >$45,000</span
                >
                <div class="flex items-end w-full">
                  <div
                    class="relative flex justify-center flex-grow h-10 bg-indigo-200"
                  ></div>
                  <div
                    class="relative flex justify-center flex-grow h-6 bg-indigo-300"
                  ></div>
                  <div
                    class="relative flex justify-center flex-grow h-20 bg-indigo-400"
                  ></div>
                </div>
                <span class="absolute bottom-0 text-xs font-bold">Feb</span>
              </div>
              <div
                class="relative flex flex-col items-center flex-grow pb-5 group"
              >
                <span
                  class="absolute top-0 hidden -mt-6 text-xs font-bold group-hover:block"
                  >$47,500</span
                >
                <div class="flex items-end w-full">
                  <div
                    class="relative flex justify-center flex-grow h-10 bg-indigo-200"
                  ></div>
                  <div
                    class="relative flex justify-center flex-grow h-8 bg-indigo-300"
                  ></div>
                  <div
                    class="relative flex justify-center flex-grow h-20 bg-indigo-400"
                  ></div>
                </div>
                <span class="absolute bottom-0 text-xs font-bold">Mar</span>
              </div>
              <div
                class="relative flex flex-col items-center flex-grow pb-5 group"
              >
                <span
                  class="absolute top-0 hidden -mt-6 text-xs font-bold group-hover:block"
                  >$50,000</span
                >
                <div class="flex items-end w-full">
                  <div
                    class="relative flex justify-center flex-grow h-10 bg-indigo-200"
                  ></div>
                  <div
                    class="relative flex justify-center flex-grow h-6 bg-indigo-300"
                  ></div>
                  <div
                    class="relative flex justify-center flex-grow h-24 bg-indigo-400"
                  ></div>
                </div>
                <span class="absolute bottom-0 text-xs font-bold">Apr</span>
              </div>
              <div
                class="relative flex flex-col items-center flex-grow pb-5 group"
              >
                <span
                  class="absolute top-0 hidden -mt-6 text-xs font-bold group-hover:block"
                  >$47,500</span
                >
                <div class="flex items-end w-full">
                  <div
                    class="relative flex justify-center flex-grow h-10 bg-indigo-200"
                  ></div>
                  <div
                    class="relative flex justify-center flex-grow h-8 bg-indigo-300"
                  ></div>
                  <div
                    class="relative flex justify-center flex-grow h-20 bg-indigo-400"
                  ></div>
                </div>
                <span class="absolute bottom-0 text-xs font-bold">May</span>
              </div>
              <div
                class="relative flex flex-col items-center flex-grow pb-5 group"
              >
                <span
                  class="absolute top-0 hidden -mt-6 text-xs font-bold group-hover:block"
                  >$55,000</span
                >
                <div class="flex items-end w-full">
                  <div
                    class="relative flex justify-center flex-grow h-12 bg-indigo-200"
                  ></div>
                  <div
                    class="relative flex justify-center flex-grow h-8 bg-indigo-300"
                  ></div>
                  <div
                    class="relative flex justify-center flex-grow h-24 bg-indigo-400"
                  ></div>
                </div>
                <span class="absolute bottom-0 text-xs font-bold">Jun</span>
              </div>
              <div
                class="relative flex flex-col items-center flex-grow pb-5 group"
              >
                <span
                  class="absolute top-0 hidden -mt-6 text-xs font-bold group-hover:block"
                  >$60,000</span
                >
                <div class="flex items-end w-full">
                  <div
                    class="relative flex justify-center flex-grow h-12 bg-indigo-200"
                  ></div>
                  <div
                    class="relative flex justify-center flex-grow h-16 bg-indigo-300"
                  ></div>
                  <div
                    class="relative flex justify-center flex-grow h-20 bg-indigo-400"
                  ></div>
                </div>
                <span class="absolute bottom-0 text-xs font-bold">Jul</span>
              </div>
              <div
                class="relative flex flex-col items-center flex-grow pb-5 group"
              >
                <span
                  class="absolute top-0 hidden -mt-6 text-xs font-bold group-hover:block"
                  >$57,500</span
                >
                <div class="flex items-end w-full">
                  <div
                    class="relative flex justify-center flex-grow h-12 bg-indigo-200"
                  ></div>
                  <div
                    class="relative flex justify-center flex-grow h-10 bg-indigo-300"
                  ></div>
                  <div
                    class="relative flex justify-center flex-grow h-24 bg-indigo-400"
                  ></div>
                </div>
                <span class="absolute bottom-0 text-xs font-bold">Aug</span>
              </div>
              <div
                class="relative flex flex-col items-center flex-grow pb-5 group"
              >
                <span
                  class="absolute top-0 hidden -mt-6 text-xs font-bold group-hover:block"
                  >$67,500</span
                >
                <div class="flex items-end w-full">
                  <div
                    class="relative flex justify-center flex-grow h-12 bg-indigo-200"
                  ></div>
                  <div
                    class="relative flex justify-center flex-grow h-10 bg-indigo-300"
                  ></div>
                  <div
                    class="relative flex justify-center flex-grow h-32 bg-indigo-400"
                  ></div>
                </div>
                <span class="absolute bottom-0 text-xs font-bold">Sep</span>
              </div>
              <div
                class="relative flex flex-col items-center flex-grow pb-5 group"
              >
                <span
                  class="absolute top-0 hidden -mt-6 text-xs font-bold group-hover:block"
                  >$65,000</span
                >
                <div class="flex items-end w-full">
                  <div
                    class="relative flex justify-center flex-grow h-12 bg-indigo-200"
                  ></div>
                  <div
                    class="relative flex justify-center flex-grow h-12 bg-indigo-300"
                  ></div>
                  <div
                    class="relative flex justify-center flex-grow bg-indigo-400 h-28"
                  ></div>
                </div>
                <span class="absolute bottom-0 text-xs font-bold">Oct</span>
              </div>
              <div
                class="relative flex flex-col items-center flex-grow pb-5 group"
              >
                <span
                  class="absolute top-0 hidden -mt-6 text-xs font-bold group-hover:block"
                  >$70,000</span
                >
                <div class="flex items-end w-full">
                  <div
                    class="relative flex justify-center flex-grow h-8 bg-indigo-200"
                  ></div>
                  <div
                    class="relative flex justify-center flex-grow h-8 bg-indigo-300"
                  ></div>
                  <div
                    class="relative flex justify-center flex-grow h-40 bg-indigo-400"
                  ></div>
                </div>
                <span class="absolute bottom-0 text-xs font-bold">Nov</span>
              </div>
              <div
                class="relative flex flex-col items-center flex-grow pb-5 group"
              >
                <span
                  class="absolute top-0 hidden -mt-6 text-xs font-bold group-hover:block"
                  >$75,000</span
                >
                <div class="flex items-end w-full">
                  <div
                    class="relative flex justify-center flex-grow h-12 bg-indigo-200"
                  ></div>
                  <div
                    class="relative flex justify-center flex-grow h-8 bg-indigo-300"
                  ></div>
                  <div
                    class="relative flex justify-center flex-grow h-40 bg-indigo-400"
                  ></div>
                </div>
                <span class="absolute bottom-0 text-xs font-bold">Dec</span>
              </div>
            </div>
            <div class="flex w-full mt-3">
              <div class="flex items-center ml-auto">
                <span class="block w-4 h-4 bg-indigo-400"></span>
                <span class="ml-1 text-xs font-medium">Existing</span>
              </div>
              <div class="flex items-center ml-4">
                <span class="block w-4 h-4 bg-indigo-300"></span>
                <span class="ml-1 text-xs font-medium">Upgrades</span>
              </div>
              <div class="flex items-center ml-4">
                <span class="block w-4 h-4 bg-indigo-200"></span>
                <span class="ml-1 text-xs font-medium">New</span>
              </div>
            </div>
          </div>
        </VCard>
      </div>

      <!--      https://lofiui.co/-->
      <div class="p-3">
        <VCard title="Page Views">
          <div class="flex flex-col items-center w-full bg-white">
            <div class="flex items-end flex-grow w-full space-x-2 sm:space-x-3">
              <div
                class="relative flex flex-col items-center flex-grow pb-5 group"
              >
                <span
                  class="absolute top-0 hidden -mt-6 text-xs font-bold group-hover:block"
                  >$37,500</span
                >
                <div
                  class="relative flex justify-center w-full h-8 bg-indigo-200"
                ></div>
                <div
                  class="relative flex justify-center w-full h-6 bg-indigo-300"
                ></div>
                <div
                  class="relative flex justify-center w-full h-16 bg-indigo-400"
                ></div>
                <span class="absolute bottom-0 text-xs font-bold">Jan</span>
              </div>
              <div
                class="relative flex flex-col items-center flex-grow pb-5 group"
              >
                <span
                  class="absolute top-0 hidden -mt-6 text-xs font-bold group-hover:block"
                  >$45,000</span
                >
                <div
                  class="relative flex justify-center w-full h-10 bg-indigo-200"
                ></div>
                <div
                  class="relative flex justify-center w-full h-6 bg-indigo-300"
                ></div>
                <div
                  class="relative flex justify-center w-full h-20 bg-indigo-400"
                ></div>
                <span class="absolute bottom-0 text-xs font-bold">Feb</span>
              </div>
              <div
                class="relative flex flex-col items-center flex-grow pb-5 group"
              >
                <span
                  class="absolute top-0 hidden -mt-6 text-xs font-bold group-hover:block"
                  >$47,500</span
                >
                <div
                  class="relative flex justify-center w-full h-10 bg-indigo-200"
                ></div>
                <div
                  class="relative flex justify-center w-full h-8 bg-indigo-300"
                ></div>
                <div
                  class="relative flex justify-center w-full h-20 bg-indigo-400"
                ></div>
                <span class="absolute bottom-0 text-xs font-bold">Mar</span>
              </div>
              <div
                class="relative flex flex-col items-center flex-grow pb-5 group"
              >
                <span
                  class="absolute top-0 hidden -mt-6 text-xs font-bold group-hover:block"
                  >$50,000</span
                >
                <div
                  class="relative flex justify-center w-full h-10 bg-indigo-200"
                ></div>
                <div
                  class="relative flex justify-center w-full h-6 bg-indigo-300"
                ></div>
                <div
                  class="relative flex justify-center w-full h-24 bg-indigo-400"
                ></div>
                <span class="absolute bottom-0 text-xs font-bold">Apr</span>
              </div>
              <div
                class="relative flex flex-col items-center flex-grow pb-5 group"
              >
                <span
                  class="absolute top-0 hidden -mt-6 text-xs font-bold group-hover:block"
                  >$47,500</span
                >
                <div
                  class="relative flex justify-center w-full h-10 bg-indigo-200"
                ></div>
                <div
                  class="relative flex justify-center w-full h-8 bg-indigo-300"
                ></div>
                <div
                  class="relative flex justify-center w-full h-20 bg-indigo-400"
                ></div>
                <span class="absolute bottom-0 text-xs font-bold">May</span>
              </div>
              <div
                class="relative flex flex-col items-center flex-grow pb-5 group"
              >
                <span
                  class="absolute top-0 hidden -mt-6 text-xs font-bold group-hover:block"
                  >$55,000</span
                >
                <div
                  class="relative flex justify-center w-full h-12 bg-indigo-200"
                ></div>
                <div
                  class="relative flex justify-center w-full h-8 bg-indigo-300"
                ></div>
                <div
                  class="relative flex justify-center w-full h-24 bg-indigo-400"
                ></div>
                <span class="absolute bottom-0 text-xs font-bold">Jun</span>
              </div>
              <div
                class="relative flex flex-col items-center flex-grow pb-5 group"
              >
                <span
                  class="absolute top-0 hidden -mt-6 text-xs font-bold group-hover:block"
                  >$60,000</span
                >
                <div
                  class="relative flex justify-center w-full h-12 bg-indigo-200"
                ></div>
                <div
                  class="relative flex justify-center w-full h-16 bg-indigo-300"
                ></div>
                <div
                  class="relative flex justify-center w-full h-20 bg-indigo-400"
                ></div>
                <span class="absolute bottom-0 text-xs font-bold">Jul</span>
              </div>
              <div
                class="relative flex flex-col items-center flex-grow pb-5 group"
              >
                <span
                  class="absolute top-0 hidden -mt-6 text-xs font-bold group-hover:block"
                  >$57,500</span
                >
                <div
                  class="relative flex justify-center w-full h-12 bg-indigo-200"
                ></div>
                <div
                  class="relative flex justify-center w-full h-10 bg-indigo-300"
                ></div>
                <div
                  class="relative flex justify-center w-full h-24 bg-indigo-400"
                ></div>
                <span class="absolute bottom-0 text-xs font-bold">Aug</span>
              </div>
              <div
                class="relative flex flex-col items-center flex-grow pb-5 group"
              >
                <span
                  class="absolute top-0 hidden -mt-6 text-xs font-bold group-hover:block"
                  >$67,500</span
                >
                <div
                  class="relative flex justify-center w-full h-12 bg-indigo-200"
                ></div>
                <div
                  class="relative flex justify-center w-full h-10 bg-indigo-300"
                ></div>
                <div
                  class="relative flex justify-center w-full h-32 bg-indigo-400"
                ></div>
                <span class="absolute bottom-0 text-xs font-bold">Sep</span>
              </div>
              <div
                class="relative flex flex-col items-center flex-grow pb-5 group"
              >
                <span
                  class="absolute top-0 hidden -mt-6 text-xs font-bold group-hover:block"
                  >$65,000</span
                >
                <div
                  class="relative flex justify-center w-full h-12 bg-indigo-200"
                ></div>
                <div
                  class="relative flex justify-center w-full h-12 bg-indigo-300"
                ></div>
                <div
                  class="relative flex justify-center w-full bg-indigo-400 h-28"
                ></div>
                <span class="absolute bottom-0 text-xs font-bold">Oct</span>
              </div>
              <div
                class="relative flex flex-col items-center flex-grow pb-5 group"
              >
                <span
                  class="absolute top-0 hidden -mt-6 text-xs font-bold group-hover:block"
                  >$70,000</span
                >
                <div
                  class="relative flex justify-center w-full h-8 bg-indigo-200"
                ></div>
                <div
                  class="relative flex justify-center w-full h-8 bg-indigo-300"
                ></div>
                <div
                  class="relative flex justify-center w-full h-40 bg-indigo-400"
                ></div>
                <span class="absolute bottom-0 text-xs font-bold">Nov</span>
              </div>
              <div
                class="relative flex flex-col items-center flex-grow pb-5 group"
              >
                <span
                  class="absolute top-0 hidden -mt-6 text-xs font-bold group-hover:block"
                  >$75,000</span
                >
                <div
                  class="relative flex justify-center w-full h-12 bg-indigo-200"
                ></div>
                <div
                  class="relative flex justify-center w-full h-8 bg-indigo-300"
                ></div>
                <div
                  class="relative flex justify-center w-full h-40 bg-indigo-400"
                ></div>
                <span class="absolute bottom-0 text-xs font-bold">Dec</span>
              </div>
            </div>
            <div class="flex w-full mt-3">
              <div class="flex items-center ml-auto">
                <span class="block w-4 h-4 bg-indigo-400"></span>
                <span class="ml-1 text-xs font-medium">Existing</span>
              </div>
              <div class="flex items-center ml-4">
                <span class="block w-4 h-4 bg-indigo-300"></span>
                <span class="ml-1 text-xs font-medium">Upgrades</span>
              </div>
              <div class="flex items-center ml-4">
                <span class="block w-4 h-4 bg-indigo-200"></span>
                <span class="ml-1 text-xs font-medium">New</span>
              </div>
            </div>
          </div>
        </VCard>
      </div>

      <div class="p-3">
        <div class="grid lg:grid-cols-3 md:grid-cols-2 gap-6 w-full">
          <VCard>
            <div class="flex items-center bg-white">
              <div
                class="flex flex-shrink-0 items-center justify-center bg-green-200 h-16 w-16 rounded"
              >
                <svg
                  class="w-6 h-6 fill-current text-green-700"
                  xmlns="http://www.w3.org/2000/svg"
                  viewBox="0 0 20 20"
                  fill="currentColor"
                >
                  <path
                    fill-rule="evenodd"
                    d="M3.293 9.707a1 1 0 010-1.414l6-6a1 1 0 011.414 0l6 6a1 1 0 01-1.414 1.414L11 5.414V17a1 1 0 11-2 0V5.414L4.707 9.707a1 1 0 01-1.414 0z"
                    clip-rule="evenodd"
                  />
                </svg>
              </div>
              <div class="flex-grow flex flex-col ml-4">
                <span class="text-xl font-bold">$8,430</span>
                <div class="flex items-center justify-between">
                  <span class="text-gray-500">Revenue last 30 days</span>
                  <span class="text-green-500 text-sm font-semibold ml-2"
                    >+12.6%</span
                  >
                </div>
              </div>
            </div>
          </VCard>

          <VCard>
            <div class="flex items-center bg-white">
              <div
                class="flex flex-shrink-0 items-center justify-center bg-red-200 h-16 w-16 rounded"
              >
                <svg
                  class="w-6 h-6 fill-current text-red-700"
                  xmlns="http://www.w3.org/2000/svg"
                  viewBox="0 0 20 20"
                  fill="currentColor"
                >
                  <path
                    fill-rule="evenodd"
                    d="M16.707 10.293a1 1 0 010 1.414l-6 6a1 1 0 01-1.414 0l-6-6a1 1 0 111.414-1.414L9 14.586V3a1 1 0 012 0v11.586l4.293-4.293a1 1 0 011.414 0z"
                    clip-rule="evenodd"
                  />
                </svg>
              </div>
              <div class="flex-grow flex flex-col ml-4">
                <span class="text-xl font-bold">211</span>
                <div class="flex items-center justify-between">
                  <span class="text-gray-500">Sales last 30 days</span>
                  <span class="text-red-500 text-sm font-semibold ml-2"
                    >-8.1%</span
                  >
                </div>
              </div>
            </div>
          </VCard>

          <VCard>
            <div class="flex items-center bg-white">
              <div
                class="flex flex-shrink-0 items-center justify-center bg-green-200 h-16 w-16 rounded"
              >
                <svg
                  class="w-6 h-6 fill-current text-green-700"
                  xmlns="http://www.w3.org/2000/svg"
                  viewBox="0 0 20 20"
                  fill="currentColor"
                >
                  <path
                    fill-rule="evenodd"
                    d="M3.293 9.707a1 1 0 010-1.414l6-6a1 1 0 011.414 0l6 6a1 1 0 01-1.414 1.414L11 5.414V17a1 1 0 11-2 0V5.414L4.707 9.707a1 1 0 01-1.414 0z"
                    clip-rule="evenodd"
                  />
                </svg>
              </div>
              <div class="flex-grow flex flex-col ml-4">
                <span class="text-xl font-bold">140</span>
                <div class="flex items-center justify-between">
                  <span class="text-gray-500">Customers last 30 days</span>
                  <span class="text-green-500 text-sm font-semibold ml-2"
                    >+28.4%</span
                  >
                </div>
              </div>
            </div>
          </VCard>
        </div>
      </div>
    </template>
  </Story>
</template>
