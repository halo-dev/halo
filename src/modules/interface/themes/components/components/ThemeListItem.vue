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

const { currentUserHasPermission } = usePermission();

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
        ? "确定要卸载该主题以及对应的配置吗？"
        : "确定要卸载该主题吗？"
    }`,
    description: "该操作不可恢复。",
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

        Toast.success("卸载成功");
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
                    <span class="text-xs text-gray-400">加载中...</span>
                  </div>
                </template>
                <template #error>
                  <div
                    class="flex h-full items-center justify-center object-cover"
                  >
                    <span class="text-xs text-red-400">加载异常</span>
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
          <VTag v-if="isActivated"> 当前启用 </VTag>
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
          <VStatusDot v-tooltip="`删除中`" state="warning" animate />
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
        启用
      </VButton>
      <VButton v-close-popper block type="default" @click="emit('upgrade')">
        升级
      </VButton>
      <VButton v-close-popper block type="default" @click="emit('preview')">
        预览
      </VButton>
      <FloatingDropdown class="w-full" placement="right" :triggers="['click']">
        <VButton block type="danger"> 卸载 </VButton>
        <template #popper>
          <div class="w-52 p-2">
            <VSpace class="w-full" direction="column">
              <VButton
                v-close-popper.all
                block
                type="danger"
                @click="handleUninstall(theme)"
              >
                卸载
              </VButton>
              <VButton
                v-close-popper.all
                block
                type="danger"
                @click="handleUninstall(theme, true)"
              >
                卸载并删除配置
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
        重置
      </VButton>
    </template>
  </VEntity>
</template>
