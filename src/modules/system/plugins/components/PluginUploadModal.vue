<script lang="ts" setup>
import { VModal, Dialog } from "@halo-dev/components";
import FilePondUpload from "@/components/upload/FilePondUpload.vue";
import { apiClient } from "@/utils/api-client";
import type { Plugin } from "@halo-dev/api-client";
import { computed, ref } from "vue";
import type { AxiosResponse } from "axios";

const props = withDefaults(
  defineProps<{
    visible: boolean;
    upgradePlugin?: Plugin;
  }>(),
  {
    visible: false,
    upgradePlugin: undefined,
  }
);

const emit = defineEmits<{
  (event: "update:visible", visible: boolean): void;
  (event: "close"): void;
}>();

const FilePondUploadRef = ref();

const modalTitle = computed(() => {
  return props.upgradePlugin
    ? `升级插件（${props.upgradePlugin.spec.displayName}）`
    : "安装插件";
});

const handleVisibleChange = (visible: boolean) => {
  emit("update:visible", visible);
  if (!visible) {
    emit("close");
    FilePondUploadRef.value.handleRemoveFiles();
  }
};

const uploadHandler = computed(() => {
  if (props.upgradePlugin) {
    return (file, config) =>
      apiClient.plugin.upgradePlugin(
        {
          name: props.upgradePlugin.metadata.name as string,
          file: file,
        },
        config
      );
  }
  return (file, config) =>
    apiClient.plugin.installPlugin(
      {
        file: file,
      },
      config
    );
});

const onUploaded = async (response: AxiosResponse) => {
  if (props.upgradePlugin) {
    handleVisibleChange(false);
    return;
  }

  const plugin = response.data as Plugin;
  handleVisibleChange(false);
  Dialog.success({
    title: "上传成功",
    description: "是否启动当前安装的插件？",
    onConfirm: async () => {
      try {
        const { data: pluginToUpdate } =
          await apiClient.extension.plugin.getpluginHaloRunV1alpha1Plugin({
            name: plugin.metadata.name,
          });
        pluginToUpdate.spec.enabled = true;

        await apiClient.extension.plugin.updatepluginHaloRunV1alpha1Plugin({
          name: pluginToUpdate.metadata.name,
          plugin: pluginToUpdate,
        });

        window.location.reload();
      } catch (e) {
        console.error(e);
      }
    },
  });
};
</script>
<template>
  <VModal
    :visible="visible"
    :width="500"
    :title="modalTitle"
    @update:visible="handleVisibleChange"
  >
    <FilePondUpload
      v-if="visible && uploadHandler"
      ref="FilePondUploadRef"
      :allow-multiple="false"
      :handler="uploadHandler"
      label-idle="点击选择文件或者拖拽文件到此处"
      @uploaded="onUploaded"
    />
  </VModal>
</template>
