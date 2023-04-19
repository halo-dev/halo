<script lang="ts" setup>
import {
  IconTerminalBoxLine,
  IconClipboardLine,
  VAlert,
  VPageHeader,
  VCard,
  VButton,
  Toast,
  VDescription,
  VDescriptionItem,
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
  if (!globalInfo.value?.useAbsolutePermalink) {
    return true;
  }

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
          <VDescription>
            <VDescriptionItem :label="$t('core.actuator.fields.external_url')">
              <span v-if="globalInfo?.externalUrl">
                {{ globalInfo?.externalUrl }}
              </span>
              <span v-else>
                {{ $t("core.actuator.fields_values.external_url.not_setup") }}
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
            </VDescriptionItem>
            <VDescriptionItem
              :label="$t('core.actuator.fields.start_time')"
              :content="formatDatetime(startup?.timeline.startTime)"
            />
            <VDescriptionItem
              :label="$t('core.actuator.fields.timezone')"
              :content="globalInfo?.timeZone"
            />
            <VDescriptionItem
              :label="$t('core.actuator.fields.locale')"
              :content="globalInfo?.locale"
            />
          </VDescription>
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
          <VDescription>
            <VDescriptionItem
              v-if="info.build"
              :label="$t('core.actuator.fields.version')"
            >
              <a
                :href="`https://github.com/halo-dev/halo/releases/tag/v${info.build.version}`"
                class="hover:text-gray-600"
                target="_blank"
              >
                {{ info.build.version }}
              </a>
            </VDescriptionItem>
            <VDescriptionItem
              v-if="info.build"
              :label="$t('core.actuator.fields.build_time')"
              :content="formatDatetime(info.build.time)"
            />
            <VDescriptionItem v-if="info.git" label="Git Commit">
              <a
                :href="`https://github.com/halo-dev/halo/commit/${info.git.commit.id}`"
                class="hover:text-gray-600"
                target="_blank"
              >
                {{ info.git.commit.id }}
              </a>
            </VDescriptionItem>
            <VDescriptionItem
              label="Java"
              :content="
                [info.java.runtime.name, info.java.runtime.version].join(' / ')
              "
            />
            <VDescriptionItem
              :label="$t('core.actuator.fields.database')"
              :content="[info.database.name, info.database.version].join(' / ')"
            />
            <VDescriptionItem :label="$t('core.actuator.fields.os')">
              {{ info.os.name }} {{ info.os.version }} / {{ info.os.arch }}
            </VDescriptionItem>
            <VDescriptionItem
              :label="$t('core.actuator.fields.log')"
              vertical-center
            >
              <VButton size="sm" @click="handleDownloadLogfile()">
                {{ $t("core.common.buttons.download") }}
              </VButton>
            </VDescriptionItem>
          </VDescription>
        </div>
      </div>
    </VCard>
  </div>
</template>
