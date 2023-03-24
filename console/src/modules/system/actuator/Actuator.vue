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
import { useI18n } from "vue-i18n";

const { t } = useI18n();

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
    Toast.warning(t("core.actuator.actions.copy.toast_browser_not_supported"));
    return;
  }

  const text = `
- ${t("core.actuator.copy_results.external_url", {
    external_url: globalInfo.value?.externalUrl,
  })}
- ${t("core.actuator.copy_results.start_time", {
    start_time: formatDatetime(startup.value?.timeline.startTime),
  })}
- ${t("core.actuator.fields.version", { version: info.value?.build?.version })}
- ${t("core.actuator.copy_results.build_time", {
    build_time: formatDatetime(info.value?.build?.time),
  })}
- Git Commit：${info.value?.git?.commit.id}
- Java：${info.value?.java.runtime.name} / ${info.value?.java.runtime.version}
- ${t("core.actuator.copy_results.database", {
    database: [info.value?.database.name, info.value?.database.version].join(
      " / "
    ),
  })}
- ${t("core.actuator.copy_results.os", {
    os: [info.value?.os.name, info.value?.os.version].join(" / "),
  })}
  `;

  copy(text);

  Toast.success(t("core.common.toast.copy_success"));
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

      Toast.success(t("core.common.toast.download_success"));
    })
    .catch((e) => {
      Toast.error(t("core.common.toast.download_failed"));
      console.log("Failed to download log file.", e);
    });
};
</script>

<template>
  <VPageHeader :title="$t('core.actuator.title')">
    <template #icon>
      <IconTerminalBoxLine class="mr-2 self-center" />
    </template>
    <template #actions>
      <VButton size="sm" @click="handleCopy">
        <template #icon>
          <IconClipboardLine class="h-full w-full" />
        </template>
        {{ $t("core.common.buttons.copy") }}
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
              {{ $t("core.actuator.header.titles.general") }}
            </h3>
          </div>
        </div>
        <div class="border-t border-gray-200">
          <dl class="divide-y divide-gray-100">
            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">
                {{ $t("core.actuator.fields.external_url") }}
              </dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
                <span>
                  {{ globalInfo?.externalUrl }}
                </span>
                <VAlert
                  v-if="!isExternalUrlValid"
                  class="mt-3"
                  type="warning"
                  :title="$t('core.common.text.warning')"
                  :closable="false"
                >
                  <template #description>
                    {{ $t("core.actuator.alert.external_url_invalid") }}
                  </template>
                </VAlert>
              </dd>
            </div>
            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">
                {{ $t("core.actuator.fields.start_time") }}
              </dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
                {{ formatDatetime(startup?.timeline.startTime) }}
              </dd>
            </div>
            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">
                {{ $t("core.actuator.fields.timezone") }}
              </dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
                {{ globalInfo?.timeZone }}
              </dd>
            </div>
            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">
                {{ $t("core.actuator.fields.locale") }}
              </dt>
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
              {{ $t("core.actuator.header.titles.environment") }}
            </h3>
          </div>
        </div>
        <div class="border-t border-gray-200">
          <dl class="divide-y divide-gray-100">
            <div
              v-if="info.build"
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">
                {{ $t("core.actuator.fields.version") }}
              </dt>
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
              <dt class="text-sm font-medium text-gray-900">
                {{ $t("core.actuator.fields.build_time") }}
              </dt>
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
              <dt class="text-sm font-medium text-gray-900">
                {{ $t("core.actuator.fields.database") }}
              </dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
                {{ [info.database.name, info.database.version].join(" / ") }}
              </dd>
            </div>
            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">
                {{ $t("core.actuator.fields.os") }}
              </dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
                {{ info.os.name }} {{ info.os.version }} / {{ info.os.arch }}
              </dd>
            </div>
            <div
              class="items-center bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">
                {{ $t("core.actuator.fields.log") }}
              </dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
                <VButton size="sm" @click="handleDownloadLogfile()">
                  {{ $t("core.common.buttons.download") }}
                </VButton>
              </dd>
            </div>
          </dl>
        </div>
      </div>
    </VCard>
  </div>
</template>
