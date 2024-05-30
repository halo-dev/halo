<script lang="ts" setup>
import type { GlobalInfo, Info, Startup } from "@/types";
import { apiClient } from "@/utils/api-client";
import { formatDatetime } from "@/utils/date";
import { usePermission } from "@/utils/permission";
import { useThemeStore } from "@console/stores/theme";
import type { Plugin } from "@halo-dev/api-client";
import {
  IconClipboardLine,
  IconTerminalBoxLine,
  Toast,
  VAlert,
  VButton,
  VCard,
  VDescription,
  VDescriptionItem,
  VLoading,
  VPageHeader,
  VTag,
} from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import { useClipboard } from "@vueuse/core";
import axios from "axios";
import { computed } from "vue";
import { useI18n } from "vue-i18n";

const { t } = useI18n();
const themeStore = useThemeStore();
const { currentUserHasPermission } = usePermission();

const { data: info } = useQuery<Info>({
  queryKey: ["system-info"],
  queryFn: async () => {
    const { data } = await axios.get<Info>(`/actuator/info`, {
      withCredentials: true,
    });
    return data;
  },
  retry: 0,
});

const { data: globalInfo } = useQuery<GlobalInfo>({
  queryKey: ["system-global-info"],
  queryFn: async () => {
    const { data } = await axios.get<GlobalInfo>(`/actuator/globalinfo`, {
      withCredentials: true,
    });
    return data;
  },
  retry: 0,
});

const { data: startup } = useQuery<Startup>({
  queryKey: ["system-startup-info"],
  queryFn: async () => {
    const { data } = await axios.get<Startup>(`/actuator/startup`, {
      withCredentials: true,
    });
    return data;
  },
  retry: 0,
});

const { data: plugins, isLoading: isPluginsLoading } = useQuery<Plugin[]>({
  queryKey: ["enabled-plugins"],
  queryFn: async () => {
    const { data } = await apiClient.plugin.listPlugins({
      page: 0,
      size: 0,
      enabled: true,
    });

    return data.items;
  },
  enabled: computed(() => currentUserHasPermission(["system:plugins:view"])),
});

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

// copy system information to clipboard
const { copy, isSupported } = useClipboard({ legacy: true });

interface CopyItem {
  label: string;
  value?: string;
  href?: string;
  children?: CopyItem[];
}

const handleCopy = () => {
  if (!isSupported.value) {
    Toast.warning(t("core.overview.actions.copy.toast_browser_not_supported"));
    return;
  }

  const copyItems: CopyItem[] = [
    {
      label: t("core.overview.fields.external_url"),
      value: globalInfo.value?.externalUrl || "",
    },
    {
      label: t("core.overview.fields.start_time"),
      value: formatDatetime(startup.value?.timeline.startTime) || "",
    },
    {
      label: t("core.overview.fields.version"),
      value: info.value?.build?.version || "",
    },
    {
      label: t("core.overview.fields.build_time"),
      value: formatDatetime(info.value?.build?.time) || "",
    },
    {
      label: "Git Commit",
      value: info.value?.git?.commit.id || "",
    },
    {
      label: "Java",
      value:
        [info.value?.java.runtime.name, info.value?.java.runtime.version]
          .filter(Boolean)
          .join(" / ")
          .trim() || "",
    },
    {
      label: t("core.overview.fields.database"),
      value:
        [info.value?.database.name, info.value?.database.version]
          .filter(Boolean)
          .join(" / ") || "",
    },
    {
      label: t("core.overview.fields.os"),
      value:
        [info.value?.os.name, info.value?.os.version]
          .filter(Boolean)
          .join(" / ") || "",
    },
    {
      label: t("core.overview.fields.activated_theme"),
      value: `${themeStore.activatedTheme?.spec.displayName} ${themeStore.activatedTheme?.spec.version}`,
      href:
        themeStore.activatedTheme?.spec.repo ||
        themeStore.activatedTheme?.spec.homepage,
    },
    {
      label: t("core.overview.fields.enabled_plugins"),
      children: plugins.value?.map((plugin) => ({
        value: `${plugin.spec.displayName} ${plugin.spec.version}`,
        href: plugin.spec.repo || plugin.spec.homepage,
      })) as CopyItem[],
    },
  ];

  const text = copyItems
    .map((item) => {
      if (item.children?.length) {
        const childrenText = item.children
          .map(
            (child) =>
              `  - ${
                child.href ? `[${child.value}](${child.href})` : child.value
              }`
          )
          .filter(Boolean)
          .join("\n");
        return `- ${item.label}:\n${childrenText}`;
      } else {
        return `- ${item.label}: ${
          item.href ? `[${item.value}](${item.href})` : item.value || ""
        }`;
      }
    })
    .join("\n");

  copy(text);

  Toast.success(t("core.common.toast.copy_success"));
};

