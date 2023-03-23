<script lang="ts" setup>
import {
  IconGitHub,
  VButton,
  VSpace,
  VTag,
  VEntity,
  VEntityField,
  VStatusDot,
  Dialog,
  Toast,
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
              class="group aspect-w-4 aspect-h-3 block w-full overflow-hidden rounded border bg-gray-100"
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
      <VButton
        v-if="!isActivated"
        v-close-popper
        block
        type="secondary"
        @click="handleActiveTheme"
      >
        {{ $t("core.common.buttons.active") }}
      </VButton>
      <VButton v-close-popper block type="default" @click="emit('upgrade')">
        {{ $t("core.common.buttons.upgrade") }}
      </VButton>
      <VButton v-close-popper block type="default" @click="emit('preview')">
        {{ $t("core.common.buttons.preview") }}
      </VButton>
      <FloatingDropdown class="w-full" placement="right" :triggers="['click']">
        <VButton block type="danger">
          {{ $t("core.common.buttons.uninstall") }}
        </VButton>
        <template #popper>
          <div class="w-52 p-2">
            <VSpace class="w-full" direction="column">
              <VButton
                v-close-popper.all
                block
                type="danger"
                @click="handleUninstall(theme)"
              >
                {{ $t("core.common.buttons.uninstall") }}
              </VButton>
              <VButton
                v-close-popper.all
                block
                type="danger"
                @click="handleUninstall(theme, true)"
              >
                {{
                  $t("core.theme.operations.uninstall_and_delete_config.button")
                }}
              </VButton>
            </VSpace>
          </div>
        </template>
      </FloatingDropdown>
      <VButton
        v-close-popper
        block
        type="danger"
        @click="handleResetSettingConfig"
      >
        {{ $t("core.common.buttons.reset") }}
      </VButton>
    </template>
  </VEntity>
</template>
