<script lang="ts" setup>
import {
  IconTerminalBoxLine,
  IconClipboardLine,
  VAlert,
  VPageHeader,
  VCard,
  VButton,
  Toast,
} from "@halo-dev/components";
import { computed, onMounted, ref } from "vue";
import type { Info, GlobalInfo, Startup } from "./types";
import axios from "axios";
import { formatDatetime } from "@/utils/date";
import { useClipboard } from "@vueuse/core";

const info = ref<Info>();
const globalInfo = ref<GlobalInfo>();
const startup = ref<Startup>();

const handleFetchActuatorInfo = async () => {
  const { data } = await axios.get(
    `${import.meta.env.VITE_API_URL}/actuator/info`,
    {
      withCredentials: true,
    }
  );
  info.value = data;
};

const handleFetchActuatorGlobalInfo = async () => {
  const { data } = await axios.get(
    `${import.meta.env.VITE_API_URL}/actuator/globalinfo`,
    {
      withCredentials: true,
    }
  );
  globalInfo.value = data;
};

const handleFetchActuatorStartup = async () => {
  const { data } = await axios.get(
    `${import.meta.env.VITE_API_URL}/actuator/startup`,
    {
      withCredentials: true,
    }
  );
  startup.value = data;
};

const isExternalUrlValid = computed(() => {
  if (!globalInfo.value?.externalUrl) {
    return true;
  }
  const url = new URL(globalInfo.value.externalUrl);
  const { host: currentHost, protocol: currentProtocol } = window.location;
  return url.host === currentHost && url.protocol === currentProtocol;
});

onMounted(() => {
  handleFetchActuatorInfo();
  handleFetchActuatorGlobalInfo();
  handleFetchActuatorStartup();
});

// copy system information to clipboard
const { copy, isSupported } = useClipboard();

const handleCopy = () => {
  if (!isSupported.value) {
    Toast.warning("当前浏览器不支持复制");
  }

  const text = `
- 外部访问地址：${globalInfo.value?.externalUrl}
- 启动时间：${formatDatetime(startup.value?.timeline.startTime)}
- Halo 版本：${info.value?.build?.version}
- 构建时间：${formatDatetime(info.value?.build?.time)}
- Git Commit：${info.value?.git?.commit.id}
- Java：${info.value?.java.runtime.name} / ${info.value?.java.runtime.version}
- 数据库：${info.value?.database.name} / ${info.value?.database.version}
- 操作系统：${info.value?.os.name} / ${info.value?.os.version}
  `;

  copy(text);

  Toast.success("复制成功");
};

const handleDownloadLogfile = () => {
  axios
    .get(`${import.meta.env.VITE_API_URL}/actuator/logfile`)
    .then((response) => {
      const blob = new Blob([response.data]);
      const downloadElement = document.createElement("a");
      const href = window.URL.createObjectURL(blob);
      downloadElement.href = href;
      downloadElement.download = `halo-log-${formatDatetime(new Date())}.log`;
      document.body.appendChild(downloadElement);
      downloadElement.click();
      document.body.removeChild(downloadElement);
      window.URL.revokeObjectURL(href);

      Toast.success("下载成功");
    })
    .catch((e) => {
      Toast.error("下载失败");
      console.log("Failed to download log file.", e);
    });
};
</script>

<template>
  <VPageHeader title="系统概览">
    <template #icon>
      <IconTerminalBoxLine class="mr-2 self-center" />
    </template>
    <template #actions>
      <VButton size="sm" @click="handleCopy">
        <template #icon>
          <IconClipboardLine class="h-full w-full" />
        </template>
        复制
      </VButton>
    </template>
  </VPageHeader>

  <div class="m-0 flex flex-col gap-4 md:m-4">
    <VCard :body-class="['!p-0']">
      <div class="bg-white">
        <div
          class="flex items-center justify-between bg-white px-4 py-4 sm:px-6"
        >
          <div>
            <h3 class="text-lg font-medium leading-6 text-gray-900">
              基本信息
            </h3>
          </div>
        </div>
        <div class="border-t border-gray-200">
          <dl class="divide-y divide-gray-100">
            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">外部访问地址</dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
                <span>
                  {{ globalInfo?.externalUrl }}
                </span>
                <VAlert
                  v-if="!isExternalUrlValid"
                  class="mt-3"
                  type="warning"
                  title="警告"
                  :closable="false"
                >
                  <template #description>
                    检测到外部访问地址与当前访问地址不一致，可能会导致部分链接无法正常跳转，请检查外部访问地址设置。
                  </template>
                </VAlert>
              </dd>
            </div>
            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">启动时间</dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
                {{ formatDatetime(startup?.timeline.startTime) }}
              </dd>
            </div>
            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">时区</dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
                {{ globalInfo?.timeZone }}
              </dd>
            </div>
            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">语言</dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
                {{ globalInfo?.locale }}
              </dd>
            </div>
          </dl>
        </div>
      </div>
    </VCard>
    <VCard v-if="info" :body-class="['!p-0']">
      <div class="bg-white">
        <div
          class="flex items-center justify-between bg-white px-4 py-4 sm:px-6"
        >
          <div>
            <h3 class="text-lg font-medium leading-6 text-gray-900">
              环境信息
            </h3>
          </div>
        </div>
        <div class="border-t border-gray-200">
          <dl class="divide-y divide-gray-100">
            <div
              v-if="info.build"
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">版本</dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
                <a
                  :href="`https://github.com/halo-dev/halo/releases/tag/v${info.build.version}`"
                  class="hover:text-gray-600"
                  target="_blank"
                >
                  {{ info.build.version }}
                </a>
              </dd>
            </div>
            <div
              v-if="info.build"
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">构建时间</dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
                {{ formatDatetime(info.build.time) }}
              </dd>
            </div>
            <div
              v-if="info.git"
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">Git Commit</dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
                <a
                  :href="`https://github.com/halo-dev/halo/commit/${info.git.commit.id}`"
                  class="hover:text-gray-600"
                  target="_blank"
                >
                  {{ info.git.commit.id }}
                </a>
              </dd>
            </div>
            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">Java</dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
                {{ info.java.runtime.name }} / {{ info.java.runtime.version }}
              </dd>
            </div>
            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">数据库</dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
                {{ [info.database.name, info.database.version].join(" / ") }}
              </dd>
            </div>
            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">操作系统</dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
                {{ info.os.name }} {{ info.os.version }} / {{ info.os.arch }}
              </dd>
            </div>
            <div
              class="items-center bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">运行日志</dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
                <VButton size="sm" @click="handleDownloadLogfile()">
                  下载
                </VButton>
              </dd>
            </div>
          </dl>
        </div>
      </div>
    </VCard>
  </div>
</template>
