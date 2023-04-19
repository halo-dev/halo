<script lang="ts" setup>
import {
  IconGitHub,
  VTag,
  VEntity,
  VEntityField,
  VStatusDot,
  Dialog,
  Toast,
  VDropdownItem,
  VDropdown,
  VDropdownDivider,
} from "@halo-dev/components";
import LazyImage from "@/components/image/LazyImage.vue";
import type { Theme } from "@halo-dev/api-client";
import { apiClient } from "@/utils/api-client";
import { toRefs } from "vue";
import { useThemeLifeCycle } from "../../composables/use-theme";
import { usePermission } from "@/utils/permission";
import { useI18n } from "vue-i18n";

const { currentUserHasPermission } = usePermission();
const { t } = useI18n();

const props = withDefaults(
  defineProps<{
    theme: Theme;
    isSelected?: boolean;
  }>(),
  {
    isSelected: false,
  }
);

const emit = defineEmits<{
  (event: "reload"): void;
  (event: "upgrade"): void;
  (event: "preview"): void;
}>();

const { theme } = toRefs(props);

const {
  isActivated,
  getFailedMessage,
  handleActiveTheme,
  handleResetSettingConfig,
} = useThemeLifeCycle(theme);

const handleUninstall = async (theme: Theme, deleteExtensions?: boolean) => {
  Dialog.warning({
    title: `${
      deleteExtensions
        ? t("core.theme.operations.uninstall_and_delete_config.title")
        : t("core.theme.operations.uninstall.title")
    }`,
    description: t("core.common.dialog.descriptions.cannot_be_recovered"),
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    onConfirm: async () => {
      try {
        await apiClient.extension.theme.deletethemeHaloRunV1alpha1Theme({
          name: theme.metadata.name,
        });

        // delete theme setting and configMap
        if (deleteExtensions) {
          const { settingName, configMapName } = theme.spec;

          if (settingName) {
            await apiClient.extension.setting.deletev1alpha1Setting(
              {
                name: settingName,
              },
              {
                mute: true,
              }
            );
          }

          if (configMapName) {
            await apiClient.extension.configMap.deletev1alpha1ConfigMap(
              {
                name: configMapName,
              },
              {
                mute: true,
              }
            );
          }
        }

        Toast.success(t("core.common.toast.uninstall_success"));
      } catch (e) {
        console.error("Failed to uninstall theme", e);
      } finally {
        emit("reload");
      }
    },
  });
};
</script>

<template>
  <VEntity :is-selected="isSelected">
    <template #start>
      <VEntityField>
        <template #description>
          <div class="w-32">
            <div
              class="group aspect-h-3 aspect-w-4 block w-full overflow-hidden rounded border bg-gray-100"
            >
              <LazyImage
                :key="theme.metadata.name"
                :src="theme.spec.logo"
                :alt="theme.spec.displayName"
                classes="pointer-events-none object-cover group-hover:opacity-75"
              >
                <template #loading>
                  <div
                    class="flex h-full items-center justify-center object-cover"
                  >
                    <span class="text-xs text-gray-400">
                      {{ $t("core.common.status.loading") }}...
                    </span>
                  </div>
                </template>
                <template #error>
                  <div
                    class="flex h-full items-center justify-center object-cover"
                  >
                    <span class="text-xs text-red-400">
                      {{ $t("core.common.status.loading_error") }}
                    </span>
                  </div>
                </template>
              </LazyImage>
            </div>
          </div>
        </template>
      </VEntityField>
      <VEntityField
        :title="theme.spec.displayName"
        :description="theme.spec.version"
      >
        <template #extra>
          <VTag v-if="isActivated">
            {{ $t("core.common.status.activated") }}
          </VTag>
        </template>
      </VEntityField>
    </template>
    <template #end>
      <VEntityField v-if="getFailedMessage()">
        <template #description>
          <VStatusDot v-tooltip="getFailedMessage()" state="warning" animate />
        </template>
      </VEntityField>
      <VEntityField v-if="theme.metadata.deletionTimestamp">
        <template #description>
          <VStatusDot
            v-tooltip="$t('core.common.status.deleting')"
            state="warning"
            animate
          />
        </template>
      </VEntityField>
      <VEntityField>
        <template #description>
          <a
            class="text-sm text-gray-400 hover:text-blue-600"
            :href="theme.spec.author.website"
            target="_blank"
            @click.stop
          >
            {{ theme.spec.author.name }}
          </a>
        </template>
      </VEntityField>
      <VEntityField>
        <template #description>
          <a
            :href="theme.spec.repo"
            class="text-gray-900 hover:text-blue-600"
            target="_blank"
          >
            <IconGitHub />
          </a>
        </template>
      </VEntityField>
    </template>
    <template
      v-if="currentUserHasPermission(['system:themes:manage'])"
      #dropdownItems
    >
      <VDropdownItem v-if="!isActivated" @click="handleActiveTheme(true)">
        {{ $t("core.common.buttons.active") }}
      </VDropdownItem>
      <VDropdownItem @click="emit('upgrade')">
        {{ $t("core.common.buttons.upgrade") }}
      </VDropdownItem>
      <VDropdownItem @click="emit('preview')">
        {{ $t("core.common.buttons.preview") }}
      </VDropdownItem>
      <VDropdownDivider />
      <VDropdown placement="right" :triggers="['click']">
        <VDropdownItem type="danger">
          {{ $t("core.common.buttons.uninstall") }}
        </VDropdownItem>
        <template #popper>
          <VDropdownItem
            v-close-popper.all
            type="danger"
            @click="handleUninstall(theme)"
          >
            {{ $t("core.common.buttons.uninstall") }}
          </VDropdownItem>
          <VDropdownItem
            v-close-popper.all
            type="danger"
            @click="handleUninstall(theme, true)"
          >
            {{ $t("core.theme.operations.uninstall_and_delete_config.button") }}
          </VDropdownItem>
        </template>
      </VDropdown>
      <VDropdownItem type="danger" @click="handleResetSettingConfig">
        {{ $t("core.common.buttons.reset") }}
      </VDropdownItem>
    </template>
  </VEntity>
</template>