const handleDownloadLogfile = () => {
  axios
    .get(`/actuator/logfile`)
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
  <VPageHeader :title="$t('core.overview.title')">
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
              {{ $t("core.overview.header.titles.general") }}
            </h3>
          </div>
        </div>
        <div class="border-t border-gray-200">
          <VDescription>
            <VDescriptionItem :label="$t('core.overview.fields.external_url')">
              <span v-if="globalInfo?.externalUrl">
                {{ globalInfo?.externalUrl }}
              </span>
              <span v-else>
                {{ $t("core.overview.fields_values.external_url.not_setup") }}
              </span>
              <VAlert
                v-if="!isExternalUrlValid"
                class="mt-3"
                type="warning"
                :title="$t('core.common.text.warning')"
                :closable="false"
              >
                <template #description>
                  {{ $t("core.overview.alert.external_url_invalid") }}
                </template>
              </VAlert>
            </VDescriptionItem>
            <VDescriptionItem
              v-if="startup?.timeline.startTime"
              :label="$t('core.overview.fields.start_time')"
              :content="formatDatetime(startup?.timeline.startTime)"
            />
            <VDescriptionItem
              v-if="themeStore.activatedTheme"
              :label="$t('core.overview.fields.activated_theme')"
            >
              <VTag @click="$router.push({ name: 'ThemeDetail' })">
                <template v-if="themeStore.activatedTheme.spec.logo" #leftIcon>
                  <img
                    class="h-3.5 w-3.5 rounded-sm"
                    :src="themeStore.activatedTheme.spec.logo"
                    :alt="themeStore.activatedTheme.spec.displayName"
                  />
                </template>
                {{ themeStore.activatedTheme.spec.displayName }}
              </VTag>
            </VDescriptionItem>
            <VDescriptionItem
              v-permission="['system:plugins:view']"
              :label="$t('core.overview.fields.enabled_plugins')"
            >
              <VLoading v-if="isPluginsLoading" />
              <span v-else-if="!plugins?.length">
                {{ $t("core.common.text.none") }}
              </span>
              <div v-else class="flex flex-wrap gap-1.5">
                <VTag
                  v-for="plugin in plugins"
                  :key="plugin.metadata.name"
                  @click="
                    $router.push({
                      name: 'PluginDetail',
                      params: { name: plugin.metadata.name },
                    })
                  "
                >
                  <template v-if="plugin.status?.logo" #leftIcon>
                    <img
                      class="h-3.5 w-3.5 rounded-sm"
                      :src="plugin.status?.logo"
                      :alt="plugin.spec.displayName"
                    />
                  </template>
                  {{ plugin.spec.displayName }}
                </VTag>
              </div>
            </VDescriptionItem>
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
              {{ $t("core.overview.header.titles.environment") }}
            </h3>
          </div>
        </div>
        <div class="border-t border-gray-200">
          <VDescription>
            <VDescriptionItem
              v-if="info.build"
              :label="$t('core.overview.fields.version')"
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
              :label="$t('core.overview.fields.build_time')"
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
            <VDescriptionItem :label="$t('core.overview.fields.database')">
              <span>
                {{ [info.database.name, info.database.version].join(" / ") }}
              </span>
              <VAlert
                v-if="info.database.name.startsWith('H2')"
                class="mt-3"
                type="warning"
                :title="$t('core.common.text.warning')"
                :closable="false"
              >
                <template #description>
                  {{ $t("core.overview.alert.h2_warning") }}
                </template>
              </VAlert>
            </VDescriptionItem>
            <VDescriptionItem :label="$t('core.overview.fields.os')">
              {{ info.os.name }} {{ info.os.version }} / {{ info.os.arch }}
            </VDescriptionItem>
            <VDescriptionItem
              :label="$t('core.overview.fields.timezone')"
              :content="globalInfo?.timeZone"
            />
            <VDescriptionItem
              :label="$t('core.overview.fields.locale')"
              :content="globalInfo?.locale"
            />
            <VDescriptionItem
              :label="$t('core.overview.fields.log')"
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
